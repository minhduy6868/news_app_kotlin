package com.gk.news_pro.page.screen.explore_sceen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    private val _newsState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val newsState: StateFlow<ExploreUiState> = _newsState

    private val _trendingNews = MutableStateFlow<List<News>>(emptyList())
    val trendingNews: StateFlow<List<News>> = _trendingNews

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("general")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _bookmarkedNews = MutableStateFlow<List<News>>(emptyList())
    val bookmarkedNews: StateFlow<List<News>> = _bookmarkedNews

    val categories = listOf(
        "general", "business", "entertainment", "health",
        "science", "sports", "technology"
    )

    init {
        fetchGeneralNews()
        fetchTrendingNews()
    }

    fun fetchGeneralNews() {
        _newsState.value = ExploreUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc",
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Failed to load news")
            }
        }
    }

    fun fetchTrendingNews() {
        viewModelScope.launch {
            try {
                // Fetch trending news with different parameters (e.g., popular/latest)
                val response = repository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc",
                )
                _trendingNews.value = response.results?.take(5) ?: emptyList() // Limit to 5 trending items
            } catch (e: Exception) {
                _trendingNews.value = emptyList()
            }
        }
    }

    fun fetchNewsByCategory(category: String) {
        _selectedCategory.value = category
        _newsState.value = ExploreUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc",
                    category = category
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Failed to load news")
            }
        }
    }

    fun searchNews(query: String) {
        _newsState.value = ExploreUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc",
                    query = query
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Failed to search news")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        fetchGeneralNews()
    }

    fun retry() {
        if (searchQuery.value.isNotEmpty()) {
            searchNews(searchQuery.value)
        } else {
            fetchGeneralNews()
        }
    }

    fun toggleBookmark(news: News, isBookmarked: Boolean) {
        viewModelScope.launch {
            val currentBookmarks = _bookmarkedNews.value.toMutableList()
            if (isBookmarked) {
                currentBookmarks.add(news)
            } else {
                currentBookmarks.remove(news)
            }
            _bookmarkedNews.value = currentBookmarks
        }
    }
}

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    data class Success(val news: List<News>) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}