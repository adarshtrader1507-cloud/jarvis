package com.jarvis.assistant.di

import android.content.Context
import com.jarvis.assistant.data.local.AppDatabase
import com.jarvis.assistant.data.local.repository.ConversationRepository
import com.jarvis.assistant.data.local.repository.MemoryRepository
import com.jarvis.assistant.data.local.repository.NoteRepository
import com.jarvis.assistant.data.local.repository.ReminderRepository
import com.jarvis.assistant.data.prefs.PreferencesManager
import com.jarvis.assistant.data.remote.ChatRepositoryImpl
import com.jarvis.assistant.util.AlarmScheduler
import com.jarvis.assistant.util.ConnectivityObserver

/**
 * Simple, explicit, no-magic manual DI container — no Hilt/Dagger needed for an app
 * this size, and it keeps every wiring decision visible in one file for the audit.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)

    val preferencesManager = PreferencesManager(appContext)
    val connectivityObserver = ConnectivityObserver(appContext)
    val alarmScheduler = AlarmScheduler(appContext)

    val conversationRepository = ConversationRepository(database.conversationDao())
    val memoryRepository = MemoryRepository(database.memoryDao())
    val noteRepository = NoteRepository(database.noteDao())
    val reminderRepository = ReminderRepository(database.reminderDao())

    val chatRepository = ChatRepositoryImpl(preferencesManager)
}
