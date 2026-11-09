package com.greenhome.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.greenhome.app.R

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "Время позаботиться о растении"
        val body = inputData.getString("body") ?: "Загляните в Cashmere Botanica"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel("plant_care", "Уход за растениями", NotificationManager.IMPORTANCE_DEFAULT))
        val notification = NotificationCompat.Builder(applicationContext, "plant_care")
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar).setContentTitle(title).setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).build()
        manager.notify(inputData.getLong("eventId", System.currentTimeMillis()).toInt(), notification)
        return Result.success()
    }
}
