package com.greenhome.app

import android.Manifest
import android.os.Build
import android.app.TimePickerDialog
import java.time.Instant
import java.time.ZoneOffset
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.greenhome.app.data.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Sage = Color(0xFF82977B); private val Olive = Color(0xFF465B43); private val Mint = Color(0xFFDDE9D9)
private val Cream = Color(0xFFF6F5EE); private val Ink = Color(0xFF263528); private val Muted = Color(0xFF829080)

private fun displayDate(isoDate: String?): String = isoDate?.let { raw ->
    runCatching { LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) }.getOrDefault(raw)
} ?: "не указан"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent {
        val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
        LaunchedEffect(Unit) { if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS) }
        GreenHomeTheme { GreenHomeRoot() }
    } }
}

@Composable fun GreenHomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Olive, secondary = Sage, background = Cream, surface = Color.White, onBackground = Ink, onSurface = Ink), content = content)
}

@Composable fun GreenHomeRoot(vm: GreenHomeViewModel = viewModel()) {
    var tab by remember { mutableStateOf(0) }
    Scaffold(containerColor = Cream, bottomBar = {
        NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
            val labels = listOf("Главная", "Растения", "Календарь", "История", "Настройки")
            val icons = listOf(Icons.Default.Home, Icons.Default.LocalFlorist, Icons.Default.CalendarMonth, Icons.Default.History, Icons.Default.Settings)
            labels.forEachIndexed { i, label -> NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icons[i], null) }, label = { Text(label, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Olive, indicatorColor = Mint, selectedTextColor = Olive, unselectedTextColor = Muted)) }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> HomeScreen(vm, onPlants = { tab = 1 }, onCalendar = { tab = 2 })
                1 -> PlantsScreen(vm)
                2 -> CalendarScreen(vm)
                3 -> HistoryScreen(vm)
                else -> SettingsScreen(vm)
            }
        }
    }
}

@Composable private fun ScreenTitle(title: String, subtitle: String? = null) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp)) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink)
        if (subtitle != null) Text(subtitle, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable private fun HomeScreen(vm: GreenHomeViewModel, onPlants: () -> Unit, onCalendar: () -> Unit) {
    val plants by vm.plants.collectAsState(); val events by vm.events.collectAsState(); val today = LocalDate.now(); val todayString = today.toString()
    val due = events.filter { it.date == todayString && !it.completed }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Column(Modifier.padding(horizontal = 22.dp).padding(top = 8.dp)) {
            Text(today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))).replaceFirstChar { it.uppercase() }, color = Muted, fontSize = 13.sp)
            Text("Добрый день 🌿", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("Ваш зелёный уголок в порядке?", color = Muted, fontSize = 14.sp)
        } }
        item {
            Box(Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(168.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFE2EBDD))) {
                Column(Modifier.align(Alignment.CenterStart).padding(20.dp).width(190.dp)) {
                    Text("Растём вместе", color = Olive, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Немного заботы сегодня — пышная зелень завтра.", color = Color(0xFF61745C), fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp))
                    Text("${plants.size} растений в коллекции", color = Olive, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                }
                Text("🪴", fontSize = 78.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp))
            }
        }
        item { Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Сегодня нужно позаботиться", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("${due.size} процедур ожидают вас", color = Muted, fontSize = 12.sp) }
            TextButton(onClick = onCalendar) { Text("Календарь", color = Olive) }
        } }
        if (due.isEmpty()) item { Surface(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = Color.White) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Text("☀️", fontSize = 28.sp); Spacer(Modifier.width(12.dp)); Column { Text("На сегодня всё спокойно", fontWeight = FontWeight.SemiBold); Text("Можно просто полюбоваться зеленью", color = Muted, fontSize = 12.sp) } } } }
        items(due, key = { it.id }) { event -> EventRow(event, plants.firstOrNull { it.id == event.plantId }?.name ?: "Растение", onToggle = { vm.toggleEvent(event) }, onDelete = { vm.deleteEvent(event) }, onEdit = { }) }
        item { Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Моя коллекция", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = onPlants) { Text("Все растения", color = Olive) } } }
        items(plants.take(4), key = { it.id }) { PlantRow(it, events.filter { e -> e.plantId == it.id }, onEdit = { onPlants() }) }
        item { Button(onClick = onPlants, modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Olive)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Добавить растение") } }
    }
}

