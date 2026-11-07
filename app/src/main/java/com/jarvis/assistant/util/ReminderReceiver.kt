package com.jarvis.assistant.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jarvis.assistant.MainActivity
import android.app.PendingIntent

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_LABEL = "extra_label"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra(EXTRA_LABEL) ?: "Reminder"
        val id = intent.getLongExtra(EXTRA_ID, 0L)

        if (!PermissionsUtil.hasNotificationPermission(context)) return // never post without permission

        val openIntent = PendingIntent.getActivity(
            context, id.toInt(), Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationHelper.REMINDER_CHANNEL_ID)
            .setContentTitle("JARVIS Reminder")
            .setContentText(label)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id.toInt(), notification)
    }
}
