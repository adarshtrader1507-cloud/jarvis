package com.jarvis.assistant.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jarvis_prefs")

enum class AppLanguage(val tag: String, val label: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "Hindi"),
    HINGLISH("en-IN", "Hinglish"),
}

data class JarvisPrefs(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val speechRate: Float = 1.0f,
    val animationIntensity: Float = 1.0f,
    val forceOfflineMode: Boolean = false,
    val memoryEnabled: Boolean = true,
    val backendUrl: String = "",
    val backendAuthToken: String = "",
)

/**
 * Single DataStore-backed source for every Settings-dashboard toggle (Phase 9).
 * Nothing here is decorative — every value read here is actually consumed:
 * language -> SpeechRecognizer/TTS locale, speechRate -> TextToSpeech.setSpeechRate,
 * animationIntensity -> AiFace draw scale, forceOfflineMode -> ConnectivityObserver override,
 * memoryEnabled -> gates MemoryRepository writes, backendUrl -> overrides BuildConfig default.
 */
class PreferencesManager(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val ANIM_INTENSITY = floatPreferencesKey("anim_intensity")
        val FORCE_OFFLINE = booleanPreferencesKey("force_offline")
        val MEMORY_ENABLED = booleanPreferencesKey("memory_enabled")
        val BACKEND_URL = stringPreferencesKey("backend_url")
        val BACKEND_AUTH_TOKEN = stringPreferencesKey("backend_auth_token")
    }

    val prefsFlow: Flow<JarvisPrefs> = context.dataStore.data.map { p ->
        JarvisPrefs(
            language = AppLanguage.entries.find { it.tag == p[Keys.LANGUAGE] } ?: AppLanguage.ENGLISH,
            speechRate = p[Keys.SPEECH_RATE] ?: 1.0f,
            animationIntensity = p[Keys.ANIM_INTENSITY] ?: 1.0f,
            forceOfflineMode = p[Keys.FORCE_OFFLINE] ?: false,
            memoryEnabled = p[Keys.MEMORY_ENABLED] ?: true,
            backendUrl = p[Keys.BACKEND_URL] ?: "",
            backendAuthToken = p[Keys.BACKEND_AUTH_TOKEN] ?: "",
        )
    }

    suspend fun setLanguage(language: AppLanguage) = context.dataStore.edit { it[Keys.LANGUAGE] = language.tag }
    suspend fun setSpeechRate(rate: Float) = context.dataStore.edit { it[Keys.SPEECH_RATE] = rate }
    suspend fun setAnimationIntensity(v: Float) = context.dataStore.edit { it[Keys.ANIM_INTENSITY] = v }
    suspend fun setForceOffline(v: Boolean) = context.dataStore.edit { it[Keys.FORCE_OFFLINE] = v }
    suspend fun setMemoryEnabled(v: Boolean) = context.dataStore.edit { it[Keys.MEMORY_ENABLED] = v }
    suspend fun setBackendUrl(url: String) = context.dataStore.edit { it[Keys.BACKEND_URL] = url }
    suspend fun setBackendAuthToken(token: String) = context.dataStore.edit { it[Keys.BACKEND_AUTH_TOKEN] = token }
}
