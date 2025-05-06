package com.gk.news_pro.page.screen.favorite_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _favoriteNews = MutableStateFlow<List<News>>(emptyList())
    val favoriteNews: StateFlow<List<News>> = _favoriteNews

    init {
        loadFavoriteNews()
    }

    private fun loadFavoriteNews() {
        viewModelScope.launch {
            if (userRepository.isLoggedIn()) {
                try {
                    val newsList = userRepository.getFavoriteNewsList()
                    _favoriteNews.value = newsList
                    Log.d("FavoriteViewModel", "Loaded ${newsList.size} favorite news")
                } catch (e: Exception) {
                    Log.e("FavoriteViewModel", "Error loading favorite news: ${e.message}", e)
                    _favoriteNews.value = emptyList()
                }
            } else {
                Log.d("FavoriteViewModel", "User not logged in, no favorite news loaded")
                _favoriteNews.value = emptyList()
            }
        }
    }

    fun removeFavoriteNews(newsId: String) {
        viewModelScope.launch {
            try {
                userRepository.removeFavoriteNews(newsId)
                _favoriteNews.value = _favoriteNews.value.filter { it.article_id != newsId }
                Log.d("FavoriteViewModel", "Removed news with ID: $newsId")
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error removing favorite news: ${e.message}", e)
            }
        }
    }

    fun refreshFavoriteNews() {
        loadFavoriteNews()
    }
}