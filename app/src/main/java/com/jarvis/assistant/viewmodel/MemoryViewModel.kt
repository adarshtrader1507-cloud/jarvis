package com.jarvis.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.data.local.entity.MemoryEntity
import com.jarvis.assistant.data.local.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Phase 8: the user stays in control of what Jarvis remembers. */
class MemoryViewModel(private val repository: MemoryRepository) : ViewModel() {
    val memories: StateFlow<List<MemoryEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remember(key: String, value: String) = viewModelScope.launch { repository.remember(key, value) }
    fun forget(memory: MemoryEntity) = viewModelScope.launch { repository.forget(memory) }
    fun forgetAll() = viewModelScope.launch { repository.forgetAll() }
}
