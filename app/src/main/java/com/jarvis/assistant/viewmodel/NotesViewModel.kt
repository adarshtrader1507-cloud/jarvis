package com.jarvis.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.data.local.entity.NoteEntity
import com.jarvis.assistant.data.local.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {
    val notes: StateFlow<List<NoteEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(title: String, content: String, existing: NoteEntity? = null) = viewModelScope.launch {
        repository.save(
            (existing ?: NoteEntity(title = title, content = content))
                .copy(title = title, content = content, updatedAt = System.currentTimeMillis()),
        )
    }

    fun delete(note: NoteEntity) = viewModelScope.launch { repository.delete(note) }
}
