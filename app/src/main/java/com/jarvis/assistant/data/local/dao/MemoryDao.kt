package com.jarvis.assistant.data.local.dao

import androidx.room.*
import com.jarvis.assistant.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(memory: MemoryEntity)

    @Query("SELECT * FROM memory ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Delete
    suspend fun delete(memory: MemoryEntity)

    @Query("DELETE FROM memory")
    suspend fun clearAll()
}
