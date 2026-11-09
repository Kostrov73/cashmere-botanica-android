package com.greenhome.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val photoUri: String? = null,
    val lastWatered: String? = null,
    val wateringEveryDays: Int = 7,
    val fertilizingEveryDays: Int = 30,
    val fertilizerType: String = "Универсальное",
    val remindersEnabled: Boolean = true
)

@Entity(tableName = "care_events", foreignKeys = [ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE)], indices = [Index("plantId"), Index("date")])
data class CareEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val date: String,
    val type: String,
    val completed: Boolean = false,
    val fertilizer: String = "",
    val dosage: String = "",
    val note: String = "",
    val completedAt: String? = null
)

@Dao interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY name COLLATE NOCASE") fun observeAll(): Flow<List<Plant>>
    @Query("SELECT * FROM plants WHERE id = :id") suspend fun get(id: Long): Plant?
    @Insert suspend fun insert(plant: Plant): Long
    @Update suspend fun update(plant: Plant)
    @Delete suspend fun delete(plant: Plant)
}

@Dao interface EventDao {
    @Query("SELECT * FROM care_events ORDER BY COALESCE(completedAt, date) DESC, id DESC") fun observeAll(): Flow<List<CareEvent>>
    @Query("SELECT * FROM care_events WHERE date = :date ORDER BY id") fun observeDate(date: String): Flow<List<CareEvent>>
    @Query("SELECT * FROM care_events WHERE plantId = :plantId ORDER BY COALESCE(completedAt, date) DESC, id DESC") fun observePlant(plantId: Long): Flow<List<CareEvent>>
    @Insert suspend fun insert(event: CareEvent): Long
    @Update suspend fun update(event: CareEvent)
    @Delete suspend fun delete(event: CareEvent)
    @Query("SELECT COUNT(*) FROM care_events WHERE type = :type AND completed = 1 AND substr(COALESCE(completedAt, date),1,7) = :month") suspend fun countCompleted(type: String, month: String): Int
    @Query("SELECT * FROM care_events") suspend fun getAll(): List<CareEvent>
    @Query("SELECT * FROM care_events WHERE id = :id") suspend fun get(id: Long): CareEvent?
    @Query("SELECT * FROM care_events WHERE plantId = :plantId AND type = :type AND completed = 0 AND date >= :afterDate ORDER BY date ASC LIMIT 1") suspend fun findNextPending(plantId: Long, type: String, afterDate: String): CareEvent?
    @Query("SELECT MAX(COALESCE(completedAt, date)) FROM care_events WHERE plantId = :plantId AND type = 'Полив' AND completed = 1") suspend fun latestCompletedWater(plantId: Long): String?
}

@Database(entities = [Plant::class, CareEvent::class], version = 2, exportSchema = true)
abstract class GreenDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun eventDao(): EventDao
}
