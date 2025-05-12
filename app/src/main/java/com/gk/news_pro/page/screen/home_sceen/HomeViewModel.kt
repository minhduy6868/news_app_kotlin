package com.gk.news_pro.page.screen.home_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.model.User
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    private val _newsState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val newsState: StateFlow<NewsUiState> = _newsState
    private val TAG = "HomeViewModel"

    // Lưu trữ danh sách News
    private val newsList = mutableListOf<News>()

    init {
        fetchGeneralNews()
    }

    fun testGetUser() {
        val userRepository = UserRepository()
        viewModelScope.launch {
            try {
                val user = userRepository.createUser(
                    username = "duy test",
                    email = "ttttesttttvvv@example.com",
                    password = "test1234"
                )
                Log.d(TAG, "testGetUser: Create user successful: ${user?.email}, Password = ${user?.password}")
            } catch (e: Exception) {
                Log.e(TAG, "testGetUser: Error creating user: ${e.message}", e)
            }
        }
    }

    fun fetchGeneralNews() {
        _newsState.value = NewsUiState.Loading
        viewModelScope.launch {
            try {
                testGetUser()
                val response = repository.getNews(
                    apiKey = "pub_832257f70990e247b185d0a5036ebffda6e10" //"pub_865207b1af8edb43a150aac59d2fcf96f8456"
                )
                if (response.results.isNullOrEmpty()) {
                    Log.e(TAG, "fetchGeneralNews: No news available from API")
                    _newsState.value = NewsUiState.Error("No news available")
                } else {
                    newsList.clear()
                    newsList.addAll(response.results)
                    Log.d(TAG, "fetchGeneralNews: Loaded ${newsList.size} news items")
                    _newsState.value = NewsUiState.Success(response.results)
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchGeneralNews: Error loading news: ${e.message}", e)
                _newsState.value = NewsUiState.Error(e.message ?: "Failed to load news")
            }
        }
    }

    fun getNewsById(articleId: String): News? {
        val news = newsList.find { it.article_id == articleId }
        if (news == null) {
            Log.e(TAG, "getNewsById: No news found for articleId: $articleId")
        } else {
            Log.d(TAG, "getNewsById: Found news for articleId: $articleId, title: ${news.title}")
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