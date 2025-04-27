package com.gk.news_pro.data.model
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val results: List<News>
)

data class News(
    val article_id: String,
    val title: String,
    val link: String,
    val description: String?,
    val pubDate: String?,
    val image_url: String?,
    val source_name: String,
    val category: List<String>,
    val country: List<String>,
    val language: String
)