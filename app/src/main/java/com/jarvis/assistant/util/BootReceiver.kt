package com.jarvis.assistant.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jarvis.assistant.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmManager alarms are wiped on reboot — without this, reminders silently stop working
 * after the phone restarts. Re-reads active, still-future reminders from Room and
 * re-schedules each one for real.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(appContext)
                val scheduler = AlarmScheduler(appContext)
                val pending = db.reminderDao().getActiveFuture(System.currentTimeMillis())
                pending.forEach { scheduler.schedule(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
