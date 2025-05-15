package com.gk.news_pro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gk.news_pro.data.local.entity.NewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNews(news: NewsEntity)

    @Query("SELECT * FROM saved_news ORDER BY savedAt DESC")
    fun getAllSavedNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM saved_news WHERE link = :link")
    suspend fun getSavedNewsByLink(link: String): NewsEntity?

    @Query("DELETE FROM saved_news WHERE link = :link")
    suspend fun deleteNews(link: String)
}