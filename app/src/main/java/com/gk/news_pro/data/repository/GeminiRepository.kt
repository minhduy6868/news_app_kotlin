package com.gk.news_pro.data.repository

import GeminiApiService
import com.gk.news_pro.page.utils.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import org.json.JSONObject
import org.json.JSONArray

class GeminiRepository {
    private val geminiApiService: GeminiApiService =
        RetrofitClient.geminiRetrofit.create(GeminiApiService::class.java)

    suspend fun generateContent(apiKey: String, prompt: String): Response<ResponseBody> {
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            requestJson.toString()
        )

        return geminiApiService.generateContent(apiKey, requestBody)
    }
}