package com.greenhome.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.greenhome.app.data.*
import com.greenhome.app.notifications.ReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.greenhome.app.data.GreenPreferences
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class GreenHomeViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as GreenHomeApp).database
    private val plantsDao = db.plantDao(); private val eventsDao = db.eventDao()
    private val preferences = GreenPreferences(app)
    val reminderTime = preferences.time.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "09:00")
    fun setReminderTime(value: String) = viewModelScope.launch {
        preferences.setTime(value)
        eventsDao.getAll().forEach { schedule(it) }
    }
    val plants = plantsDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val events = eventsDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun eventsFor(date: String) = eventsDao.observeDate(date)
    fun eventsForPlant(id: Long) = eventsDao.observePlant(id)
    fun savePlant(plant: Plant) = viewModelScope.launch {
        val isNew = plant.id == 0L
        val id = if (isNew) plantsDao.insert(plant) else { plantsDao.update(plant); plant.id }
        if (!isNew) eventsDao.getAll().filter { it.plantId == id }.forEach { schedule(it) }
        if (isNew) {
            val today = LocalDate.now()
            // Если пользователь указал последний полив, заносим его в календарь
            // как уже выполненное событие, сохраняя обе даты.
            val lastWaterDate = plant.lastWatered?.let { raw ->
                runCatching { LocalDate.parse(raw) }.getOrNull()
            }
            if (lastWaterDate != null) {
                eventsDao.insert(
                    CareEvent(
                        plantId = id,
                        date = lastWaterDate.toString(),
                        type = "Полив",
                        completed = true,
                        completedAt = lastWaterDate.toString()
                    )
                )
            }

            val nextWater = lastWaterDate?.plusDays(plant.wateringEveryDays.coerceAtLeast(1).toLong()) ?: today
            val nextFeed = today.plusDays(plant.fertilizingEveryDays.coerceAtLeast(1).toLong())
            val waterEvent = CareEvent(plantId = id, date = nextWater.toString(), type = "Полив")
            val feedEvent = CareEvent(plantId = id, date = nextFeed.toString(), type = "Удобрение", fertilizer = plant.fertilizerType)
            schedule(waterEvent.copy(id = eventsDao.insert(waterEvent)))
            schedule(feedEvent.copy(id = eventsDao.insert(feedEvent)))
        }
    }
    fun deletePlant(plant: Plant) = viewModelScope.launch { plantsDao.delete(plant) }
    fun saveEvent(event: CareEvent) = viewModelScope.launch {
        val previous = if (event.id == 0L) null else eventsDao.get(event.id)
        // Плановая дата (date) остаётся неизменной. Фактический день выполнения
        // хранится отдельно, чтобы история показывала обе даты.
        val completedNow = event.completed && previous?.completed != true
        val completedAt = when {
            !event.completed -> null
            completedNow -> LocalDate.now().toString()
            else -> previous?.completedAt ?: event.completedAt ?: LocalDate.now().toString()
        }
        val savedEvent = event.copy(completedAt = completedAt)
        val id = if (savedEvent.id == 0L) eventsDao.insert(savedEvent) else { eventsDao.update(savedEvent); savedEvent.id }
        val persisted = savedEvent.copy(id = id)
        schedule(persisted)

        if (persisted.type == "Полив") {
            plantsDao.get(persisted.plantId)?.let { plantsDao.update(it.copy(lastWatered = eventsDao.latestCompletedWater(persisted.plantId))) }
        }

        // После выполнения создаём следующий цикл, если он ещё не запланирован.
        if (completedNow) createNextCareEvent(persisted)
    }

    private suspend fun createNextCareEvent(completedEvent: CareEvent) {
        val plant = plantsDao.get(completedEvent.plantId) ?: return
        // Ищем уже созданную следующую процедуру относительно плановой даты
        // текущей записи, а не относительно сегодняшнего дня. Иначе при
        // выполнении просроченной/будущей записи можно принять не ту запись
        // за следующий цикл.
        val scheduledDate = runCatching { LocalDate.parse(completedEvent.date) }.getOrNull() ?: LocalDate.now()
        val nextAlreadyScheduled = eventsDao.findNextPending(
            plantId = completedEvent.plantId,
            type = completedEvent.type,
            afterDate = scheduledDate.plusDays(1).toString()
        )
        if (nextAlreadyScheduled != null) return

        val interval = if (completedEvent.type == "Полив") plant.wateringEveryDays else plant.fertilizingEveryDays
        val completionDate = completedEvent.completedAt?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
        // Если процедуру отметили раньше запланированного дня, не сдвигаем
        // календарный цикл назад: следующая дата будет позже обеих дат.
        val cycleStart = maxOf(completionDate, scheduledDate)
        val nextDate = cycleStart.plusDays(interval.coerceAtLeast(1).toLong()).toString()
        val nextEvent = CareEvent(
            plantId = plant.id,
            date = nextDate,
            type = completedEvent.type,
            fertilizer = if (completedEvent.type == "Удобрение") plant.fertilizerType else ""
        )
        val nextId = eventsDao.insert(nextEvent)
        schedule(nextEvent.copy(id = nextId))
    }
    fun deleteEvent(event: CareEvent) = viewModelScope.launch {
        WorkManager.getInstance(getApplication()).cancelUniqueWork("care-${event.id}")
        eventsDao.delete(event)
        if (event.type == "Полив") plantsDao.get(event.plantId)?.let { plantsDao.update(it.copy(lastWatered = eventsDao.latestCompletedWater(event.plantId))) }
    }
    fun toggleEvent(event: CareEvent) = saveEvent(event.copy(completed = !event.completed))
    fun setRemindersEnabled(plant: Plant, enabled: Boolean) = savePlant(plant.copy(remindersEnabled = enabled))
    private suspend fun schedule(event: CareEvent) {
        val plant = plantsDao.get(event.plantId) ?: return
        val wm = WorkManager.getInstance(getApplication())
        wm.cancelUniqueWork("care-${event.id}")
        if (!plant.remindersEnabled || event.completed) return
        val clock = runCatching { java.time.LocalTime.parse(preferences.time.first()) }.getOrDefault(java.time.LocalTime.of(9, 0))
        val target = runCatching { LocalDate.parse(event.date).atTime(clock).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull() ?: return
        val delay = (target - System.currentTimeMillis()).coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("title" to "${event.type}: ${plant.name}", "body" to if (event.type == "Полив") "Пора полить растение" else "Пора внести подкормку", "eventId" to event.id)).build()
        wm.enqueueUniqueWork("care-${event.id}", ExistingWorkPolicy.REPLACE, request)
    }
    fun completedCount(type: String, month: String, callback: (Int) -> Unit) = viewModelScope.launch { callback(eventsDao.countCompleted(type, month)) }
}
