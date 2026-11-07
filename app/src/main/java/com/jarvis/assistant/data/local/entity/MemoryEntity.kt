package com.jarvis.assistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Long-term memory — only written when the user approves saving something. */
@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val createdAt: Long = System.currentTimeMillis(),
)
