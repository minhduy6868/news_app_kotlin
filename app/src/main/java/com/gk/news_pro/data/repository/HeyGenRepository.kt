package com.gk.news_pro.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gk.news_pro.data.api.HeyGenApiService
import com.gk.news_pro.data.api.HeyGenVideoRequest
import com.gk.news_pro.data.api.VideoInput
import com.gk.news_pro.data.api.CharacterSettings
import com.gk.news_pro.page.utils.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class HeyGenRepository(private val context: Context) {
    private val retrofit by lazy {
        RetrofitClient.heyGenRetrofit
    }

    private val apiService: HeyGenApiService = retrofit.create(HeyGenApiService::class.java)
    private val apiKey = "" //""NjU4YzhhOWMzZGRhNDgzMmIwOTkwYTA3ODMzMjU0ZmItMTc0NzA2NjcwNw=="
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("HeyGenPrefs", Context.MODE_PRIVATE)

    suspend fun generateVideo(script: String, avatarId: String = "Jin_expressive_2024112501"): Result {
        return withContext(Dispatchers.IO) {
            try {
                if (script.isBlank()) {
                    Log.e("HeyGenRepository", "Script is empty or blank")
                    return@withContext Result.Error("Lỗi khi tạo video: Kịch bản không được để trống")
                }

                Log.d("HeyGenRepository", "Original script (length: ${script.length}): $script")

                // Clean script while preserving square brackets for validation
                val cleanedScript = cleanScript(script)
                Log.d("HeyGenRepository", "Cleaned script (length: ${cleanedScript.length}): $cleanedScript")

                // Validate script
                if (!isScriptValid(cleanedScript)) {
                    Log.e("HeyGenRepository", "Script is incomplete, missing news or closing statement")
                    return@withContext Result.Error("Lỗi: Kịch bản không đầy đủ")
                }

                // Estimate spoken duration (assuming ~2.5 chars/sec for Vietnamese)
                val estimatedSeconds = cleanedScript.length / 2.5
                Log.d("HeyGenRepository", "Estimated spoken duration: $estimatedSeconds seconds")
                if (estimatedSeconds > 40) {
                    Log.w("HeyGenRepository", "Script may exceed 40-second limit")
                }

                // Remove square brackets for API text-to-speech
                val apiScript = cleanedScript
                    .replace("[Lời chào]", "")
                    .replace("[Tóm tắt các tin tức]", "")
                    .replace("[Lời kết]", "")
                    .trim()
                Log.d("HeyGenRepository", "API script (length: ${apiScript.length}): $apiScript")

                // Adjust max length for safety
                val maxScriptLength = 1000 // Strict limit to ensure <40s
                val finalScript = if (apiScript.length > maxScriptLength) {
                    val lastIndex = apiScript.substring(0, maxScriptLength).lastIndexOf(".")
                    if (lastIndex != -1) apiScript.substring(0, lastIndex + 1) else apiScript.take(maxScriptLength)
                } else {
                    apiScript
                }
                Log.d("HeyGenRepository", "Final script (length: ${finalScript.length}): $finalScript")

                val request = HeyGenVideoRequest(
                    caption = false,
                    title = "Quick News",
                    videoInputs = listOf(
                        VideoInput(
                            character = CharacterSettings.AvatarSettings(
                                avatarId = avatarId,
                                scale = 1.0f,
                                avatarStyle = "normal",
                                matting = true
                            ),
                            voice = CharacterSettings.TextVoiceSettings(
                                type = "text",
                                voiceId = "4286c03d11f44af093e379fc7e2cafa6",
                                text = finalScript
                            ),
                            background = CharacterSettings.ColorBackground(
                                value = "#FFFFFF"
                            )
                        )
                    ),
                    dimension = CharacterSettings.Dimension(
                        width = 854,
                        height = 480
                    )
                )

                val requestJson = Gson().toJson(request)
                Log.d("HeyGenRepository", "Request JSON: $requestJson")

                val createResponse = apiService.createVideo(apiKey, request)
                try {
                   // createResponse.errorBody()?.close() // Close error body if present
                    Log.d("HeyGenRepository", "Create Response: code=${createResponse.code}, video_id=${createResponse.data?.videoId}, error=${createResponse.error?.message}")

                    if (createResponse.data?.videoId == null) {
                        val errorMsg = createResponse.error?.message ?: createResponse.message ?: "Unknown error"
                        Log.e("HeyGenRepository", "Failed to create video: $errorMsg")
                        return@withContext Result.Error("Lỗi khi tạo video: $errorMsg")
                    }

                    val videoId = createResponse.data.videoId!!
                    saveVideoId(videoId)
                    Result.Success(videoId, finalScript)
                } finally {
                   // createResponse.errorBody()?.close() // Ensure closure in all cases
                }
            } catch (e: HttpException) {
                e.response()?.errorBody()?.close()
                Log.e("HeyGenRepository", "HTTP ${e.code()}: ${e.message()}")
                Result.Error("Lỗi khi tạo video: HTTP ${e.code()} - ${e.message()}")
            } catch (e: IOException) {
                Log.e("HeyGenRepository", "IO Error: ${e.message}")
                Result.Error("Lỗi khi tạo video: Kết nối mạng không ổn định")
            } catch (e: Exception) {
                Log.e("HeyGenRepository", "Error: ${e.message}")
                Result.Error("Lỗi khi tạo video: ${e.message}")
            }
        }
    }

    private fun cleanScript(script: String): String {
        return script
            .replace(Regex("[\\*\\^\\#\\`\\~\\=\\-\\_\\+\\|<>]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isScriptValid(script: String): Boolean {
        val lowerScript = script.lowercase()
        return lowerScript.contains("tóm tắt các tin tức") && lowerScript.contains("lời kết")
    }

    suspend fun checkVideoStatus(videoId: String): String {
        try {
            val statusResponse = apiService.getVideoStatus(apiKey, videoId)
            try {
               // statusResponse.errorBody()?.close()
                Log.d("HeyGenRepository", "Status Response: status=${statusResponse.data?.status}, video_url=${statusResponse.data?.videoUrl}")
                return when (statusResponse.data?.status) {
                    "completed" -> statusResponse.data.videoUrl ?: "Lỗi: Không tìm thấy URL video"
                    "failed" -> "Lỗi: Video thất bại"
                    "pending", "processing" -> "Đang xử lý"
                    else -> "Lỗi: Trạng thái không xác định"
                }
            } finally {
               // statusResponse.errorBody()?.close()
            }
        } catch (e: Exception) {
            Log.e("HeyGenRepository", "Error polling status: ${e.message}")
            return "Lỗi khi lấy trạng thái video: ${e.message}"
        }
    }

    private fun saveVideoId(videoId: String) {
        sharedPreferences.edit()
            .putString("video_id", videoId)
            .putLong("creation_time", System.currentTimeMillis())
            .apply()
    }

    fun getSavedVideoId(): String? = sharedPreferences.getString("video_id", null)
    fun getCreationTime(): Long = sharedPreferences.getLong("creation_time", 0L)
    fun clearVideoId() = sharedPreferences.edit().clear().apply()

    sealed class Result {
        data class Success(val videoId: String, val script: String) : Result()
        data class Error(val message: String) : Result()
    }
}