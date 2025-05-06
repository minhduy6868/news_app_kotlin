package com.gk.news_pro.page.screen.explore_sceen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val newsRepository: NewsRepository,
    private val userRepository: UserRepository
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
        loadBookmarkedNews()
    }

    private fun loadBookmarkedNews() {
        viewModelScope.launch {
            if (userRepository.isLoggedIn()) {
                try {
                    val favoriteNews = userRepository.getFavoriteNewsList()
                    _bookmarkedNews.value = favoriteNews
                    Log.d("ExploreViewModel", "Loaded ${favoriteNews.size} bookmarked news")
                } catch (e: Exception) {
                    Log.e("ExploreViewModel", "Error loading bookmarked news: ${e.message}", e)
                }
            } else {
                Log.d("ExploreViewModel", "User not logged in, skipping bookmark load")
                _bookmarkedNews.value = emptyList()
            }
        }
    }

    fun fetchGeneralNews() {
        _newsState.value = ExploreUiState.Loading
        viewModelScope.launch {
            try {
                val response = newsRepository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc"
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
                Log.d("ExploreViewModel", "Fetched ${response.results?.size ?: 0} general news")
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Failed to load news")
                Log.e("ExploreViewModel", "Error fetching general news: ${e.message}", e)
            }
        }
    }

    fun fetchTrendingNews() {
        viewModelScope.launch {
            try {
                val response = newsRepository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc"
                )
                _trendingNews.value = response.results?.take(5) ?: emptyList()
                Log.d("ExploreViewModel", "Fetched ${response.results?.size ?: 0} trending news, limited to 5")
            } catch (e: Exception) {
                _trendingNews.value = emptyList()
                Log.e("ExploreViewModel", "Error fetching trending news: ${e.message}", e)
            }
        }
    }

    fun fetchNewsByCategory(category: String) {
        _selectedCategory.value = category
        _newsState.value = ExploreUiState.Loading
        viewModelScope.launch {
            try {
                val response = newsRepository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc",
                    category = category
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
                Log.d("ExploreViewModel", "Fetched ${response.results?.size ?: 0} news for category $category")
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Failed to load news")
                Log.e("ExploreViewModel", "Error fetching news for category $category: ${e.message}", e)
            }
        }
    }

    fun searchNews(query: String) {
        _newsState.value = ExploreUiState.Loading
        viewModelScope.launch {
            try {
                val response = newsRepository.getNews(
                    apiKey = "pub_7827211e80c068cf7ded249ee01e644d60afc",
                    query = query
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
                Log.d("ExploreViewModel", "Fetched ${response.results?.size ?: 0} news for query $query")
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Failed to search news")
                Log.e("ExploreViewModel", "Error searching news for query $query: ${e.message}", e)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        Log.d("ExploreViewModel", "Updated search query: $query")
    }

    fun clearSearch() {
        _searchQuery.value = ""
        fetchGeneralNews()
        Log.d("ExploreViewModel", "Cleared search query")
    }

    fun retry() {
        if (searchQuery.value.isNotEmpty()) {
            searchNews(searchQuery.value)
        } else {
            fetchGeneralNews()
        }
        Log.d("ExploreViewModel", "Retrying with query: ${searchQuery.value}")
    }

    fun toggleBookmark(news: News, isBookmarked: Boolean) {
        viewModelScope.launch {
            if (!userRepository.isLoggedIn()) {
                Log.d("ExploreViewModel", "User not logged in, cannot toggle bookmark")
                return@launch
            }

            try {
                if (isBookmarked) {
                    Log.d("ExploreViewModel", "Adding news ${news.article_id} to favorites")
                    userRepository.saveFavoriteNews(news)
                    _bookmarkedNews.value = _bookmarkedNews.value + news
                } else {
                    Log.d("ExploreViewModel", "Removing news ${news.article_id} from favorites")
                    userRepository.removeFavoriteNews(news.article_id)
                    _bookmarkedNews.value = _bookmarkedNews.value - news
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Error toggling bookmark for news ${news.article_id}: ${e.message}", e)
            }
        }
    }
}

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    data class Success(val news: List<News>) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}