package com.gk.news_pro.data.api

import com.gk.news_pro.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("news")
    suspend fun getNews(
        @Query("apikey") apiKey: String,
        @Query("q") query: String? = null,
        @Query("category") category: String? = null,
        @Query("country") country: String? = null,
        @Query("language") language: String? = "vi"
    ): NewsResponse
}