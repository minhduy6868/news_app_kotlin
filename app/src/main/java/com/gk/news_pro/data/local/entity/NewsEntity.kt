package com.gk.news_pro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gk.news_pro.data.model.News

@Entity(tableName = "saved_news")
data class NewsEntity(
    @PrimaryKey val link: String,
    val title: String,
    val description: String?,
    val image_url: String?,
    val source_name: String,
    val pubDate: String?,
    val aiAnalysis: String?,
    val savedAt: Long = System.currentTimeMillis()
)

fun NewsEntity.toNews(): News {
    return News(
        link = link,
        title = title,
        description = description,
        image_url = image_url,
        source_name = source_name,
        pubDate = pubDate
    )
}

fun News.toNewsEntity(aiAnalysis: String? = null): NewsEntity {
    return NewsEntity(
        link = link,
        title = title,
        description = description,
        image_url = image_url,
        source_name = source_name,
        pubDate = pubDate,
        aiAnalysis = aiAnalysis
    )
}