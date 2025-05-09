package com.gk.news_pro.data.api

import com.gk.news_pro.data.model.Country
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.data.model.Tag
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RadioApiService {
    @GET("json/stations/bycountry/{country}")
    suspend fun getStationsByCountry(
        @Path("country") country: String,
        @Query("limit") limit: Int = 100
    ): List<RadioStation>

    @GET("json/stations/bytag/{tag}")
    suspend fun getStationsByTag(
        @Path("tag") tag: String,
        @Query("limit") limit: Int = 100
    ): List<RadioStation>

    @GET("json/stations/byuuid")
    suspend fun getStationByUuid(
        @Query("uuid") uuid: String
    ): List<RadioStation>

    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String,
        @Query("country") country: String = "",
        @Query("tag") tag: String = "",
        @Query("limit") limit: Int = 100
    ): List<RadioStation>

    @GET("json/countries")
    suspend fun getCountries(): List<Country>

    @GET("json/tags")
    suspend fun getTags(): List<Tag>
}