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
    override suspend fun sendMessage(
        message: String,
        history: List<ChatMessage>,
        memories: List<String>,
    ): Result<String> {
        try {
            val p = prefs.prefsFlow.first()
            val effectiveUrl = p.backendUrl.ifBlank { BuildConfig.BACKEND_BASE_URL }

            if (effectiveUrl.contains("your-backend.example.com")) {
                return Result.failure(
                    Exception("No AI backend configured yet. Go to Settings -> AI Provider and enter your deployed backend URL and auth token (see jarvis-backend/README.md to deploy one)."),
                )
            }
            if (p.backendAuthToken.isBlank()) {
                return Result.failure(
                    Exception("Backend URL is set, but no auth token is configured. Go to Settings -> AI Provider and enter the APP_SHARED_SECRET you set on your backend."),
                )
            }

            val api = NetworkModule.apiFor(p.backendUrl)
            val dto = ChatRequestDto(
                message = message,
                history = history.takeLast(10).map { (if (it.isUser) "user: " else "ai: ") + it.text },
                language = com.jarvis.assistant.util.LanguageManager.backendLanguageTag(p.language),
                memories = if (p.memoryEnabled) memories else emptyList(),
            )
            val response = api.chat("Bearer ${p.backendAuthToken}", dto)
            return Result.success(response.reply)
        } catch (e: SocketTimeoutException) {
            return Result.failure(Exception("AI service timed out. Your backend may be asleep (free-tier hosts do this) - try again in a few seconds."))
        } catch (e: retrofit2.HttpException) {
            val detail = when (e.code()) {
                401 -> "Backend rejected the request - your auth token in Settings doesn't match the backend's APP_SHARED_SECRET."
                404 -> "Backend URL reached, but /chat endpoint wasn't found - check the URL in Settings has no typo."
                else -> "Backend returned an error (HTTP ${e.code()})."
            }
            return Result.failure(Exception(detail))
        } catch (e: java.net.UnknownHostException) {
            return Result.failure(Exception("Can't reach that backend URL - check it's correct and your backend is actually deployed and running."))
        } catch (e: IOException) {
            return Result.failure(Exception("AI service unavailable - check your internet connection."))
        } catch (e: Exception) {
            return Result.failure(Exception(e.message ?: "AI service unavailable"))
        }
    }
}
