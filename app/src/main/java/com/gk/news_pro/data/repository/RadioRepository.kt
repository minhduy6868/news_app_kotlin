package com.gk.news_pro.data.repository

import android.util.Log
import com.gk.news_pro.data.api.RadioApiService
import com.gk.news_pro.data.model.Country
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.data.model.Tag
import com.gk.news_pro.page.utils.RetrofitClient

class RadioRepository {
    private val radioApiService: RadioApiService = RetrofitClient.radioRetrofit.create(RadioApiService::class.java)

    suspend fun getStations(country: String = "Vietnam", tag: String? = null): List<RadioStation> {
        return try {
            val stations = if (!tag.isNullOrEmpty()) {
                radioApiService.searchStations(
                    name = "",
                    country = country,
                    tag = tag,
                    limit = 100
                )
            } else {
                radioApiService.getStationsByCountry(country)
            }
            Log.d("RadioRepository", "Fetched ${stations.size} stations for country: $country, tag: $tag")
            stations.filter { it.url.isNotBlank() }
        } catch (e: Exception) {
            Log.e("RadioRepository", "Error fetching stations for country $country, tag $tag: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getStationsByTag(tag: String): List<RadioStation> {
        return try {
            val stations = radioApiService.getStationsByTag(tag)
            Log.d("RadioRepository", "Fetched ${stations.size} stations for tag: $tag")
            stations.filter { it.url.isNotBlank() }
        } catch (e: Exception) {
            Log.e("RadioRepository", "Error fetching stations for tag $tag: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getStationByUuid(uuid: String): RadioStation? {
        return try {
            val station = radioApiService.getStationByUuid(uuid).firstOrNull()
            if (station?.url?.isNotBlank() == true) {
                Log.d("RadioRepository", "Fetched station with UUID: $uuid")
                station
            } else {
                Log.w("RadioRepository", "Invalid or missing URL for station with UUID: $uuid")
                null
            }
        } catch (e: Exception) {
            Log.e("RadioRepository", "Error fetching station with UUID $uuid: ${e.message}", e)
            null
        }
    }

    suspend fun searchStations(query: String, country: String? = null, tag: String? = null, limit: Int = 100): List<RadioStation> {
        return try {
            val stations = radioApiService.searchStations(
                name = query,
                country = country ?: "",
                tag = tag ?: "",
                limit = limit
            )
            Log.d("RadioRepository", "Fetched ${stations.size} stations for query: $query, country: $country, tag: $tag")
            stations.filter { it.url.isNotBlank() }
        } catch (e: Exception) {
            Log.e("RadioRepository", "Error searching stations for query $query: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getCountries(): List<Country> {
        return try {
            val countries = radioApiService.getCountries()
            Log.d("RadioRepository", "Fetched ${countries.size} countries")
            countries
        } catch (e: Exception) {
            Log.e("RadioRepository", "Error fetching countries: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getTags(): List<Tag> {
        return try {
            val tags = radioApiService.getTags()
            Log.d("RadioRepository", "Fetched ${tags.size} tags")
            tags
        } catch (e: Exception) {
            Log.e("RadioRepository", "Error fetching tags: ${e.message}", e)
            emptyList()
        }
    }
}