package com.gk.news_pro.page.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val NEWS_BASE_URL = "https://newsdata.io/api/1/"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val RADIO_BASE_URL = "https://de1.api.radio-browser.info/"
    private const val HEYGEN_BASE_URL = "https://api.heygen.com/"

    val newsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NEWS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val geminiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val radioRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RADIO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val heyGenRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(HEYGEN_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}