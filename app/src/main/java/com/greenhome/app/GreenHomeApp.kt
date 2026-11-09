package com.greenhome.app

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.greenhome.app.data.GreenDatabase

class GreenHomeApp : Application() {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE care_events ADD COLUMN completedAt TEXT")
            // В старой версии date уже перезаписывалась фактической датой выполнения.
            // Переносим её в новое поле, сохраняя доступную историю.
            db.execSQL("UPDATE care_events SET completedAt = date WHERE completed = 1")
        }
    }
    lateinit var database: GreenDatabase; private set
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, GreenDatabase::class.java, "green-home.db")
            .addMigrations(migration1To2)
            .build()
    }
}
