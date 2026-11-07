package com.jarvis.assistant.data.local.repository

import com.jarvis.assistant.data.local.dao.MemoryDao
import com.jarvis.assistant.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val dao: MemoryDao) {
    fun observeAll(): Flow<List<MemoryEntity>> = dao.observeAll()
    suspend fun remember(key: String, value: String) = dao.upsert(MemoryEntity(key = key, value = value))
    suspend fun forget(memory: MemoryEntity) = dao.delete(memory)
    suspend fun forgetAll() = dao.clearAll()
}
