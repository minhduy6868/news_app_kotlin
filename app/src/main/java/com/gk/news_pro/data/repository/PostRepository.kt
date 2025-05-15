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

class PostRepository(
    private val firebasePostService: FirebasePostService = FirebasePostService(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val stringHelper: StringHelper = StringHelper()
) {
    private val TAG = "PostRepository"

    init {
        auth.setLanguageCode("en")
    }

    suspend fun createPost(content: String, imageFiles: List<File>?, location: String?, username: String): Post {
        if (content.isBlank()) throw IllegalArgumentException("Post content cannot be empty")

        val user = auth.currentUser ?: run {
            Log.e(TAG, "createPost: No user logged in")
            throw IllegalStateException("User not logged in")
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
            throw Exception("Failed to create post: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "createPost: Failed to create post: ${e.message}", e)
            throw Exception("Failed to create post: ${e.message}")
        }
    }

    suspend fun getPost(postId: String): Post? {
        if (postId.isBlank()) throw IllegalArgumentException("Post ID cannot be empty")
        try {
            return firebasePostService.getPost(postId)
        } catch (e: Exception) {
            Log.e(TAG, "getPost: Failed to retrieve post: ${e.message}", e)
            throw Exception("Failed to retrieve post: ${e.message}")
        }
    }

    suspend fun getAllPosts(): List<Post> {
        try {
            return firebasePostService.getAllPosts()
        } catch (e: Exception) {
            Log.e(TAG, "getAllPosts: Failed to retrieve posts: ${e.message}", e)
            throw Exception("Failed to retrieve posts: ${e.message}")
        }
    }

    suspend fun likePost(postId: String) {
        if (postId.isBlank()) throw IllegalArgumentException("Post ID cannot be empty")
        val user = auth.currentUser ?: run {
            Log.e(TAG, "likePost: No user logged in")
            throw IllegalStateException("User not logged in")
        }
        try {
            val post = firebasePostService.getPost(postId) ?: throw Exception("Post not found")
            val isLiked = post.likes[user.uid] == true
            firebasePostService.likePost(postId, user.uid, !isLiked)
            Log.d(TAG, "likePost: Toggled like for post $postId")
        } catch (e: Exception) {
            Log.e(TAG, "likePost: Failed to like post: ${e.message}", e)
            throw Exception("Failed to like post: ${e.message}")
        }
    }

    suspend fun addComment(postId: String, content: String, username: String): Comment {
        if (postId.isBlank()) throw IllegalArgumentException("Post ID cannot be empty")
        if (content.isBlank()) throw IllegalArgumentException("Comment content cannot be empty")
        val user = auth.currentUser ?: run {
            Log.e(TAG, "addComment: No user logged in")
            throw IllegalStateException("User not logged in")
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
            throw Exception("Failed to add comment: ${e.message}")
        }
    }

    suspend fun deletePost(postId: String) {
        if (postId.isBlank()) throw IllegalArgumentException("Post ID cannot be empty")
        val user = auth.currentUser ?: run {
            Log.e(TAG, "deletePost: No user logged in")
            throw IllegalStateException("User not logged in")
        }
        try {
            val post = firebasePostService.getPost(postId) ?: throw Exception("Post not found")
            if (post.userId != user.uid) {
                throw Exception("You do not have permission to delete this post")
            }
            firebasePostService.deletePost(postId)
            Log.d(TAG, "deletePost: Post $postId deleted")
        } catch (e: Exception) {
            Log.e(TAG, "deletePost: Failed to delete post: ${e.message}", e)
            throw Exception("Failed to delete post: ${e.message}")
        }
    }
}