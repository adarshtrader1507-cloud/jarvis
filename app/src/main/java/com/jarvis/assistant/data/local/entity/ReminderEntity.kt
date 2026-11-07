package com.jarvis.assistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val triggerAtMillis: Long,
    val isActive: Boolean = true,
)
