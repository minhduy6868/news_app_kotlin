package com.gk.news_pro.page.screen.favorite_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class FavoriteViewModel(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _favoriteNews = MutableStateFlow<List<News>>(emptyList())
    val favoriteNews: StateFlow<List<News>> = _favoriteNews

    private val _favoriteRadioStations = MutableStateFlow<List<RadioStation>>(emptyList())
    val favoriteRadioStations: StateFlow<List<RadioStation>> = _favoriteRadioStations

    init {
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d("FavoriteViewModel", "Auth state changed: User logged in, UID: ${user.uid}")
                loadFavorites(user.uid)
            } else {
                Log.d("FavoriteViewModel", "Auth state changed: No user logged in")
                _favoriteNews.value = emptyList()
                _favoriteRadioStations.value = emptyList()
            }
        }
    }

    private fun loadFavorites(uid: String) {
        viewModelScope.launch {
            try {
                if (userRepository.isLoggedIn()) {
                    // Load favorite news
                    val newsList = userRepository.getFavoriteNewsList()
                    _favoriteNews.value = newsList
                    Log.d("FavoriteViewModel", "Loaded ${newsList.size} favorite news for UID: $uid")
                    Log.d("FavoriteViewModel", "Loaded ${newsList} ")

                    // Load favorite radio stations
                    val radioList = userRepository.getFavoriteRadioStations()
                    _favoriteRadioStations.value = radioList
                    Log.d("FavoriteViewModel", "Loaded ${radioList.size} favorite radio stations for UID: $uid")
                } else {
                    Log.d("FavoriteViewModel", "User not logged in, no favorites loaded")
                    _favoriteNews.value = emptyList()
                    _favoriteRadioStations.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error loading favorites for UID: $uid: ${e.message}", e)
                _favoriteNews.value = emptyList()
                _favoriteRadioStations.value = emptyList()
            }
        }
    }

    fun removeFavoriteNews(newsId: String) {
        viewModelScope.launch {
            try {
                userRepository.removeFavoriteNews(newsId)
                _favoriteNews.value = _favoriteNews.value.filter { it.article_id != newsId }
                Log.d("FavoriteViewModel", "Removed news with ID: $newsId")
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error removing favorite news: ${e.message}", e)
            }
        }
    }

    fun removeFavoriteRadioStation(stationId: String) {
        viewModelScope.launch {
            try {
                userRepository.removeFavoriteRadioStation(stationId)
                _favoriteRadioStations.value = _favoriteRadioStations.value.filter { it.stationuuid != stationId }
                Log.d("FavoriteViewModel", "Removed radio station with ID: $stationId")
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error removing favorite radio station: ${e.message}", e)
            }
        }
    }

    fun refreshFavorites() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            loadFavorites(uid)
        } else {
            Log.d("FavoriteViewModel", "Cannot refresh favorites: No user logged in")
            _favoriteNews.value = emptyList()
            _favoriteRadioStations.value = emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("FavoriteViewModel", "ViewModel cleared")
    }
}