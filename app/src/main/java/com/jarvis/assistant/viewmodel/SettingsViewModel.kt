package com.jarvis.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.data.local.repository.ConversationRepository
import com.jarvis.assistant.data.local.repository.MemoryRepository
import com.jarvis.assistant.data.prefs.AppLanguage
import com.jarvis.assistant.data.prefs.ConnectivityMode
import com.jarvis.assistant.data.prefs.JarvisPrefs
import com.jarvis.assistant.data.prefs.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefsManager: PreferencesManager,
    private val memoryRepository: MemoryRepository,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {

    val prefs: StateFlow<JarvisPrefs> = prefsManager.prefsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JarvisPrefs())

    fun setLanguage(language: AppLanguage) = viewModelScope.launch { prefsManager.setLanguage(language) }
    fun setSpeechRate(rate: Float) = viewModelScope.launch { prefsManager.setSpeechRate(rate) }
    fun setAnimationIntensity(v: Float) = viewModelScope.launch { prefsManager.setAnimationIntensity(v) }
    fun setConnectivityMode(mode: ConnectivityMode) = viewModelScope.launch { prefsManager.setConnectivityMode(mode) }
    fun setMemoryEnabled(v: Boolean) = viewModelScope.launch { prefsManager.setMemoryEnabled(v) }
    fun setBackendUrl(url: String) = viewModelScope.launch { prefsManager.setBackendUrl(url) }
    fun setBackendAuthToken(token: String) = viewModelScope.launch { prefsManager.setBackendAuthToken(token) }

    fun clearAllData() = viewModelScope.launch {
        memoryRepository.forgetAll()
        conversationRepository.clear()
    }
}
