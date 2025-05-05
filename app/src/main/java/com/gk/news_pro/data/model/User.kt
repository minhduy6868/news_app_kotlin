package com.gk.news_pro.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val avatar: String = "",
    val username: String = "",
    val favoriteTopics: Map<String, Int> = emptyMap(),
    val favoriteNews: Map<String, News> = emptyMap()
)
