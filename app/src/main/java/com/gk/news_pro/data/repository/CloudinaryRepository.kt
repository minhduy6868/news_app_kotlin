package com.gk.news_pro.data.repository

import android.util.Log
import com.gk.news_pro.data.api.CloudinaryApiService
import com.gk.news_pro.data.api.CloudinaryResponse
import com.gk.news_pro.page.utils.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CloudinaryRepository(
    private val cloudName: String = "dwrmsia23", // Xác minh cloud_name
    private val uploadPreset: String = "alo123" // Thay bằng "fsiiigpo" nếu cần
) {
    private val cloudinaryApiService: CloudinaryApiService =
        RetrofitClient.cloudinaryRetrofit.create(CloudinaryApiService::class.java)

    suspend fun uploadImage(imageFile: File): String {
        try {
            // Validate file
            require(imageFile.exists() && imageFile.canRead()) { "Image file does not exist or is not readable" }

            // Create MultipartBody.Part for the image
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            // Call the API with unsigned upload
            val response = cloudinaryApiService.uploadImage(
                cloudName = cloudName,
                file = filePart,
                uploadPreset = uploadPreset
            )
            Log.d("Upload Anh", "up ảnh được : ${response.secureUrl}")
            return response.secureUrl;
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            throw CloudinaryUploadException("Failed to upload image: ${e.message}, $errorBody", e)
        } catch (e: Exception) {
            throw CloudinaryUploadException("Failed to upload image: ${e.message ?: "Unknown error"}", e)
        }
    }
}

class CloudinaryUploadException(message: String, cause: Throwable? = null) : Exception(message, cause)