@Composable private fun PlantRow(plant: Plant, events: List<CareEvent>, onEdit: () -> Unit, onDelete: (() -> Unit)? = null) {
    val next = events.filter { !it.completed && it.date >= LocalDate.now().toString() }.minByOrNull { it.date }
    Surface(onClick = onEdit, modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            PlantPhoto(plant, 64)
            Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(plant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text("Последний полив: ${displayDate(plant.lastWatered)}", color = Muted, fontSize = 11.sp); Text(if (next != null) "${next.type} · ${displayDate(next.date)}" else "Нет запланированных процедур", color = Sage, fontSize = 11.sp) }
            if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить растение", tint = Color(0xFFB45E50)) } else Icon(Icons.Default.ChevronRight, null, tint = Muted)
        }
    }
}

@Composable private fun PlantPhoto(plant: Plant, size: Int) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(18.dp)).background(Mint), contentAlignment = Alignment.Center) {
        if (plant.photoUri != null) AsyncImage(model = plant.photoUri, contentDescription = plant.name, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        else Text("🌿", fontSize = (size * .48).sp)
    }
}

@Composable private fun PlantsScreen(vm: GreenHomeViewModel) {
    val plants by vm.plants.collectAsState(); val events by vm.events.collectAsState(); var editing by remember { mutableStateOf<Plant?>(null) }; var showAdd by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(end = 18.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { ScreenTitle("Мои растения", "Ваша домашняя оранжерея") }; IconButton(onClick = { editing = null; showAdd = true }) { Icon(Icons.Default.AddCircle, null, tint = Olive, modifier = Modifier.size(32.dp)) } }
        if (plants.isEmpty()) EmptyState("Пока здесь тихо", "Добавьте первое растение и начните вести его историю.", "Добавить растение") { showAdd = true }
        else LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(plants, key = { it.id }) { p ->
            PlantRow(p, events.filter { it.plantId == p.id }, onEdit = { editing = p; showAdd = true }, onDelete = { vm.deletePlant(p) })
        } }
    }
    if (showAdd) PlantDialog(editing, onDismiss = { showAdd = false }, onSave = { vm.savePlant(it); showAdd = false })
}

@Composable private fun PlantDialog(plant: Plant?, onDismiss: () -> Unit, onSave: (Plant) -> Unit) {
    var name by remember { mutableStateOf(plant?.name ?: "") }; var description by remember { mutableStateOf(plant?.description ?: "") }
    var photo by remember { mutableStateOf(plant?.photoUri) }; var water by remember { mutableStateOf((plant?.wateringEveryDays ?: 7).toString()) }; var feed by remember { mutableStateOf((plant?.fertilizingEveryDays ?: 30).toString()) }
    var fertilizer by remember { mutableStateOf(plant?.fertilizerType ?: "Универсальное") }; var last by remember { mutableStateOf(plant?.lastWatered ?: "") }; var reminders by remember { mutableStateOf(plant?.remindersEnabled ?: true) }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; photo = uri.toString() }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (plant == null) "Новое растение" else "Редактировать растение") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(Mint), contentAlignment = Alignment.Center) { if (photo != null) AsyncImage(photo, null, Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop) else Text("🌱", fontSize = 30.sp) }; TextButton(onClick = { picker.launch(arrayOf("image/*")) }) { Text("Выбрать фото") } }
            OutlinedTextField(name, { name = it }, label = { Text("Название") }, singleLine = true)
            OutlinedTextField(description, { description = it }, label = { Text("Описание и заметки") })
            DatePickerField(label = "Последний полив", value = last, onValueChange = { last = it }, allowEmpty = true)
            OutlinedTextField(water, { water = it.filter(Char::isDigit) }, label = { Text("Поливать каждые (дней)") }, singleLine = true)
            OutlinedTextField(feed, { feed = it.filter(Char::isDigit) }, label = { Text("Удобрять каждые (дней)") }, singleLine = true)
            OutlinedTextField(fertilizer, { fertilizer = it }, label = { Text("Тип удобрения") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Напоминания", modifier = Modifier.weight(1f)); Switch(reminders, { reminders = it }) }
        }
    }, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onSave(Plant(id = plant?.id ?: 0, name = name.trim(), description = description, photoUri = photo, lastWatered = last.ifBlank { null }, wateringEveryDays = water.toIntOrNull()?.coerceAtLeast(1) ?: 7, fertilizingEveryDays = feed.toIntOrNull()?.coerceAtLeast(1) ?: 30, fertilizerType = fertilizer, remindersEnabled = reminders)) }) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}

