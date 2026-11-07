package com.jarvis.assistant.data.local.repository

import com.jarvis.assistant.data.local.dao.ConversationDao
import com.jarvis.assistant.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

class ConversationRepository(private val dao: ConversationDao) {
    fun observeAll(): Flow<List<ConversationEntity>> = dao.observeAll()
    suspend fun save(text: String, isUser: Boolean) = dao.insert(ConversationEntity(text = text, isUser = isUser))
    suspend fun recentForContext(limit: Int = 10) = dao.recent(limit).reversed()
    suspend fun clear() = dao.clearAll()
}
