package com.gk.news_pro.page.screen.detail_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class NewsDetailViewModel(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis: StateFlow<String?> = _aiAnalysis

    fun analyzeNewsContent(news: News) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _aiAnalysis.value = null

            try {
                val content = generateContentPrompt(news)
                val response = geminiRepository.generateContent("AIzaSyCLbQa-NpxZKbgLzhaCp9ugcTVLvz1EDbM",content)

                val analysis = parseAnalysisFromResponse(response.toString())
                _aiAnalysis.value = analysis
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Đã xảy ra lỗi không xác định"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateContentPrompt(news: News): String {
        val builder = StringBuilder()
        builder.appendLine("Phân tích nội dung tin tức sau đây:")
        builder.appendLine()
        news.title?.let { builder.appendLine("Tiêu đề: $it") }
        news.description?.let { builder.appendLine("Mô tả: $it") }
        news.description?.let { builder.appendLine("Nội dung: $it") }
        return builder.toString()
    }

    private fun parseAnalysisFromResponse(response: String): String {
        return try {
            val json = JSONObject(response)
            json.optString("analysis", "Không có nội dung phân tích.")
        } catch (e: Exception) {
            "Không thể phân tích phản hồi từ AI."
        }
    }
}
