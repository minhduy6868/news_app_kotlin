package com.gk.news_pro.data.service

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body requestBody: RequestBody
    ): Response<GeminiResponse>
}

// Data class để ánh xạ phản hồi JSON
data class GeminiResponse(
    val candidates: List<Candidate>?,
    val error: GeminiError? = null
)

data class Candidate(
    val content: Content,
    val finishReason: String? = null
)

data class Content(
    val parts: List<Part>,
    val role: String? = null
)

data class Part(
    val text: String
)

data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)