@Composable private fun CalendarScreen(vm: GreenHomeViewModel) {
    val events by vm.events.collectAsState(); val plants by vm.plants.collectAsState()
    var month by remember { mutableStateOf(YearMonth.now()) }; var selected by remember { mutableStateOf(LocalDate.now()) }
    var editing by remember { mutableStateOf<CareEvent?>(null) }; var showDialog by remember { mutableStateOf(false) }
    val dayEvents = events.filter { it.date == selected.toString() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { ScreenTitle("Календарь ухода", "Планируйте заботу на любую дату") }
        item {
            Surface(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(26.dp), color = Color.White, shadowElevation = 2.dp) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { month = month.minusMonths(1); selected = month.atDay(minOf(selected.dayOfMonth, month.lengthOfMonth())) }) { Icon(Icons.Default.ChevronLeft, null) }
                        Text(month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))).replaceFirstChar { it.uppercase() }, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        IconButton(onClick = { month = month.plusMonths(1); selected = month.atDay(minOf(selected.dayOfMonth, month.lengthOfMonth())) }) { Icon(Icons.Default.ChevronRight, null) }
                    }
                    Row(Modifier.fillMaxWidth()) { listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { Text(it, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium) } }
                    val firstOffset = month.atDay(1).dayOfWeek.value - 1
                    val totalCells = ((firstOffset + month.lengthOfMonth() + 6) / 7) * 7
                    for (week in 0 until totalCells / 7) {
                        Row(Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                val day = week * 7 + col - firstOffset + 1
                                if (day in 1..month.lengthOfMonth()) {
                                    val date = month.atDay(day); val list = events.filter { it.date == date.toString() }; val chosen = selected == date
                                    Column(Modifier.weight(1f).padding(vertical = 3.dp).clip(RoundedCornerShape(12.dp)).background(if (chosen) Mint else Color.Transparent).clickable { selected = date }.padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(day.toString(), fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal, color = if (chosen) Olive else Ink, fontSize = 13.sp)
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(11.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Внутренняя строка занимает только ширину меток и центрируется под числом.
                                            // Все интервалы между соседними метками одинаковые — 4.dp.
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val waterCount = list.count { it.type == "Полив" }
                                                val feedCount = list.count { it.type == "Удобрение" }
                                                if (waterCount > 0) {
                                                    if (waterCount >= 3) {
                                                        Box(Modifier.width(17.dp).height(4.dp).clip(CircleShape).background(Color(0xFF70A7C0)))
                                                    } else {
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                            repeat(waterCount) { Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF70A7C0))) }
                                                        }
                                                    }
                                                }
                                                if (waterCount > 0 && feedCount > 0) Spacer(Modifier.width(4.dp))
                                                if (feedCount > 0) {
                                                    if (feedCount >= 3) {
                                                        Box(Modifier.width(17.dp).height(4.dp).clip(CircleShape).background(Color(0xFFB79A55)))
                                                    } else {
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                            repeat(feedCount) { Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFB79A55))) }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 10.dp)) { LegendDot(Color(0xFF70A7C0), "Полив"); LegendDot(Color(0xFFB79A55), "Удобрение") }
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 22.dp, vertical = 5.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(displayDate(selected.toString()), fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("${dayEvents.size} процедур", color = Muted, fontSize = 12.sp) }
                TextButton(onClick = { editing = null; showDialog = true }) { Icon(Icons.Default.Add, null); Text("Добавить", color = Olive) }
            }
        }
        if (dayEvents.isEmpty()) item { EmptyState("День свободен", "Добавьте полив или подкормку на выбранную дату.", "Добавить процедуру") { editing = null; showDialog = true } }
        else items(dayEvents, key = { it.id }) { event ->
            EventRow(event, plants.firstOrNull { it.id == event.plantId }?.name ?: "Растение", onToggle = { vm.toggleEvent(event) }, onDelete = { vm.deleteEvent(event) }, onEdit = { editing = event; showDialog = true })
        }
    }
    if (showDialog) EventDialog(event = editing, initialDate = selected.toString(), plants = plants, onDismiss = { showDialog = false }, onSave = { vm.saveEvent(it); selected = LocalDate.parse(it.date); month = YearMonth.from(selected); showDialog = false })
}

