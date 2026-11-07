package com.jarvis.assistant.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jarvis.assistant.viewmodel.CalculatorViewModel
import com.jarvis.assistant.viewmodel.JarvisViewModel
import com.jarvis.assistant.viewmodel.MemoryViewModel
import com.jarvis.assistant.viewmodel.NotesViewModel
import com.jarvis.assistant.viewmodel.RemindersViewModel
import com.jarvis.assistant.viewmodel.SettingsViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        JarvisViewModel::class.java -> JarvisViewModel(
            container.chatRepository,
            container.conversationRepository,
            container.memoryRepository,
            container.preferencesManager,
            container.connectivityObserver,
        ) as T
        MemoryViewModel::class.java -> MemoryViewModel(container.memoryRepository) as T
        NotesViewModel::class.java -> NotesViewModel(container.noteRepository) as T
        RemindersViewModel::class.java -> RemindersViewModel(container.reminderRepository, container.alarmScheduler) as T
        SettingsViewModel::class.java -> SettingsViewModel(container.preferencesManager, container.memoryRepository, container.conversationRepository) as T
        CalculatorViewModel::class.java -> CalculatorViewModel() as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
