package com.gk.news_pro.data.api

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CloudinaryApiService {
    @Multipart
    @POST("v1_1/{cloud_name}/image/upload")
    suspend fun uploadImage(
        @Path("cloud_name") cloudName: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: String
    ): CloudinaryResponse
}

data class CloudinaryResponse(
    @SerializedName("secure_url") val secureUrl: String,
    @SerializedName("public_id") val publicId: String,
    @SerializedName("asset_id") val assetId: String
)