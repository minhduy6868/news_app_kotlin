package com.gk.news_pro.page.screen.home_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    private val _newsState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val newsState: StateFlow<NewsUiState> = _newsState

    // Lưu trữ danh sách News
    private val newsList = mutableListOf<News>()

    init {
        fetchGeneralNews()
    }

    fun fetchGeneralNews() {
        _newsState.value = NewsUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc"
                )
                if (response.results.isNullOrEmpty()) {
                    Log.e("HomeViewModel", "No news available from API")
                    _newsState.value = NewsUiState.Error("No news available")
                } else {
                    newsList.clear()
                    newsList.addAll(response.results)
                    Log.d("HomeViewModel", "Loaded ${newsList.size} news items")
                    _newsState.value = NewsUiState.Success(response.results)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading news: ${e.message}")
                _newsState.value = NewsUiState.Error(e.message ?: "Failed to load news")
            }
        }
    }

    fun getNewsById(articleId: String): News? {
        val news = newsList.find { it.article_id == articleId }
        if (news == null) {
            Log.e("HomeViewModel", "No news found for articleId: $articleId")
        } else {
            Log.d("HomeViewModel", "Found news for articleId: $articleId, title: ${news.title}")
        }
        return news
    }

    fun retry() {
        fetchGeneralNews()
    }
}

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val news: List<News>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}