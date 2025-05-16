package com.gk.news_pro.page.screen.explore_sceen

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.MainActivity
import com.gk.news_pro.R
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.GeminiRepository
import com.gk.news_pro.data.repository.HeyGenRepository
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.main_viewmodel.PrefsManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExploreViewModel(
    private val newsRepository: NewsRepository,
    private val userRepository: UserRepository,
    private val prefsManager: PrefsManager,
    private val context: Context,
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

    private var notificationSent = false

    val categories = listOf(
        "general", "business", "entertainment", "health",
        "science", "sports", "technology"
    )

    private val defaultVideoUrl = "https://files2.heygen.ai/aws_pacific/avatar_tmp/9126811df9534fae9d242898dbc51bb7/902a481e7ac742e29757e76bfd2e4424.mp4?Expires=1747673557&Signature=IeYKG2te0uqKcGsgX5gfB6qH29LKUt8thmDV-MXpepcrcaxUJNO55w7Ojh-9o84wxLSf3SJou~vHHOliBFV-PMrBElRVVFQgAyop7oRODIoIYhNyRPaBvlqeDvtZys-6hBNdV5d6vyWzImidZWNDUneU71C~49LZZhNn8CLc1eMNNSSEjP8HRKdG0mB1bbtkBJZHqBdeETFjpRtpeUzw6cKMx91pBbgD9CMxKo~bW~mH~PGunrjR1SwreNrKVJWYnbPwVQUXxUd4LLFLlEYTTQxKiY~0zZ2QPqJC9IIaf1gG5AtKwqJWTAUvDRr18AAb0nP0P1vcEOKPMh9eviliCA__&Key-Pair-Id=K38HBHX5LX3X2H"

    init {
        fetchGeneralNews()
        fetchTrendingNews()
        loadBookmarkedNews()
        checkPendingVideo()
    }

    private suspend fun sendPushNotification(videoUrl: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("ExploreViewModel", "FCM Token: $token")

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "video_notification_channel"

            // Kiểm tra quyền thông báo trên Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("ExploreViewModel", "Quyền POST_NOTIFICATIONS chưa được cấp, không thể gửi thông báo")
                    return
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Thông Báo Video",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Thông báo khi video tóm tắt hoàn tất"
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("ExploreViewModel", "Đã tạo Notification Channel: $channelId")
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("video_url", videoUrl) // Thêm URL video vào intent
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Video Tóm Tắt Sẵn Sàng")
                .setContentText("Video tóm tắt tin tức mới đã được tạo!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            Log.d("ExploreViewModel", "Thông báo cục bộ đã được gửi cho video: $videoUrl")
        } catch (e: Exception) {
            Log.e("ExploreViewModel", "Lỗi khi gửi thông báo cục bộ: ${e.message}", e)
        }
    }

    fun checkAndGenerateDailyVideo() {
        viewModelScope.launch {
            notificationSent = false // Đặt lại notificationSent khi bắt đầu tạo video mới
            val videoId = prefsManager.getVideoId()
            if (videoId != null) {
                Log.d("ExploreViewModel", "Existing video_id found: $videoId, checking status immediately")
                checkVideoStatus(videoId)
            } else if (prefsManager.shouldCreateDailyVideo()) {
                Log.d("ExploreViewModel", "Starting daily video generation")
                while (_trendingNews.value.isEmpty()) {
                    Log.d("ExploreViewModel", "Waiting for trending news...")
                    delay(1000L)
                }

                repeat(3) { attempt ->
                    try {
                        Log.d("ExploreViewModel", "Generating daily video, attempt ${attempt + 1}")
                        val newsList = _trendingNews.value.take(7)
                        if (newsList.isEmpty()) {
                            Log.e("ExploreViewModel", "No trending news available for daily video")
                            _videoScriptState.value = VideoScriptState.Error("Không có tin tức để tạo video")
                            return@launch
                        }
                        val script = geminiRepository.generateNewsVideoScript(newsList)
                        Log.d("ExploreViewModel", "Generated script (length: ${script.length}): $script")
                        if (script.isBlank() || script.startsWith("Lỗi")) {
                            Log.e("ExploreViewModel", "Invalid script for daily video: $script")
                            _videoScriptState.value = VideoScriptState.Error("Kịch bản không hợp lệ: $script")
                            return@launch
                        }
                        val result = heyGenRepository.generateVideo(script)
                        when (result) {
                            is HeyGenRepository.Result.Success -> {
                                prefsManager.saveVideoId(result.videoId)
                                prefsManager.saveVideoCreationTime(System.currentTimeMillis())
                                _videoScriptState.value = VideoScriptState.Processing(script)
                                Log.d("ExploreViewModel", "Daily video creation started, video_id: ${result.videoId}")
                                delay(3 * 60 * 1000L)
                                checkVideoStatus(result.videoId)
                                return@launch
                            }
                            is HeyGenRepository.Result.Error -> {
                                Log.e("ExploreViewModel", "Daily video generation failed: ${result.message}")
                                _videoScriptState.value = VideoScriptState.Error(result.message)
                                return@launch
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ExploreViewModel", "Error generating daily video, attempt ${attempt + 1}: ${e.message}", e)
                        if (attempt == 2) {
                            _videoScriptState.value = VideoScriptState.Error("Lỗi khi tạo video: ${e.message}")
                            val fallbackUrl = prefsManager.getLastSuccessfulVideoUrl() ?: defaultVideoUrl
                            _latestVideoUrl.value = fallbackUrl
                            _videoScriptState.value = VideoScriptState.Success(script = "Default script", videoUrl = fallbackUrl)
                        }
                        delay(1000L)
                    }
                }
            } else {
                Log.d("ExploreViewModel", "Daily video already generated today")
                val fallbackUrl = prefsManager.getLastSuccessfulVideoUrl() ?: defaultVideoUrl
                _latestVideoUrl.value = fallbackUrl
                _videoScriptState.value = VideoScriptState.Success(script = "Default script", videoUrl = fallbackUrl)
            }
        }
    }

    private fun checkPendingVideo() {
        viewModelScope.launch {
            val videoId = prefsManager.getVideoId()
            Log.d("ExploreViewModel", "Checking pending video, video_id: $videoId")
            if (videoId != null) {
                Log.d("ExploreViewModel", "Checking status immediately for video_id: $videoId")
                checkVideoStatus(videoId)
            } else {
                Log.d("ExploreViewModel", "No pending video found, triggering daily video check")
                checkAndGenerateDailyVideo()
            }
        }
    }

    suspend fun checkVideoStatusAfterDelay(videoId: String) {
        Log.d("ExploreViewModel", "Checking video status with delay for video_id: $videoId")
        delay(3 * 60 * 1000L)
        checkVideoStatus(videoId)
    }

    private fun checkVideoStatusPeriodically(videoId: String) {
        viewModelScope.launch {
            repeat(30) { attempt ->
                Log.d("ExploreViewModel", "Checking video status periodically, attempt ${attempt + 1}, video_id: $videoId")
                checkVideoStatus(videoId)
                if (_videoScriptState.value !is VideoScriptState.Processing) return@launch
                delay(30_000L)
            }
            if (_videoScriptState.value is VideoScriptState.Processing) {
                _videoScriptState.value = VideoScriptState.Error("Video xử lý quá lâu, thử lại sau")
                Log.e("ExploreViewModel", "Video processing timed out for video_id: $videoId")
                val lastUrl = prefsManager.getLastSuccessfulVideoUrl()
                val fallbackUrl = if (lastUrl != null && isVideoUrlValid(lastUrl)) {
                    lastUrl
                } else {
                    defaultVideoUrl
                }
                _latestVideoUrl.value = fallbackUrl
                _videoScriptState.value = VideoScriptState.Success(script = "Default script", videoUrl = fallbackUrl)
                delay(30 * 60 * 1000L)
                checkAndGenerateDailyVideo()
            }
        }
    }

    private suspend fun isVideoUrlValid(url: String): Boolean {
        return try {
            val expires = url.substringAfter("Expires=").substringBefore("&").toLongOrNull()
            val currentTime = System.currentTimeMillis() / 1000
            expires != null && currentTime <= expires
        } catch (e: Exception) {
            Log.e("ExploreViewModel", "Error validating video URL: ${e.message}")
            false
        }
    }

    suspend fun checkVideoStatus(videoId: String) {
        Log.d("ExploreViewModel", "Checking video status for video_id: $videoId")
        try {
            val result = heyGenRepository.checkVideoStatus(videoId)
            Log.d("ExploreViewModel", "Video status result: $result")
            when {
                result.isNotBlank() && !result.startsWith("Lỗi") && result != "Đang xử lý" -> {
                    val expires = result.substringAfter("Expires=").substringBefore("&").toLongOrNull()
                    val currentTime = System.currentTimeMillis() / 1000
                    if (expires != null && currentTime > expires) {
                        Log.w("ExploreViewModel", "Video URL expired, generating new video")
                        _videoScriptState.value = VideoScriptState.Error("Video URL đã hết hạn")
                        prefsManager.clearVideoId()
                        checkAndGenerateDailyVideo()
                    } else {
                        _latestVideoUrl.value = result
                        prefsManager.saveLastSuccessfulVideoUrl(result)
                        _videoScriptState.value = VideoScriptState.Success(
                            script = _videoScriptState.value.let { if (it is VideoScriptState.Processing) it.script else "" },
                            videoUrl = result
                        )
                        // Gửi thông báo nếu chưa gửi
                        if (!notificationSent) {
                            Log.d("ExploreViewModel", "Preparing to send notification. Last successful URL: ${prefsManager.getLastSuccessfulVideoUrl()}")
                            sendPushNotification(result)
                            notificationSent = true
                        } else {
                            Log.d("ExploreViewModel", "Notification already sent for this video, skipping")
                        }
                        prefsManager.clearVideoId()
                        Log.d("ExploreViewModel", "Video completed, URL: $result")
                    }
                }
                result == "Đang xử lý" -> {
                    _videoScriptState.value = VideoScriptState.Processing(
                        script = _videoScriptState.value.let { if (it is VideoScriptState.Processing) it.script else "" }
                    )
                    Log.d("ExploreViewModel", "Video still processing for video_id: $videoId")
                    checkVideoStatusPeriodically(videoId)
                }
                else -> {
                    Log.e("ExploreViewModel", "Error checking video status: $result")
                    _videoScriptState.value = VideoScriptState.Error(result)
                    val fallbackUrl = prefsManager.getLastSuccessfulVideoUrl() ?: defaultVideoUrl
                    _latestVideoUrl.value = fallbackUrl
                    _videoScriptState.value = VideoScriptState.Success(script = "Default script", videoUrl = fallbackUrl)
                }
            }
        } catch (e: Exception) {
            Log.e("ExploreViewModel", "Exception checking video status: ${e.message}", e)
            _videoScriptState.value = VideoScriptState.Error("Lỗi khi kiểm tra trạng thái video: ${e.message}")
            val fallbackUrl = prefsManager.getLastSuccessfulVideoUrl() ?: defaultVideoUrl
            _latestVideoUrl.value = fallbackUrl
            _videoScriptState.value = VideoScriptState.Success(script = "Default script", videoUrl = fallbackUrl)
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
                    apiKey = "pub_865207b1af8edb43a150aac59d2fcf96f8456"
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
                Log.d("ExploreViewModel", "Fetched ${response.results?.size ?: 0} general news")
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Không thể tải tin tức")
                Log.e("ExploreViewModel", "Error fetching general news: ${e.message}", e)
            }
        }
    }

    fun fetchTrendingNews() {
        viewModelScope.launch {
            try {
                val response = newsRepository.getNews(
                    apiKey = "pub_865207b1af8edb43a150aac59d2fcf96f8456"
                )
                _trendingNews.value = response.results?.take(7) ?: emptyList()
                Log.d("ExploreViewModel", "Fetched ${response.results?.size ?: 0} trending news, limited to 7")
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
                    apiKey = "pub_865207b1af8edb43a150aac59d2fcf96f8456",
                    category = category
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
                Log.d("ExploreViewModel", "Fetched ${response.results?.size ?: 0} news for category $category")
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Không thể tải tin tức")
                Log.e("ExploreViewModel", "Error fetching news for category $category: ${e.message}", e)
            }
        }
    }

    fun searchNews(query: String) {
        _newsState.value = ExploreUiState.Loading
        viewModelScope.launch {
            try {
                val response = newsRepository.getNews(
                    apiKey = "pub_865207b1af8edb43a150aac59d2fcf96f8456",
                    query = query
                )
                _newsState.value = ExploreUiState.Success(response.results ?: emptyList())
                Log.d("ExploreViewModel", "Fetched ${response.results?.size ?: 0} news for query $query")
            } catch (e: Exception) {
                _newsState.value = ExploreUiState.Error(e.message ?: "Không thể tìm kiếm tin tức")
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
        notificationSent = false // Đặt lại notificationSent khi tạo video mới
        viewModelScope.launch {
            try {
                Log.d("ExploreViewModel", "Generating script for ${newsList.size} news items")
                val script = geminiRepository.generateNewsVideoScript(newsList)
                Log.d("ExploreViewModel", "Generated script (length: ${script.length}): $script")
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
                        prefsManager.saveVideoId(result.videoId)
                        prefsManager.saveVideoCreationTime(System.currentTimeMillis())
                        _videoScriptState.value = VideoScriptState.Processing(script)
                        Log.d("ExploreViewModel", "Video creation started, video_id: ${result.videoId}")
                        delay(3 * 60 * 1000L)
                        checkVideoStatus(result.videoId)
                    }
                    is HeyGenRepository.Result.Error -> {
                        Log.e("ExploreViewModel", "Video generation failed: ${result.message}")
                        _videoScriptState.value = VideoScriptState.Error(result.message)
                        val fallbackUrl = prefsManager.getLastSuccessfulVideoUrl() ?: defaultVideoUrl
                        _latestVideoUrl.value = fallbackUrl
                        _videoScriptState.value = VideoScriptState.Success(script = "Default script", videoUrl = fallbackUrl)
                    }
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Error generating video: ${e.message}", e)
                _videoScriptState.value = VideoScriptState.Error(e.message ?: "Lỗi khi tạo video")
                val fallbackUrl = prefsManager.getLastSuccessfulVideoUrl() ?: defaultVideoUrl
                _latestVideoUrl.value = fallbackUrl
                _videoScriptState.value = VideoScriptState.Success(script = "Default script", videoUrl = fallbackUrl)
            }
        }
    }

    fun clearLatestVideo() {
        _latestVideoUrl.value = null
        prefsManager.clearVideoId()
        _videoScriptState.value = VideoScriptState.Idle
        notificationSent = false // Đặt lại notificationSent khi xóa video
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