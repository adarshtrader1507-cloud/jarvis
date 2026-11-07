package com.jarvis.assistant.data.local.repository

import com.jarvis.assistant.data.local.dao.ReminderDao
import com.jarvis.assistant.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    fun observeAll(): Flow<List<ReminderEntity>> = dao.observeAll()
    suspend fun save(reminder: ReminderEntity): Long =
        if (reminder.id == 0L) dao.insert(reminder) else { dao.update(reminder); reminder.id }
    suspend fun delete(reminder: ReminderEntity) = dao.delete(reminder)
    suspend fun getById(id: Long) = dao.getById(id)
}
