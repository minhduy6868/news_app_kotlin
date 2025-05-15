package com.gk.news_pro.page.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val NEWS_BASE_URL = "https://newsdata.io/api/1/"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val RADIO_BASE_URL = "https://de1.api.radio-browser.info/"
    private const val HEYGEN_BASE_URL = "https://api.heygen.com/"
    private const val CLOUDINARY_BASE_URL = "https://api.cloudinary.com/"

    // Shared OkHttpClient with timeouts
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val newsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NEWS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val geminiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val radioRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RADIO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val heyGenRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(HEYGEN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val cloudinaryRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(CLOUDINARY_BASE_URL)
            .client(okHttpClient) // Using the shared client with timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}