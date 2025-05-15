package com.gk.news_pro.data.model

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class Post(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val imageUrls: List<String>? = null, // Changed from imageUrl to imageUrls
    val location: String? = null,
    val timestamp: Long = 0L,
    val likes: Map<String, Boolean> = emptyMap(),
    val comments: Map<String, Comment> = emptyMap()
) : Serializable

@IgnoreExtraProperties
data class Comment(
    val commentId: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0L
) : Serializable