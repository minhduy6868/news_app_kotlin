package com.gk.news_pro.page.screen.detail_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsDetailViewModel(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis: StateFlow<String?> = _aiAnalysis

    private val _aiContinuation = MutableStateFlow<String?>(null)
    val aiContinuation: StateFlow<String?> = _aiContinuation

    fun analyzeNewsContent(news: News) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val content = buildString {
                    append(news.title)
                    if (!news.description.isNullOrEmpty()) {
                        append("\n\n${news.description}")
                    }
                }
                val analysis = geminiRepository.analyzeNews(content)
                _aiAnalysis.value = analysis
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "Nội dung bài báo không hợp lệ"
                    else -> e.localizedMessage ?: "Lỗi không xác định khi phân tích nội dung"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeNewsFromUrl(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val analysis = geminiRepository.analyzeArticleFromUrl(url)
                _aiAnalysis.value = analysis
            } catch (e:

                     Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "URL không hợp lệ"
                    is java.net.UnknownHostException -> "Không thể kết nối đến URL"
                    else -> e.localizedMessage ?: "Lỗi không xác định khi phân tích URL"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun continueArticle(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val continuation = geminiRepository.continueArticleFromUrl(url)
                _aiContinuation.value = continuation
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IllegalArgumentException -> "URL không hợp lệ để tiếp tục bài báo"
                    is java.net.UnknownHostException -> "Không thể kết nối đến URL"
                    else -> e.localizedMessage ?: "Lỗi không xác định khi tiếp tục bài báo"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAnalysis() {
        _aiAnalysis.value = null
        _error.value = null
    }

    fun clearContinuation() {
        _aiContinuation.value = null
        _error.value = null
    }
}