package com.gk.news_pro.page.screen.news_feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.Post
import com.gk.news_pro.data.repository.PostRepository
import com.gk.news_pro.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsFeedViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _uiState = MutableStateFlow<NewsFeedUiState>(NewsFeedUiState.Idle)
    val uiState: StateFlow<NewsFeedUiState> = _uiState

    private val TAG = "NewsFeedViewModel"

    sealed class NewsFeedUiState {
        object Idle : NewsFeedUiState()
        object Loading : NewsFeedUiState()
        data class Success(val message: String? = null) : NewsFeedUiState()
        data class Error(val message: String) : NewsFeedUiState()
    }

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = NewsFeedUiState.Loading
            try {
                val postList = postRepository.getAllPosts()
                _posts.value = postList.sortedByDescending { it.timestamp }
                _uiState.value = NewsFeedUiState.Success()
                Log.d(TAG, "Loaded ${postList.size} posts")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load posts: ${e.message}", e)
                _uiState.value = NewsFeedUiState.Error("Không thể tải bài đăng: ${e.message}")
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                postRepository.likePost(postId)
                updateSinglePost(postId)
                _uiState.value = NewsFeedUiState.Success("Đã cập nhật lượt thích")
                Log.d(TAG, "Liked/unliked post: $postId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to like post: ${e.message}", e)
                _uiState.value = NewsFeedUiState.Error("Không thể thích bài đăng: ${e.message}")
            }
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw IllegalStateException("User not logged in")
                val userData = userRepository.getUser()
                    ?: throw IllegalStateException("User data not found")
                postRepository.addComment(postId, content, userData.username)
                updateSinglePost(postId)
                _uiState.value = NewsFeedUiState.Success("Đã thêm bình luận")
                Log.d(TAG, "Added comment to post: $postId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add comment: ${e.message}", e)
                _uiState.value = NewsFeedUiState.Error("Không thể thêm bình luận: ${e.message}")
            }
        }
    }

    private fun updateSinglePost(postId: String) {
        viewModelScope.launch {
            try {
                val updatedPost = postRepository.getPost(postId)
                if (updatedPost != null) {
                    val currentPosts = _posts.value.toMutableList()
                    val index = currentPosts.indexOfFirst { it.postId == postId }
                    if (index != -1) {
                        currentPosts[index] = updatedPost
                        _posts.value = currentPosts.sortedByDescending { it.timestamp }
                    }
                    Log.d(TAG, "Updated post: $postId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update post: ${e.message}", e)
                _uiState.value = NewsFeedUiState.Error("Không thể cập nhật bài đăng: ${e.message}")
            }
        }
    }

    fun refreshPosts() {
        loadPosts()
    }

    fun resetState() {
        _uiState.value = NewsFeedUiState.Idle
    }
}