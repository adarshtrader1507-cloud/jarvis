package com.jarvis.assistant

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.jarvis.assistant.di.AppContainer
import com.jarvis.assistant.di.ViewModelFactory
import com.jarvis.assistant.domain.model.AiState
import com.jarvis.assistant.ui.navigation.JarvisNavGraph
import com.jarvis.assistant.util.LanguageManager
import com.jarvis.assistant.util.PermissionsUtil
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID

/**
 * Integration root. Owns the two system services Compose can't own directly
 * (SpeechRecognizer, TextToSpeech) and bridges them to JarvisViewModel's AiState,
 * so the face animation, the backend call and real device speech are one connected loop:
 *
 *   mic tap -> requestMic -> SpeechRecognizer -> viewModel.onUserUtterance(text)
 *     -> ChatRepositoryImpl (your backend) -> viewModel sets Speaking
 *     -> this Activity observes Speaking -> TextToSpeech.speak(reply)
 *     -> UtteranceProgressListener.onDone -> viewModel.onSpeechFinished() -> Idle
 */
class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer
    private lateinit var factory: ViewModelFactory
    private val viewModel by lazy {
        androidx.lifecycle.ViewModelProvider(this, factory)[com.jarvis.assistant.viewmodel.JarvisViewModel::class.java]
    }

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var lastSpokenMessageCount = 0

    private val micPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening() else viewModel.onError("Microphone permission denied")
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op: reminders degrade gracefully without it */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        container = AppContainer(applicationContext)
        factory = ViewModelFactory(container)

        requestNotificationPermissionIfNeeded()
        setupSpeechRecognizer()
        setupTextToSpeech()
        observeAiStateForSpeech()

        setContent {
            MaterialTheme {
                JarvisNavGraph(
                    container = container,
                    onMicClick = { requestMicAndListen() },
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionsUtil.hasNotificationPermission(this)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestMicAndListen() {
        if (PermissionsUtil.hasMicPermission(this)) startListening()
        else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            viewModel.onError("Speech recognition isn't available on this device")
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { viewModel.onMicPressed() }
            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                viewModel.onUserUtterance(text)
            }
            override fun onError(error: Int) = viewModel.onError(speechErrorMessage(error))
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun speechErrorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that — try again"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during recognition"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
        else -> "Speech recognition error ($error)"
    }

    private fun startListening() {
        val language = viewModel.prefs.value.language
        val locale = LanguageManager.localeFor(language)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
        }
        speechRecognizer.startListening(intent)
    }

    private fun setupTextToSpeech() {
        tts = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) = viewModel.onSpeechFinished()
                    override fun onError(utteranceId: String?) = viewModel.onError("Text-to-speech error")
                })
            }
        }
    }

    /** The missing link: when the ViewModel transitions to Speaking after a successful
     *  backend reply, actually say it — applying the user's chosen language + speech rate. */
    private fun observeAiStateForSpeech() {
        viewModel.aiState.onEach { state ->
            if (state is AiState.Speaking) {
                val lastReply = viewModel.messages.value.lastOrNull { !it.isUser }?.text
                if (ttsReady && !lastReply.isNullOrBlank()) {
                    tts.language = LanguageManager.localeFor(viewModel.prefs.value.language)
                    tts.setSpeechRate(viewModel.prefs.value.speechRate)
                    tts.speak(lastReply, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
                } else {
                    viewModel.onSpeechFinished()
                }
            }
        }.launchIn(lifecycleScope)
    }

    /** Stop button (Phase 5: "Stop speaking") — exposed for future UI wiring. */
    fun stopSpeaking() {
        if (ttsReady) tts.stop()
        viewModel.onSpeechFinished()
    }

    override fun onDestroy() {
        if (::speechRecognizer.isInitialized) speechRecognizer.destroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
