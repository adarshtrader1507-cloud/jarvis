# JARVIS — Personal AI Assistant — Architecture

## 1. High-level approach
MVVM + Repository pattern, single-activity Jetpack Compose UI, Kotlin Coroutines/Flow for
async state, Room for local persistence, DataStore for prefs, Retrofit/Ktor for networking.
The AI brain itself is NEVER called directly from the phone with a provider API key — the
app talks only to **your own backend** (a small server you control), which holds the real
key and forwards chat requests. This satisfies the "never ship secrets in the APK" rule.

```
app/
 ui/            Compose screens + the AI-face animation engine (Canvas, no Lottie needed
                for the core face — keeps APK small and avoids asset licensing issues)
 viewmodel/     JarvisViewModel — single source of truth for AI state (Idle/Listening/
                Thinking/Speaking/Activating/Error), owns SpeechRecognizer + TextToSpeech
 data/local/    Room DB (ConversationDao, MemoryDao) + DataStore prefs
 data/remote/   Retrofit interface hitting YOUR backend, never a provider directly
 domain/        Plain Kotlin models + repository interfaces (keeps viewmodel UI-agnostic)
 di/            Manual or Hilt-style simple DI container
```

## 2. The face-state machine (Phase 3)
A single sealed class drives every animation — nothing plays "randomly":

```kotlin
sealed class AiState {
    object Idle : AiState()
    object Listening : AiState()
    object Thinking : AiState()
    data class Speaking(val amplitude: Float) : AiState()
    object Activating : AiState()
    data class Error(val message: String) : AiState()
}
```
`JarvisViewModel` exposes `val aiState: StateFlow<AiState>`. The Compose face composable
`collectAsState()`s it and switches its Canvas draw routine accordingly — listening rings,
thinking pulse, speaking waveform amplitude driven by actual TTS/STT callbacks, not timers.

## 3. Backend contract (Phase 4) — what you must stand up separately
A minimal server (Cloudflare Worker / small Node/FastAPI service) exposing:
`POST /chat  { message, history[] }  →  { reply }`
It holds the real provider key as a server-side secret. The app sends only the user's
message + short history over HTTPS with a request timeout and retry/backoff, and shows
"AI service unavailable" if the call fails or there's no network — it never fabricates
a response.

## 4. What I'm delivering now vs. what remains
**Now:** working skeleton — Gradle project, theme, the animated AI-face composable with all
six states wired to a ViewModel, Room entities for conversation/memory, a Retrofit client
stub pointed at your backend URL, the GitHub Actions build workflow.
**Needs you (or a follow-up session) to finish:** your actual backend endpoint URL/auth,
Play-Store-style signing keystore for a release build, and testing on real hardware (I
can't run an Android emulator or the Gradle build in this chat environment — no SDK/network
here — so you'll build the APK via Android Studio or the included GitHub Actions workflow).