@Composable private fun LegendDot(color: Color, label: String) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).clip(CircleShape).background(color)); Spacer(Modifier.width(5.dp)); Text(label, fontSize = 11.sp, color = Muted) } }

@Composable private fun EventDialog(event: CareEvent?, initialDate: String, plants: List<Plant>, onDismiss: () -> Unit, onSave: (CareEvent) -> Unit) {
    var plantId by remember { mutableStateOf(event?.plantId ?: plants.firstOrNull()?.id ?: 0L) }
    var date by remember { mutableStateOf(event?.date ?: initialDate) }; var type by remember { mutableStateOf(event?.type ?: "Полив") }
    var fertilizer by remember { mutableStateOf(event?.fertilizer ?: "") }; var dosage by remember { mutableStateOf(event?.dosage ?: "") }; var note by remember { mutableStateOf(event?.note ?: "") }; var completed by remember { mutableStateOf(event?.completed ?: false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (event == null) "Новая процедура" else "Изменить процедуру") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (plants.isEmpty()) Text("Сначала добавьте растение в коллекцию.", color = Muted)
            else {
                Text("Растение", fontSize = 12.sp, color = Muted)
                var expanded by remember { mutableStateOf(false) }
                Box { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(plants.firstOrNull { it.id == plantId }?.name ?: "Выбрать растение") }; DropdownMenu(expanded, { expanded = false }) { plants.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { plantId = it.id; expanded = false }) } } }
                Text("Тип процедуры", fontSize = 12.sp, color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Полив", "Удобрение").forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) }) } }
                DatePickerField(label = "Дата процедуры", value = date, onValueChange = { date = it })
                if (type == "Удобрение") { OutlinedTextField(fertilizer, { fertilizer = it }, label = { Text("Название удобрения") }); OutlinedTextField(dosage, { dosage = it }, label = { Text("Дозировка") }) }
                OutlinedTextField(note, { note = it }, label = { Text("Заметка") })
                if (event != null) Row(verticalAlignment = Alignment.CenterVertically) { Text("Выполнено", Modifier.weight(1f)); Switch(completed, { completed = it }) }
            }
        }
    }, confirmButton = { TextButton(enabled = plants.isNotEmpty() && runCatching { LocalDate.parse(date); true }.getOrDefault(false), onClick = { onSave(CareEvent(id = event?.id ?: 0, plantId = plantId, date = date, type = type, completed = completed, fertilizer = fertilizer, dosage = dosage, note = note)) }) { Text(if (event == null) "Добавить" else "Сохранить") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun DatePickerField(label: String, value: String, onValueChange: (String) -> Unit, allowEmpty: Boolean = false) {
    var showPicker by remember { mutableStateOf(false) }
    val initialDate = remember(value) { runCatching { LocalDate.parse(value) }.getOrDefault(LocalDate.now()) }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Muted, fontSize = 12.sp)
        OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CalendarMonth, null); Spacer(Modifier.width(8.dp))
            Text(if (value.isBlank()) "Выбрать дату" else runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) }.getOrDefault(value), Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, null)
        }
        if (allowEmpty && value.isNotBlank()) TextButton(onClick = { onValueChange("") }) { Text("Очистить дату") }
    }
    if (showPicker) DatePickerDialog(onDismissRequest = { showPicker = false }, confirmButton = {
        TextButton(onClick = {
            pickerState.selectedDateMillis?.let { millis -> onValueChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()) }
            showPicker = false
        }) { Text("Выбрать") }
    }, dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Отмена") } }) { DatePicker(state = pickerState) }
}

