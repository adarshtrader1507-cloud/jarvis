package com.jarvis.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.data.local.repository.ConversationRepository
import com.jarvis.assistant.data.local.repository.MemoryRepository
import com.jarvis.assistant.data.prefs.JarvisPrefs
import com.jarvis.assistant.data.prefs.PreferencesManager
import com.jarvis.assistant.domain.model.AiState
import com.jarvis.assistant.domain.model.ConnectivityState
import com.jarvis.assistant.domain.repository.ChatRepository
import com.jarvis.assistant.util.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class JarvisViewModel(
    private val repository: ChatRepository,
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    private val preferencesManager: PreferencesManager,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _aiState = MutableStateFlow<AiState>(AiState.Idle)
    val aiState: StateFlow<AiState> = _aiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    val prefs: StateFlow<JarvisPrefs> = preferencesManager.prefsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, JarvisPrefs())

    // Real connectivity + the user's "force offline" setting combine into one truth.
    val connectivity: StateFlow<ConnectivityState> = combine(
        connectivityObserver.observe(), prefs,
    ) { online, p ->
        if (p.forceOfflineMode) ConnectivityState.OFFLINE
        else if (online) ConnectivityState.ONLINE else ConnectivityState.OFFLINE
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ConnectivityState.OFFLINE)

    init {
        viewModelScope.launch {
            conversationRepository.observeAll().collect { entities ->
                _messages.value = entities.map { ChatMessage(it.text, it.isUser) }
            }
        }
    }

    fun onMicPressed() {
        _aiState.value = AiState.Listening
    }

    fun onUserUtterance(text: String) {
        if (text.isBlank()) {
            _aiState.value = AiState.Idle
            return
        }
        viewModelScope.launch { conversationRepository.save(text, isUser = true) }

        if (connectivity.value == ConnectivityState.OFFLINE) {
            _aiState.value = AiState.Error("You're offline — cloud AI chat isn't available. Try Smart Tools instead.")
            return
        }

        _aiState.value = AiState.Thinking
        viewModelScope.launch {
            val history = conversationRepository.recentForContext()
                .map { ChatMessage(it.text, it.isUser) }
            val memories = memoryRepository.observeAll().first().map { "${it.key}: ${it.value}" }
            val result = repository.sendMessage(text, history = history, memories = memories)
            result.onSuccess { reply ->
                conversationRepository.save(reply, isUser = false)
                _aiState.value = AiState.Speaking(amplitude = 0f)
            }.onFailure { err ->
                _aiState.value = AiState.Error(err.message ?: "AI service unavailable")
            }
        }
    }

    fun onSpeechAmplitude(amplitude: Float) {
        _aiState.value = AiState.Speaking(amplitude.coerceIn(0f, 1f))
    }

    fun onSpeechFinished() {
        _aiState.value = AiState.Idle
    }

    fun onError(message: String) {
        _aiState.value = AiState.Error(message)
    }

    fun clearConversation() {
        viewModelScope.launch { conversationRepository.clear() }
    }
}
