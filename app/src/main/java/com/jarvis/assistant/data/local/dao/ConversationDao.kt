package com.jarvis.assistant.data.local.dao

import androidx.room.*
import com.jarvis.assistant.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert
    suspend fun insert(message: ConversationEntity)

    @Query("SELECT * FROM conversation ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversation ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<ConversationEntity>

    @Query("DELETE FROM conversation")
    suspend fun clearAll()
}
