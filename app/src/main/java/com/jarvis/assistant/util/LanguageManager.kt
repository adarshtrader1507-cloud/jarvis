package com.jarvis.assistant.util

import com.jarvis.assistant.data.prefs.AppLanguage
import java.util.Locale

/**
 * Phase 5: Hindi / English / Hinglish support.
 * SpeechRecognizer and TextToSpeech both key off BCP-47 locale tags. "Hinglish" has no
 * dedicated recognizer locale on-device, so we use India-English (en-IN) which Android's
 * speech services handle well for code-switched Hindi/English speech, and we tell the
 * backend which mode the user picked so the model replies in matching style.
 */
object LanguageManager {
    fun localeFor(language: AppLanguage): Locale = when (language) {
        AppLanguage.ENGLISH -> Locale.ENGLISH
        AppLanguage.HINDI -> Locale("hi", "IN")
        AppLanguage.HINGLISH -> Locale("en", "IN")
    }

    fun backendLanguageTag(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "en"
        AppLanguage.HINDI -> "hi"
        AppLanguage.HINGLISH -> "hinglish"
    }
}
