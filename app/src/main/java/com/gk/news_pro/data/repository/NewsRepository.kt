package com.gk.news_pro.data.repository

import com.gk.news_pro.data.api.NewsApiService
import com.gk.news_pro.data.model.NewsResponse
import com.gk.news_pro.page.utils.RetrofitClient

class NewsRepository {
    private val newsApiService: NewsApiService = RetrofitClient.newsRetrofit.create(NewsApiService::class.java)

    suspend fun getNews(apiKey: String, query: String? = null, category: String? = null): NewsResponse {
        return newsApiService.getNews(apiKey, query, category)
    }
}
