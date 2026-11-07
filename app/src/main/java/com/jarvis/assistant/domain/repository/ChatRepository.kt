package com.jarvis.assistant.domain.repository

import com.jarvis.assistant.viewmodel.ChatMessage

interface ChatRepository {
    suspend fun sendMessage(message: String, history: List<ChatMessage>, memories: List<String> = emptyList()): Result<String>
}
