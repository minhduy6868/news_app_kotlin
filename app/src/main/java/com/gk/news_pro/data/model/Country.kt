package com.gk.news_pro.data.model


import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("name") val name: String,
    @SerializedName("iso_3166_1") val iso_3166_1: String,
    @SerializedName("stationcount") val stationcount: Int
)

data class Tag(
    @SerializedName("name") val name: String,
    @SerializedName("stationcount") val stationcount: Int
)