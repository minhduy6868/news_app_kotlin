package com.gk.news_pro.page.utils.service

interface CloudinaryApiService {
    suspend fun uploadImage(imagePath: String, publicId: String): String?
}