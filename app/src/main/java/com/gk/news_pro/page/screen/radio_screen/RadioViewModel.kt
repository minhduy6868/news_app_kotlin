package com.gk.news_pro.page.screen.radio_screen

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.Country
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.data.model.Tag
import com.gk.news_pro.data.repository.RadioRepository
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.utils.MediaPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RadioViewModel(
    private val radioRepository: RadioRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _radioState = MutableStateFlow<RadioUiState>(RadioUiState.Loading)
    val radioState: StateFlow<RadioUiState> = _radioState

    private val _trendingStations = MutableStateFlow<List<RadioStation>>(emptyList())
    val trendingStations: StateFlow<List<RadioStation>> = _trendingStations

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCountry = MutableStateFlow("Vietnam")
    val selectedCountry: StateFlow<String> = _selectedCountry

    private val _selectedTag = MutableStateFlow("")
    val selectedTag: StateFlow<String> = _selectedTag

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags

    private val _favoritedStations = MutableStateFlow<List<RadioStation>>(emptyList())
    val favoritedStations: StateFlow<List<RadioStation>> = _favoritedStations

    private val _playingStation = MutableStateFlow<RadioStation?>(null)
    val playingStation: StateFlow<RadioStation?> = _playingStation

    init {
        fetchCountries()
        fetchTags()
        fetchGeneralStations()
        fetchTrendingStations()
        fetchFavoriteStations()
    }

    fun bindService(context: Context) {
        MediaPlayerManager.bindService(context)
    }

    fun unbindService(context: Context) {
        MediaPlayerManager.unbindService(context)
    }

    fun playStation(context: Context, station: RadioStation) {
        if (station.url.isBlank()) {
            _radioState.value = RadioUiState.Error("Invalid station URL")
            Log.e("RadioViewModel", "Invalid URL for station: ${station.name}")
            return
        }
        if (!station.url.startsWith("http://") && !station.url.startsWith("https://")) {
            _radioState.value = RadioUiState.Error("Unsupported URL protocol")
            Log.e("RadioViewModel", "Unsupported URL: ${station.url}")
            return
        }
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        if (activeNetwork == null || !activeNetwork.isConnected) {
            _radioState.value = RadioUiState.Error("No internet connection")
            Log.e("RadioViewModel", "No internet connection")
            return
        }
        Log.d("RadioViewModel", "Attempting to play URL: ${station.url}")
        val supportedExtensions = listOf("m3u8", "mpd", "mp3", "aac")
        if (supportedExtensions.none { station.url.endsWith(it, ignoreCase = true) }) {
            Log.w("RadioViewModel", "URL may not be a supported streaming format: ${station.url}")
        }
        _playingStation.value = station
        MediaPlayerManager.initialize(
            context,
            station.url,
            station,
            onPrepared = { Log.d("RadioViewModel", "Station ${station.name} is playing") },
            onError = { error ->
                val userFriendlyError = when {
                    error.contains("No suitable media source factory") -> {
                        "This radio station is not supported. Please try another station."
                    }
                    else -> error
                }
                _radioState.value = RadioUiState.Error(userFriendlyError)
                Log.e("RadioViewModel", error)
            }
        )
    }

    fun pauseStation() {
        MediaPlayerManager.pause()
        Log.d("RadioViewModel", "Station paused")
    }

    fun resumeStation() {
        MediaPlayerManager.resume()
        Log.d("RadioViewModel", "Station resumed")
    }

    fun stopStation() {
        MediaPlayerManager.stop()
        _playingStation.value = null
        Log.d("RadioViewModel", "Station stopped")
    }

    fun fetchCountries() {
        viewModelScope.launch {
            try {
                val countries = radioRepository.getCountries()
                _countries.value = countries
                Log.d("RadioViewModel", "Fetched ${countries.size} countries")
            } catch (e: Exception) {
                Log.e("RadioViewModel", "Error fetching countries: ${e.message}", e)
            }
        }
    }

    fun fetchTags() {
        viewModelScope.launch {
            try {
                val tags = radioRepository.getTags()
                _tags.value = tags
                Log.d("RadioViewModel", "Fetched ${tags.size} tags")
            } catch (e: Exception) {
                Log.e("RadioViewModel", "Error fetching tags: ${e.message}", e)
            }
        }
    }

    fun fetchGeneralStations() {
        _radioState.value = RadioUiState.Loading
        viewModelScope.launch {
            try {
                val stations = radioRepository.getStations(
                    country = _selectedCountry.value,
                    tag = _selectedTag.value
                )
                stations.forEach { station ->
                    Log.d("RadioViewModel", "Station: ${station.name}, URL: ${station.url}")
                }
                _radioState.value = RadioUiState.Success(stations)
                Log.d("RadioViewModel", "Fetched ${stations.size} general stations")
            } catch (e: Exception) {
                _radioState.value = RadioUiState.Error(e.message ?: "Failed to load stations")
                Log.e("RadioViewModel", "Error fetching general stations: ${e.message}", e)
            }
        }
    }

    fun fetchTrendingStations() {
        viewModelScope.launch {
            try {
                val stations = radioRepository.getStations(country = _selectedCountry.value)
                stations.forEach { station ->
                    Log.d("RadioViewModel", "Trending Station: ${station.name}, URL: ${station.url}")
                }
                _trendingStations.value = stations.sortedByDescending { it.clickcount ?: 0 }.take(5)
                Log.d("RadioViewModel", "Fetched ${stations.size} trending stations, limited to 5")
            } catch (e: Exception) {
                _trendingStations.value = emptyList()
                Log.e("RadioViewModel", "Error fetching trending stations: ${e.message}", e)
            }
        }
    }

    fun fetchFavoriteStations() {
        viewModelScope.launch {
            try {
                val stations = userRepository.getFavoriteRadioStations()
                _favoritedStations.value = stations
                Log.d("RadioViewModel", "Fetched ${stations.size} favorite stations")
            } catch (e: Exception) {
                _favoritedStations.value = emptyList()
                Log.e("RadioViewModel", "Error fetching favorite stations: ${e.message}", e)
            }
        }
    }

    fun toggleFavoriteStation(station: RadioStation, isFavorited: Boolean) {
        viewModelScope.launch {
            try {
                if (isFavorited) {
                    userRepository.saveFavoriteRadioStation(station)
                    _favoritedStations.value = _favoritedStations.value + station
                    Log.d("RadioViewModel", "Added station ${station.stationuuid} to favorites")
                } else {
                    userRepository.removeFavoriteRadioStation(station.stationuuid)
                    _favoritedStations.value = _favoritedStations.value.filter { it.stationuuid != station.stationuuid }
                    Log.d("RadioViewModel", "Removed station ${station.stationuuid} from favorites")
                }
            } catch (e: Exception) {
                _radioState.value = RadioUiState.Error("Failed to update favorite: ${e.message}")
                Log.e("RadioViewModel", "Error toggling favorite for station ${station.stationuuid}: ${e.message}", e)
            }
        }
    }

    fun selectCountry(country: String) {
        _selectedCountry.value = country
        fetchGeneralStations()
        fetchTrendingStations()
        Log.d("RadioViewModel", "Selected country: $country")
    }

    fun selectTag(tag: String) {
        _selectedTag.value = if (_selectedTag.value == tag) "" else tag
        fetchGeneralStations()
        Log.d("RadioViewModel", "Selected tag: ${_selectedTag.value}")
    }

    fun searchStations(query: String) {
        _radioState.value = RadioUiState.Loading
        viewModelScope.launch {
            try {
                val stations = radioRepository.searchStations(
                    query = query,
                    country = _selectedCountry.value,
                    tag = _selectedTag.value
                )
                stations.forEach { station ->
                    Log.d("RadioViewModel", "Search Result: ${station.name}, URL: ${station.url}")
                }
                _radioState.value = RadioUiState.Success(stations)
                Log.d("RadioViewModel", "Fetched ${stations.size} stations for query $query")
            } catch (e: Exception) {
                _radioState.value = RadioUiState.Error(e.message ?: "Failed to search stations")
                Log.e("RadioViewModel", "Error searching stations for query $query: ${e.message}", e)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        Log.d("RadioViewModel", "Updated search query: $query")
    }

    fun clearSearch() {
        _searchQuery.value = ""
        fetchGeneralStations()
        Log.d("RadioViewModel", "Cleared search query")
    }

    fun retry() {
        if (searchQuery.value.isNotEmpty()) {
            searchStations(searchQuery.value)
        } else {
            fetchGeneralStations()
        }
        Log.d("RadioViewModel", "Retrying with query: ${searchQuery.value}")
    }

    override fun onCleared() {
        super.onCleared()
        MediaPlayerManager.stop()
    }
}

sealed class RadioUiState {
    object Loading : RadioUiState()
    data class Success(val stations: List<RadioStation>) : RadioUiState()
    data class Error(val message: String) : RadioUiState()
}