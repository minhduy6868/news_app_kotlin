package com.gk.news_pro.data.repository

import StringHelper
import android.util.Log
import com.gk.news_pro.data.api.FirebasePostService
import com.gk.news_pro.data.model.Comment
import com.gk.news_pro.data.model.Post
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import java.util.regex.Pattern

class PostRepository(
    private val firebasePostService: FirebasePostService = FirebasePostService(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val stringHelper: StringHelper = StringHelper()
) {
    private val TAG = "PostRepository"

    // Custom exception for content violations
    class ContentViolationException(message: String) : Exception(message)

    // List of forbidden words/phrases (case-insensitive)
    private val forbiddenPatterns = listOf(
        // Vulgar language
        Pattern.compile("\\b(đụ|địt|lồn|cặc|chó chết|đéo|mẹ kiếp|đù má|con cặc|buồi|thằng ngu|con đĩ|mẹ mày|bố mày|đồ khốn|súc vật)\\b", Pattern.CASE_INSENSITIVE),
        // Offensive terms related to Ho Chi Minh
        Pattern.compile("\\b(hồ chí minh|hcm|bác hồ)\\s+(ngu|tệ|dở|tồi|xấu xa|đồ ngu|tệ hại)\\b", Pattern.CASE_INSENSITIVE),
        // Anti-Vietnam sentiment
        Pattern.compile("\\b(việt nam|vn|việt cộng)\\s+(ngu|lạc hậu|tệ|đồ ngu)\\b", Pattern.CASE_INSENSITIVE),
        // Sovereignty violations
        Pattern.compile("\\b(hoang sa|trường sa|hoangsa|truong sa|hoang sa|truong sa)\\s+(là của trung quốc|thuộc tq|china|trung quốc)\\b", Pattern.CASE_INSENSITIVE),
        // Hate speech
        Pattern.compile("\\b(giết người việt|diệt việt nam|chống việt nam|phản động)\\b", Pattern.CASE_INSENSITIVE)
    )

    init {
        auth.setLanguageCode("en")
    }

    // Filter content for forbidden words/phrases
    private fun filterContent(content: String) {
        val normalizedContent = content.lowercase().replace("[^a-zA-Z0-9\\s]".toRegex(), " ")
        for (pattern in forbiddenPatterns) {
            if (pattern.matcher(normalizedContent).find()) {
                Log.e(TAG, "Content violation detected in: $content")
                throw ContentViolationException("Nội dung vi phạm tiêu chuẩn cộng đồng")
            }
        }
    }

    suspend fun createPost(content: String, imageFiles: List<File>?, location: String?, username: String): Post {
        if (content.isBlank()) throw IllegalArgumentException("Nội dung bài đăng không được để trống")

        // Filter content for violations
        try {
            filterContent(content)
        } catch (e: ContentViolationException) {
            Log.e(TAG, "createPost: Content violation: ${e.message}")
            throw e
        }

        val user = auth.currentUser ?: run {
            Log.e(TAG, "createPost: No user logged in")
            throw IllegalStateException("Người dùng chưa đăng nhập")
        }

        try {
            Log.d(TAG, "createPost: Creating post for user ${user.uid}")
            val postId = UUID.randomUUID().toString()

            // Upload multiple images if provided
            val imageUrls = imageFiles?.mapNotNull { file ->
                if (!file.exists()) {
                    Log.e(TAG, "Image file does not exist: ${file.path}")
                    null
                } else {
                    stringHelper.uploadImageToCloudinary(file)
                        ?: run {
                            Log.e(TAG, "Image upload failed for file: ${file.path}")
                            null
                        }
                }
            }?.filter { it.isNotBlank() }

            Log.d(TAG, "Images uploaded: $imageUrls")

            val post = Post(
                postId = postId,
                userId = user.uid,
                username = username,
                content = content.trim(),
                imageUrls = imageUrls?.ifEmpty { null },
                location = location?.trim(),
                timestamp = System.currentTimeMillis()
            )

            firebasePostService.createPost(post)
            Log.d(TAG, "createPost: Post created with ID $postId")
            return post
        } catch (e: FirebaseException) {
            Log.e(TAG, "createPost: Firebase error: ${e.message}", e)
            throw Exception("Tạo bài đăng thất bại: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "createPost: Failed to create post: ${e.message}", e)
            throw Exception("Tạo bài đăng thất bại: ${e.message}")
        }
    }

    suspend fun getPost(postId: String): Post? {
        if (postId.isBlank()) throw IllegalArgumentException("ID bài đăng không được để trống")
        try {
            return firebasePostService.getPost(postId)
        } catch (e: Exception) {
            Log.e(TAG, "getPost: Failed to retrieve post: ${e.message}", e)
            throw Exception("Lấy bài đăng thất bại: ${e.message}")
        }
    }

    suspend fun getAllPosts(): List<Post> {
        try {
            return firebasePostService.getAllPosts()
        } catch (e: Exception) {
            Log.e(TAG, "getAllPosts: Failed to retrieve posts: ${e.message}", e)
            throw Exception("Lấy danh sách bài đăng thất bại: ${e.message}")
        }
    }

    suspend fun likePost(postId: String) {
        if (postId.isBlank()) throw IllegalArgumentException("ID bài đăng không được để trống")
        val user = auth.currentUser ?: run {
            Log.e(TAG, "likePost: No user logged in")
            throw IllegalStateException("Người dùng chưa đăng nhập")
        }
        try {
            val post = firebasePostService.getPost(postId) ?: throw Exception("Bài đăng không tồn tại")
            val isLiked = post.likes[user.uid] == true
            firebasePostService.likePost(postId, user.uid, !isLiked)
            Log.d(TAG, "likePost: Toggled like for post $postId")
        } catch (e: Exception) {
            Log.e(TAG, "likePost: Failed to like post: ${e.message}", e)
            throw Exception("Thích bài đăng thất bại: ${e.message}")
        }
    }

    suspend fun addComment(postId: String, content: String, username: String): Comment {
        if (postId.isBlank()) throw IllegalArgumentException("ID bài đăng không được để trống")
        if (content.isBlank()) throw IllegalArgumentException("Nội dung bình luận không được để trống")

        // Filter comment content for violations
        try {
            filterContent(content)
        } catch (e: ContentViolationException) {
            Log.e(TAG, "addComment: Content violation: ${e.message}")
            throw e
        }

        val user = auth.currentUser ?: run {
            Log.e(TAG, "addComment: No user logged in")
            throw IllegalStateException("Người dùng chưa đăng nhập")
        }
        try {
            val commentId = UUID.randomUUID().toString()
            val comment = Comment(
                commentId = commentId,
                userId = user.uid,
                username = username,
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )
            firebasePostService.addComment(postId, comment)
            Log.d(TAG, "addComment: Comment added to post $postId")
            return comment
        } catch (e: Exception) {
            Log.e(TAG, "addComment: Failed to add comment: ${e.message}", e)
            throw Exception("Thêm bình luận thất bại: ${e.message}")
        }
    }

    suspend fun deletePost(postId: String) {
        if (postId.isBlank()) throw IllegalArgumentException("ID bài đăng không được để trống")
        val user = auth.currentUser ?: run {
            Log.e(TAG, "deletePost: No user logged in")
            throw IllegalStateException("Người dùng chưa đăng nhập")
        }
        try {
            val post = firebasePostService.getPost(postId) ?: throw Exception("Bài đăng không tồn tại")
            if (post.userId != user.uid) {
                throw Exception("Bạn không có quyền xóa bài đăng này")
            }
            firebasePostService.deletePost(postId)
            Log.d(TAG, "deletePost: Post $postId deleted")
        } catch (e: Exception) {
            Log.e(TAG, "deletePost: Failed to delete post: ${e.message}", e)
            throw Exception("Xóa bài đăng thất bại: ${e.message}")
        }
    }
}