package com.gk.news_pro.page.screen.explore_sceen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.GeminiRepository
import com.gk.news_pro.data.repository.HeyGenRepository
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExploreViewModel(
    private val newsRepository: NewsRepository,
    private val userRepository: UserRepository,
    private val geminiRepository: GeminiRepository = GeminiRepository(),
    private val heyGenRepository: HeyGenRepository
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

    private val _videoScriptState = MutableStateFlow<VideoScriptState>(VideoScriptState.Idle)
    val videoScriptState: StateFlow<VideoScriptState> = _videoScriptState

    private val _latestVideoUrl = MutableStateFlow<String?>(null)
    val latestVideoUrl: StateFlow<String?> = _latestVideoUrl

    val categories = listOf(
        "general", "business", "entertainment", "health",
        "science", "sports", "technology"
    )

    init {
        fetchGeneralNews()
        fetchTrendingNews()
        loadBookmarkedNews()
        checkPendingVideo()
    }

    private fun checkPendingVideo() {
        viewModelScope.launch {
            val videoId = heyGenRepository.getSavedVideoId()
            val creationTime = heyGenRepository.getCreationTime()
            if (videoId != null) {
                val elapsedTime = System.currentTimeMillis() - creationTime
                val fiveMinutes = 5 * 60 * 1000L
                Log.d("ExploreViewModel", "Checking pending video: video_id=$videoId, elapsed=$elapsedTime ms")
                if (elapsedTime >= fiveMinutes) {
                    checkVideoStatus(videoId)
                } else {
                    _videoScriptState.value = VideoScriptState.Processing(
                        script = _videoScriptState.value.let { if (it is VideoScriptState.Processing) it.script else "" }
                    )
                }
            } else {
                Log.d("ExploreViewModel", "No pending video found")
            }
        }
    }

    private suspend fun checkVideoStatus(videoId: String) {
        Log.d("ExploreViewModel", "Checking video status for video_id=$videoId")
        val result = heyGenRepository.checkVideoStatus(videoId)
        when {
            !result.startsWith("Lỗi") && result != "Đang xử lý" -> {
                _latestVideoUrl.value = result
                _videoScriptState.value = VideoScriptState.Success(
                    script = _videoScriptState.value.let { if (it is VideoScriptState.Processing) it.script else "" },
                    videoUrl = result
                )
                heyGenRepository.clearVideoId()
                Log.d("ExploreViewModel", "Video completed: $result")
            }
            result == "Đang xử lý" -> {
                _videoScriptState.value = VideoScriptState.Processing(
                    script = _videoScriptState.value.let { if (it is VideoScriptState.Processing) it.script else "" }
                )
                Log.d("ExploreViewModel", "Video still processing for video_id: $videoId")
            }
            else -> {
                _videoScriptState.value = VideoScriptState.Error(result)
                heyGenRepository.clearVideoId()
                Log.e("ExploreViewModel", "Error checking video status: $result")
            }
        }
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

    fun generateNewsVideo(newsList: List<News>) {
        if (newsList.isEmpty()) {
            _videoScriptState.value = VideoScriptState.Error("Không có tin tức để tạo video")
            Log.e("ExploreViewModel", "News list is empty")
            return
        }
        _videoScriptState.value = VideoScriptState.Loading
        viewModelScope.launch {
            try {
                Log.d("ExploreViewModel", "Generating script for ${newsList.size} news items")
                val script = geminiRepository.generateNewsVideoScript(newsList)
                Log.d("ExploreViewModel", "Generated script: $script")
                if (script.isBlank() || script.startsWith("Lỗi")) {
                    _videoScriptState.value = VideoScriptState.Error("Kịch bản không hợp lệ: $script")
                    Log.e("ExploreViewModel", "Invalid script: $script")
                    return@launch
                }
                _videoScriptState.value = VideoScriptState.Processing(script)
                heyGenRepository.clearVideoId()
                val result = heyGenRepository.generateVideo(script)
                when (result) {
                    is HeyGenRepository.Result.Success -> {
                        _videoScriptState.value = VideoScriptState.Processing(result.script)
                        Log.d("ExploreViewModel", "Video creation started, video_id: ${result.videoId}")
                    }
                    is HeyGenRepository.Result.Error -> {
                        _videoScriptState.value = VideoScriptState.Error(result.message)
                        Log.e("ExploreViewModel", "Video generation failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _videoScriptState.value = VideoScriptState.Error(e.message ?: "Lỗi khi tạo video")
                Log.e("ExploreViewModel", "Error generating video: ${e.message}", e)
            }
        }
    }

    fun clearLatestVideo() {
        _latestVideoUrl.value = null
        heyGenRepository.clearVideoId()
        Log.d("ExploreViewModel", "Cleared latest video URL")
    }
}

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    data class Success(val news: List<News>) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

sealed class VideoScriptState {
    object Idle : VideoScriptState()
    object Loading : VideoScriptState()
    data class Processing(val script: String) : VideoScriptState()
    data class Success(val script: String, val videoUrl: String) : VideoScriptState()
    data class Error(val message: String) : VideoScriptState()
}