@Composable private fun FertilizerBagIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val bag = Color(0xFFB69A62)
        val fold = Color(0xFF806A43)
        val label = Color(0xFFF3E7C9)
        val grain = Color(0xFF806A43)

        // Широкий бумажный мешок с плоским дном и запаянным верхом.
        val body = Path().apply {
            moveTo(w * .23f, h * .25f)
            lineTo(w * .77f, h * .25f)
            lineTo(w * .84f, h * .82f)
            quadraticTo(w * .84f, h * .90f, w * .75f, h * .90f)
            lineTo(w * .25f, h * .90f)
            quadraticTo(w * .16f, h * .90f, w * .16f, h * .82f)
            close()
        }
        drawPath(body, bag)
        // Загнутый/запаянный край упаковки — характерный верх мешка, не распылитель.
        drawRoundRect(fold, topLeft = androidx.compose.ui.geometry.Offset(w * .22f, h * .15f), size = androidx.compose.ui.geometry.Size(w * .56f, h * .16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .035f, h * .035f))
        drawLine(label, androidx.compose.ui.geometry.Offset(w * .27f, h * .23f), androidx.compose.ui.geometry.Offset(w * .73f, h * .23f), strokeWidth = w * .025f)
        // Большая центральная этикетка и гранулы удобрения.
        drawRoundRect(label, topLeft = androidx.compose.ui.geometry.Offset(w * .29f, h * .40f), size = androidx.compose.ui.geometry.Size(w * .42f, h * .30f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .035f, h * .035f))
        drawLine(bag, androidx.compose.ui.geometry.Offset(w * .37f, h * .48f), androidx.compose.ui.geometry.Offset(w * .63f, h * .48f), strokeWidth = w * .035f)
        listOf(.40f to .59f, .50f to .56f, .60f to .59f).forEach { (x, y) ->
            drawCircle(grain, radius = w * .035f, center = androidx.compose.ui.geometry.Offset(w * x, h * y))
        }
        // Складки бумажного мешка по бокам.
        drawLine(fold.copy(alpha = .65f), androidx.compose.ui.geometry.Offset(w * .22f, h * .34f), androidx.compose.ui.geometry.Offset(w * .27f, h * .79f), strokeWidth = w * .025f)
        drawLine(fold.copy(alpha = .65f), androidx.compose.ui.geometry.Offset(w * .78f, h * .34f), androidx.compose.ui.geometry.Offset(w * .73f, h * .79f), strokeWidth = w * .025f)
    }
}

@Composable private fun EventRow(event: CareEvent, plantName: String, onToggle: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    Surface(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(19.dp), color = Color.White, shadowElevation = 1.dp) {
        Row(Modifier.clickable { onEdit() }.padding(horizontal = 13.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(if (event.type == "Полив") Color(0xFFE5F0F5) else Color(0xFFF2EBD9)), contentAlignment = Alignment.Center) {
                if (event.type == "Полив") Text("💧", fontSize = 20.sp) else FertilizerBagIcon(Modifier.size(25.dp))
            }
            Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) {
                Text(event.type, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (event.completed) Muted else Ink)
                Text(plantName, fontSize = 11.sp, color = Muted)
                if (event.completed) {
                    Text("План: ${displayDate(event.date)}", fontSize = 11.sp, color = Muted)
                    Text("Выполнено: ${displayDate(event.completedAt ?: event.date)}", fontSize = 11.sp, color = Sage)
                } else {
                    Text("Запланировано: ${displayDate(event.date)}", fontSize = 11.sp, color = Muted)
                }
                if (event.note.isNotBlank()) Text(event.note, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (event.type == "Удобрение" && event.fertilizer.isNotBlank()) Text("${event.fertilizer} ${event.dosage}", fontSize = 11.sp, color = Sage)
            }
            Checkbox(checked = event.completed, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = Sage))
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFB58B82)) }
        }
    }
}

