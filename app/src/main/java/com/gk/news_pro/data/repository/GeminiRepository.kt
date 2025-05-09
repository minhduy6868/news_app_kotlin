package com.gk.news_pro.data.repository

import com.gk.news_pro.data.service.GeminiApiService
import com.gk.news_pro.data.service.GeminiResponse
import com.gk.news_pro.page.utils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiRepository {
    private val geminiApiService: GeminiApiService =
        RetrofitClient.geminiRetrofit.create(GeminiApiService::class.java)

    private val apiKey = "AIzaSyCLbQa-NpxZKbgLzhaCp9ugcTVLvz1EDbM" // Đảm bảo key hợp lệ

    suspend fun generateContent(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
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

                val response = geminiApiService.generateContent(apiKey, requestBody)

                if (response.isSuccessful) {
                    response.body()?.let { geminiResponse ->
                        parseGeminiResponse(geminiResponse)
                    } ?: "Không có phản hồi từ API."
                } else {
                    response.errorBody()?.string()?.let { errorBody ->
                        "Lỗi: ${response.code()} - ${response.message()}, Chi tiết: $errorBody"
                    } ?: "Lỗi: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                "Lỗi: ${e.localizedMessage ?: e.toString()}"
            }
        }
    }

    private fun parseGeminiResponse(response: GeminiResponse): String {
        return try {
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: response.error?.message
                ?: "Không có phản hồi từ AI."
        } catch (e: Exception) {
            "Lỗi khi phân tích phản hồi: ${e.localizedMessage ?: e.toString()}"
        }
    }

    suspend fun analyzeNews(content: String): String {
        val prompt = buildNewsAnalysisPrompt(content)
        return generateContent(prompt)
    }

    suspend fun analyzeArticleFromUrl(url: String): String {
        val prompt = buildUrlAnalysisPrompt(url)
        return generateContent(prompt)
    }

    suspend fun continueArticleFromUrl(url: String): String {
        val prompt = buildArticleContinuationPrompt(url)
        return generateContent(prompt)
    }

    private fun buildNewsAnalysisPrompt(content: String): String {
        return """
            Phân tích nội dung tin tức sau đây:
            
            Nội dung: $content
            
            Hãy cung cấp phân tích với hai phần chính, định dạng rõ ràng bằng tiếng Việt:
            
            ### 1. Tóm tắt điểm chính
            - Tóm tắt nội dung chính của bài báo trong 3-5 câu.
            - Tập trung vào các ý chính, sự kiện hoặc thông tin cốt lõi.
            
            ### 2. Đánh giá và triển vọng
            - Các yếu tố liên quan đến tin tức (ví dụ: kinh tế, xã hội, chính trị, môi trường, công nghệ...).
            - Ảnh hưởng của tin tức đến các bên liên quan (cá nhân, tổ chức, cộng đồng, quốc gia...).
            - Triển vọng hoặc xu hướng tương lai liên quan đến chủ đề của bài báo.
            - Đánh giá tính khách quan của thông tin (nếu có thể).
            
            Định dạng trả lời phải rõ ràng, sử dụng tiêu đề cấp 3 (###) cho từng phần, và các gạch đầu dòng (-) cho các ý chính trong mỗi phần.
        """.trimIndent()
    }

    private fun buildUrlAnalysisPrompt(url: String): String {
        return """
            Hãy phân tích bài báo từ link sau: $url
            
            Hãy cung cấp phân tích với hai phần chính, định dạng rõ ràng bằng tiếng Việt:
            
            ### 1. Tóm tắt điểm chính
            - Tóm tắt nội dung chính của bài báo trong 3-5 câu.
            - Tập trung vào các ý chính, sự kiện hoặc thông tin cốt lõi.
            
            ### 2. Đánh giá và triển vọng
            - Các yếu tố liên quan đến tin tức (ví dụ: kinh tế, xã hội, chính trị, môi trường, công nghệ...).
            - Ảnh hưởng của tin tức đến các bên liên quan (cá nhân, tổ chức, cộng đồng, quốc gia...).
            - Triển vọng hoặc xu hướng tương lai liên quan đến chủ đề của bài báo.
            - Đánh giá tính khách quan của thông tin (nếu có thể).
            
            Định dạng trả lời phải rõ ràng, sử dụng tiêu đề cấp 3 (###) cho từng phần, và các gạch đầu dòng (-) cho các ý chính trong mỗi phần.
        """.trimIndent()
    }

    private fun buildArticleContinuationPrompt(url: String): String {
        return """
            Dựa trên bài báo từ link sau: $url
            
            Hãy tiếp tục viết bài báo, mở rộng nội dung một cách tự nhiên và logic. Nội dung tiếp tục nên:
            1. Giữ nguyên giọng điệu và phong cách của bài báo gốc
            2. Cung cấp thông tin bổ sung hoặc phân tích sâu hơn về chủ đề
            3. Có độ dài khoảng 3-5 đoạn văn
            4. Đảm bảo tính chính xác và phù hợp với ngữ cảnh
            
            Hãy trả lời bằng tiếng Việt, định dạng rõ ràng.
        """.trimIndent()
    }
}