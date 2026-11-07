package com.jarvis.assistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jarvis.assistant.data.local.dao.ConversationDao
import com.jarvis.assistant.data.local.dao.MemoryDao
import com.jarvis.assistant.data.local.dao.NoteDao
import com.jarvis.assistant.data.local.dao.ReminderDao
import com.jarvis.assistant.data.local.entity.ConversationEntity
import com.jarvis.assistant.data.local.entity.MemoryEntity
import com.jarvis.assistant.data.local.entity.NoteEntity
import com.jarvis.assistant.data.local.entity.ReminderEntity

@Database(
    entities = [ConversationEntity::class, MemoryEntity::class, NoteEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun memoryDao(): MemoryDao
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jarvis.db",
                ).build().also { INSTANCE = it }
            }
    }
}
