package com.example.data.api

import com.squareup.moshi.JsonClass
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val stream: Boolean = false,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    val role: String,
    val content: List<OpenRouterContentPart> 
)

@JsonClass(generateAdapter = true)
data class OpenRouterContentPart(
    val type: String,
    val text: String? = null,
    val image_url: OpenRouterImageUrl? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterImageUrl(
    val url: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>? = null,
    val error: OpenRouterError? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    val delta: OpenRouterMessageContent? = null,
    val message: OpenRouterMessageContent? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessageContent(
    val role: String? = null,
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterError(
    val message: String? = null
)

interface OpenRouterApiService {
    @POST("api/v1/chat/completions")
    suspend fun generateContent(
        @Body request: OpenRouterRequest
    ): OpenRouterResponse

    @POST("api/v1/chat/completions")
    @Streaming
    suspend fun generateContentStream(
        @Body request: OpenRouterRequest
    ): ResponseBody
}

object OpenRouterRetrofitClient {
    private const val BASE_URL = "https://openrouter.ai/"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        val requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer $apiKey")
            .header("HTTP-Referer", "https://sparkex.ai") // From prompt
            .header("X-Title", "Sparkex AI") // From prompt
            
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: OpenRouterApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(OpenRouterApiService::class.java)
    }
}
