package com.greenhome.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.greenDataStore by preferencesDataStore(name = "green_home_settings")
class GreenPreferences(private val context: Context) {
    private val reminderTime = stringPreferencesKey("reminder_time")
    val time = context.greenDataStore.data.map { it[reminderTime] ?: "09:00" }
    suspend fun setTime(value: String) { context.greenDataStore.edit { it[reminderTime] = value } }
}
