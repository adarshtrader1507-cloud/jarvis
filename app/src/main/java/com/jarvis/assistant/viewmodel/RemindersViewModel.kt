package com.jarvis.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.data.local.entity.ReminderEntity
import com.jarvis.assistant.data.local.repository.ReminderRepository
import com.jarvis.assistant.util.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
) : ViewModel() {
    val reminders: StateFlow<List<ReminderEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun create(label: String, triggerAtMillis: Long) = viewModelScope.launch {
        val id = repository.save(ReminderEntity(label = label, triggerAtMillis = triggerAtMillis))
        repository.getById(id)?.let { alarmScheduler.schedule(it) }
    }

    fun cancel(reminder: ReminderEntity) = viewModelScope.launch {
        alarmScheduler.cancel(reminder)
        repository.delete(reminder)
    }
}
