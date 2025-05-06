package com.gk.news_pro.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NewsResponse(
    val status: String = "",
    val totalResults: Int = 0,
    val results: List<News> = emptyList()
)

@IgnoreExtraProperties
data class News(
    val article_id: String = "",
    val title: String = "",
    val link: String = "",
    val description: String? = null,
    val pubDate: String? = null,
    val image_url: String? = null,
    val source_name: String = "",
    val category: List<String> = emptyList(),
    val country: List<String> = emptyList(),
    val language: String = ""
)