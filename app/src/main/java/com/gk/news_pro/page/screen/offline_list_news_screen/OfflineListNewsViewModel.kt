package com.gk.news_pro.page.screen.offline_list_news_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.local.AppDatabase
import com.gk.news_pro.data.local.entity.NewsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OfflineListNewsViewModel(
    private val database: AppDatabase
) : ViewModel() {

    private val _savedNews = MutableStateFlow<List<NewsEntity>>(emptyList())
    val savedNews: StateFlow<List<NewsEntity>> = _savedNews

    init {
        refreshSavedNews()
    }

    fun refreshSavedNews() {
        viewModelScope.launch {
            try {
                database.newsDao().getAllSavedNews().collectLatest { newsList ->
                    _savedNews.value = newsList
                    Log.d("OfflineListNewsViewModel", "Loaded ${newsList.size} saved news")
                }
            } catch (e: Exception) {
                Log.e("OfflineListNewsViewModel", "Error loading saved news: ${e.message}", e)
                _savedNews.value = emptyList()
            }
        }
    }

    fun removeSavedNews(link: String) {
        viewModelScope.launch {
            try {
                database.newsDao().deleteNews(link)
                // No need to update _savedNews manually; Flow will emit the updated list
                Log.d("OfflineListNewsViewModel", "Removed news with link: $link")
            } catch (e: Exception) {
                Log.e("OfflineListNewsViewModel", "Error removing saved news: ${e.message}", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("OfflineListNewsViewModel", "ViewModel cleared")
    }
}

class OfflineListNewsViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfflineListNewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OfflineListNewsViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}