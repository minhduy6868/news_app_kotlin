package com.gk.news_pro.page.screen.create_post

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
import java.io.File

class CreatePostViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState: StateFlow<CreatePostUiState> = _uiState

    private val TAG = "CreatePostViewModel"

    sealed class CreatePostUiState {
        object Idle : CreatePostUiState()
        object Loading : CreatePostUiState()
        data class Success(val post: Post) : CreatePostUiState()
        data class Error(val message: String) : CreatePostUiState()
    }

    fun createPost(content: String, imageFiles: List<File>?, location: String?) {
        viewModelScope.launch {
            _uiState.value = CreatePostUiState.Loading
            try {
                val user = auth.currentUser ?: throw IllegalStateException("User not logged in")
                val userData = userRepository.getUser()
                    ?: throw IllegalStateException("User data not found")

                val post = postRepository.createPost(
                    content = content,
                    imageFiles = imageFiles,
                    location = location,
                    username = userData.username
                )
                _uiState.value = CreatePostUiState.Success(post)
                Log.d(TAG, "Post created successfully: ${post.postId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create post: ${e.message}", e)
                _uiState.value = CreatePostUiState.Error("Failed to create post: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreatePostUiState.Idle
    }
}