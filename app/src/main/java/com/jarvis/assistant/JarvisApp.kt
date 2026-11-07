package com.jarvis.assistant

import android.app.Application
import com.jarvis.assistant.util.NotificationHelper

class JarvisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this) // Phase 7 reminders need this channel to exist first
    }
}
