package com.gk.news_pro.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gk.news_pro.data.api.HeyGenApiService
import com.gk.news_pro.data.api.HeyGenVideoRequest
import com.gk.news_pro.data.api.VideoInput
import com.gk.news_pro.data.api.CharacterSettings
import com.gk.news_pro.page.utils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import org.json.JSONObject
import com.google.gson.Gson

class HeyGenRepository(private val context: Context) {
    private val retrofit by lazy {
        RetrofitClient.heyGenRetrofit
    }

    private val apiService: HeyGenApiService = retrofit.create(HeyGenApiService::class.java)
    private val apiKey = "OTEyNjgxMWRmOTUzNGZhZTlkMjQyODk4ZGJjNTFiYjctMTc0Njg5NDk5Nw=="
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("HeyGenPrefs", Context.MODE_PRIVATE)

    suspend fun generateVideo(script: String, avatarId: String = "Judy_Nurse_Side_public"): Result {
        return withContext(Dispatchers.IO) {
            try {
                if (script.isBlank()) {
                    Log.e("HeyGenRepository", "Script is empty or blank")
                    return@withContext Result.Error("Lỗi khi tạo video: Kịch bản không được để trống")
                }

                val cleanedScript = script
                    .replace(Regex("[*`#\\n\\t\\*\\-]+"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                val trimmedScript = if (cleanedScript.length > 200) {
                    cleanedScript.take(197) + "..."
                } else {
                    cleanedScript
                }
                Log.d("HeyGenRepository", "Cleaned and trimmed script (length: ${trimmedScript.length}): $trimmedScript")

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
                                text = trimmedScript
                            ),
                            background = CharacterSettings.ColorBackground(
                                value = "#FFFFFF"
                            )
                        )
                    ),
                    dimension = CharacterSettings.Dimension(
                        width = 1280,
                        height = 720
                    )
                )

                val requestJson = Gson().toJson(request)
                Log.d("HeyGenRepository", "Request JSON: $requestJson")

                val createResponse = apiService.createVideo(apiKey, request)
                Log.d("HeyGenRepository", "Create Response: code=${createResponse.code}, message=${createResponse.message}, video_id=${createResponse.data?.videoId}, error=${createResponse.error?.message}")

                // Check for success or partial success
                if (createResponse.data?.videoId == null) {
                    val errorMsg = createResponse.error?.message ?: createResponse.message ?: "Unknown error (no video_id)"
                    Log.e("HeyGenRepository", "Failed to create video: $errorMsg")
                    return@withContext Result.Error("Lỗi khi tạo video: $errorMsg")
                }

                // Log full response for debugging
                val responseJson = Gson().toJson(createResponse)
                Log.d("HeyGenRepository", "Full Create Response: $responseJson")

                val videoId = createResponse.data.videoId!!
                saveVideoId(videoId)

                Result.Success(videoId, trimmedScript)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: "No error body"
                Log.e("HeyGenRepository", "HTTP ${e.code()}: $errorBody")
                val errorMessage = try {
                    val json = JSONObject(errorBody)
                    json.getJSONObject("error").getString("message")
                } catch (jsonException: Exception) {
                    errorBody
                }
                Result.Error("Lỗi khi tạo video: HTTP ${e.code()} - $errorMessage")
            } catch (e: Exception) {
                Log.e("HeyGenRepository", "Error: ${e.localizedMessage ?: e.toString()}")
                Result.Error("Lỗi khi tạo video: ${e.localizedMessage ?: e.toString()}")
            }
        }
    }

    suspend fun checkVideoStatus(videoId: String): String {
        try {
            val statusResponse = apiService.getVideoStatus(apiKey, videoId)
            Log.d("HeyGenRepository", "Status Response: code=${statusResponse.code}, status=${statusResponse.data?.status}, video_url=${statusResponse.data?.videoUrl}, error=${statusResponse.error?.message}")

//            if (statusResponse.code != 100 ) {
//                val errorMsg = statusResponse.error?.message ?: statusResponse.message ?: "Unknown error"
//                return "Lỗi khi lấy trạng thái video: $errorMsg"
//            }

            return when (statusResponse.data?.status) {
                "completed" -> statusResponse.data.videoUrl ?: "Lỗi: Không tìm thấy URL video"
                "failed" -> "Lỗi: Video is too long (> 3600.0s). Please upgrade your plan to generate longer videos"
                "pending", "processing" -> "Đang xử lý"
                else -> "Lỗi: Trạng thái video không xác định"
            }
        } catch (e: Exception) {
            Log.e("HeyGenRepository", "Error polling status: ${e.localizedMessage}")
            return "Lỗi khi lấy trạng thái video: ${e.localizedMessage}"
        }
    }

    private fun saveVideoId(videoId: String) {
        with(sharedPreferences.edit()) {
            putString("video_id", videoId)
            putLong("creation_time", System.currentTimeMillis())
            apply()
        }
        Log.d("HeyGenRepository", "Saved video_id: $videoId")
    }

    fun getSavedVideoId(): String? {
        return sharedPreferences.getString("video_id", null)
    }

    fun getCreationTime(): Long {
        return sharedPreferences.getLong("creation_time", 0L)
    }

    fun clearVideoId() {
        with(sharedPreferences.edit()) {
            remove("video_id")
            remove("creation_time")
            apply()
        }
        Log.d("HeyGenRepository", "Cleared video_id")
    }

    sealed class Result {
        data class Success(val videoId: String, val script: String) : Result()
        data class Error(val message: String) : Result()
    }
}