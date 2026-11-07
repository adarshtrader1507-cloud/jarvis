package com.jarvis.assistant.data.local.repository

import com.jarvis.assistant.data.local.dao.NoteDao
import com.jarvis.assistant.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun observeAll(): Flow<List<NoteEntity>> = dao.observeAll()
    suspend fun save(note: NoteEntity): Long = if (note.id == 0L) dao.insert(note) else { dao.update(note); note.id }
    suspend fun delete(note: NoteEntity) = dao.delete(note)
}
