package com.jarvis.assistant.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val REMINDER_CHANNEL_ID = "jarvis_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Jarvis Reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Reminders you set with Jarvis" }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
