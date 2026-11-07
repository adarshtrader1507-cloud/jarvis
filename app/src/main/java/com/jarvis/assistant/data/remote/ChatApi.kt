package com.jarvis.assistant.data.remote

import com.jarvis.assistant.BuildConfig
import com.jarvis.assistant.data.prefs.PreferencesManager
import com.jarvis.assistant.domain.repository.ChatRepository
import com.jarvis.assistant.viewmodel.ChatMessage
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

data class ChatRequestDto(
    val message: String,
    val history: List<String>,
    val language: String,
    val memories: List<String> = emptyList(),
)
data class ChatResponseDto(val reply: String)

interface ChatApi {
    // Hits YOUR backend only. The backend holds the real provider key server-side —
    // the app never talks to the AI provider directly and never embeds a secret.
    // Authorization carries the app-level shared secret the backend's requireAppAuth expects
    // (see jarvis-backend/server.js) — NOT a provider API key.
    @POST("chat")
    suspend fun chat(
        @retrofit2.http.Header("Authorization") auth: String,
        @Body request: ChatRequestDto,
    ): ChatResponseDto

    @retrofit2.http.GET("health")
    suspend fun health(): okhttp3.ResponseBody
}

object NetworkModule {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    /** Settings dashboard lets the user override the backend URL (Phase 9); falls back
     *  to the build-time default so the app works out of the box. */
    fun apiFor(baseUrl: String): ChatApi {
        val url = baseUrl.ifBlank { BuildConfig.BACKEND_BASE_URL }
        val normalized = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApi::class.java)
    }
}

class ChatRepositoryImpl(private val prefs: PreferencesManager) : ChatRepository {
    override suspend fun sendMessage(message: String, history: List<ChatMessage>, memories: List<String>): Result<String> =
        try {
            val p = prefs.prefsFlow.first()
            val api = NetworkModule.apiFor(p.backendUrl)
            val dto = ChatRequestDto(
                message = message,
                history = history.takeLast(10).map { (if (it.isUser) "user: " else "ai: ") + it.text },
                language = com.jarvis.assistant.util.LanguageManager.backendLanguageTag(p.language),
                memories = if (p.memoryEnabled) memories else emptyList(),
            )
            Result.success(api.chat("Bearer ${p.backendAuthToken}", dto).reply)
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("AI service timed out. Please try again."))
        } catch (e: IOException) {
            Result.failure(Exception("AI service unavailable — check your connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "AI service unavailable"))
        }
}
