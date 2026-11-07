package com.jarvis.assistant.domain.model

/**
 * Single source of truth for what the AI face should be doing.
 * Every animation state in the UI is derived from this — nothing in the
 * face composable plays on its own timer independent of real app state.
 */
sealed class AiState {
    object Idle : AiState()
    object Listening : AiState()
    object Thinking : AiState()
    data class Speaking(val amplitude: Float) : AiState() // 0f..1f, driven by TTS progress
    object Activating : AiState()
    data class Error(val message: String) : AiState()
}

enum class ConnectivityState { ONLINE, OFFLINE }
