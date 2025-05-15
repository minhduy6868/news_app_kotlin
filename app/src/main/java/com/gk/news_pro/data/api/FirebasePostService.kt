package com.gk.news_pro.data.api

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.gk.news_pro.data.model.Comment
import com.gk.news_pro.data.model.Post
import kotlinx.coroutines.tasks.await

class FirebasePostService {

    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val postsRef: DatabaseReference = db.child("posts")
    private val TAG = "FirebasePostService"

    suspend fun createPost(post: Post) {
        try {
            postsRef.child(post.postId).setValue(post).await()
            Log.d(TAG, "createPost: Successfully created post ${post.postId}")
        } catch (e: Exception) {
            Log.e(TAG, "createPost: Failed to create post ${post.postId}: ${e.message}", e)
            throw e
        }
    }

    suspend fun getPost(postId: String): Post? {
        try {
            val snapshot = postsRef.child(postId).get().await()
            val post = snapshot.getValue(Post::class.java)
            Log.d(TAG, "getPost: Retrieved post $postId")
            return post
        } catch (e: Exception) {
            Log.e(TAG, "getPost: Failed to retrieve post $postId: ${e.message}", e)
            return null
        }
    }

    suspend fun getAllPosts(): List<Post> {
        try {
            val snapshot = postsRef.get().await()
            val postList = mutableListOf<Post>()
            snapshot.children.forEach {
                it.getValue(Post::class.java)?.let { post ->
                    postList.add(post)
                }
            }
            Log.d(TAG, "getAllPosts: Retrieved ${postList.size} posts")
            return postList.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "getAllPosts: Failed to retrieve posts: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun likePost(postId: String, userId: String, isLiked: Boolean) {
        try {
            postsRef.child(postId).child("likes").child(userId).setValue(isLiked).await()
            Log.d(TAG, "likePost: User $userId ${if (isLiked) "liked" else "unliked"} post $postId")
        } catch (e: Exception) {
            Log.e(TAG, "likePost: Failed to update like for post $postId: ${e.message}", e)
            throw e
        }
    }

    suspend fun addComment(postId: String, comment: Comment) {
        try {
            postsRef.child(postId).child("comments").child(comment.commentId).setValue(comment).await()
            Log.d(TAG, "addComment: Successfully added comment ${comment.commentId} to post $postId")
        } catch (e: Exception) {
            Log.e(TAG, "addComment: Failed to add comment to post $postId: ${e.message}", e)
            throw e
        }
    }

    suspend fun deletePost(postId: String) {
        try {
            postsRef.child(postId).removeValue().await()
            Log.d(TAG, "deletePost: Successfully deleted post $postId")
        } catch (e: Exception) {
            Log.e(TAG, "deletePost: Failed to delete post $postId: ${e.message}", e)
            throw e
        }
    }
}