@Composable private fun HistoryScreen(vm: GreenHomeViewModel) {
    val events by vm.events.collectAsState(); val plants by vm.plants.collectAsState(); val month = YearMonth.now().toString()
    val watered = events.count { it.type == "Полив" && it.completed && (it.completedAt ?: it.date).startsWith(month) }; val fed = events.count { it.type == "Удобрение" && it.completed && (it.completedAt ?: it.date).startsWith(month) }
    var editing by remember { mutableStateOf<CareEvent?>(null) }; var showDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("История ухода", "Маленькие шаги к большому росту")
        Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Поливов", watered.toString(), "💧", Modifier.weight(1f)); StatCard("Подкормок", fed.toString(), "", Modifier.weight(1f), fertilizerIcon = true)
        }
        Text("Все процедуры", Modifier.padding(horizontal = 22.dp, vertical = 16.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (events.isEmpty()) EmptyState("История пока пуста", "Отмечайте выполненные процедуры — они появятся здесь.", null, null)
        else LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { items(events, key = { it.id }) { e -> EventRow(e, plants.firstOrNull { it.id == e.plantId }?.name ?: "Растение", onToggle = { vm.toggleEvent(e) }, onDelete = { vm.deleteEvent(e) }, onEdit = { editing = e; showDialog = true }) } }
    }
    if (showDialog) EventDialog(event = editing, initialDate = editing?.date ?: LocalDate.now().toString(), plants = plants, onDismiss = { showDialog = false }, onSave = { vm.saveEvent(it); showDialog = false })
}

@Composable private fun StatCard(label: String, value: String, emoji: String, modifier: Modifier = Modifier, fertilizerIcon: Boolean = false) {
    Surface(modifier, shape = RoundedCornerShape(22.dp), color = Color.White, shadowElevation = 1.dp) { Column(Modifier.padding(17.dp)) { if (fertilizerIcon) FertilizerBagIcon(Modifier.size(25.dp)) else Text(emoji, fontSize = 23.sp); Text(value, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Olive); Text(label, fontSize = 12.sp, color = Muted) } }
}

@Composable private fun SettingsScreen(vm: GreenHomeViewModel) {
    val plants by vm.plants.collectAsState(); val reminderTime by vm.reminderTime.collectAsState(); val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Настройки", "Ваш зелёный дом — ваши правила")
        Surface(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = Color.White) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Время уведомлений", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = {
                    val parts = reminderTime.split(":"); val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9; val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    TimePickerDialog(context, { _, h, m -> vm.setReminderTime(String.format(Locale.US, "%02d:%02d", h, m)) }, hour, minute, true).show()
                }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Schedule, null); Spacer(Modifier.width(8.dp)); Text("Напоминать в $reminderTime") }
                Divider(color = Cream)
                Text("Напоминания по растениям", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Включайте или отключайте уведомления отдельно для каждого растения.", color = Muted, fontSize = 12.sp)
                plants.forEach { p -> Row(verticalAlignment = Alignment.CenterVertically) { PlantPhoto(p, 42); Spacer(Modifier.width(10.dp)); Text(p.name, Modifier.weight(1f), fontWeight = FontWeight.Medium); Switch(checked = p.remindersEnabled, onCheckedChange = { vm.setRemindersEnabled(p, it) }) } }
                if (plants.isEmpty()) Text("Добавьте растение, чтобы настроить его напоминания.", color = Muted, fontSize = 12.sp)
            }
        }
        Surface(Modifier.padding(18.dp).fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = Color(0xFFE7EDE2)) { Column(Modifier.padding(18.dp)) { Text("Cashmere Botanica", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Olive); Text("Версия 1.8.0 · Ваш уютный помощник по уходу за растениями.", color = Muted, fontSize = 12.sp) } }
    }
}

@Composable private fun EmptyState(title: String, subtitle: String, action: String?, onAction: (() -> Unit)?) {
    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🪴", fontSize = 46.sp); Spacer(Modifier.height(10.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(subtitle, color = Muted, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 5.dp))
        if (action != null && onAction != null) { Spacer(Modifier.height(12.dp)); Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = Olive), shape = RoundedCornerShape(14.dp)) { Text(action) } }
    }
}
