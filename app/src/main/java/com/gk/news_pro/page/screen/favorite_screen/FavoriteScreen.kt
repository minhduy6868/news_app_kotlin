package com.gk.news_pro.page.screen.favorite_screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.components.NewsCard
import com.gk.news_pro.page.components.RadioCard
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import com.gk.news_pro.page.screen.radio_screen.RadioViewModel
import com.gk.news_pro.page.screen.radio_screen.RadioUiState
import com.gk.news_pro.utils.MediaPlayerManager
import com.gk.news_pro.page.utils.service.PlaybackState
import kotlinx.coroutines.launch

@Composable
fun FavoriteScreen(
    userRepository: UserRepository,
    favoriteViewModel: FavoriteViewModel = viewModel(factory = ViewModelFactory(userRepository)),
    radioViewModel: RadioViewModel = viewModel(factory = ViewModelFactory(userRepository)),
    onNewsClick: (News) -> Unit = {},
    onRadioClick: (RadioStation) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteNews by favoriteViewModel.favoriteNews.collectAsState()
    val favoriteRadioStations by favoriteViewModel.favoriteRadioStations.collectAsState()
    val playingStation by radioViewModel.playingStation.collectAsState()
    val playbackState by MediaPlayerManager.getPlaybackState()?.collectAsState()
        ?: remember { mutableStateOf(PlaybackState.Idle) }
    val radioState by radioViewModel.radioState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("News", "Radio")

    // Refresh favorite news and radio stations when screen is loaded
    LaunchedEffect(Unit) {
        //favoriteViewModel.refreshFavoriteNews()
    }

    // Bind MediaPlayerManager service
    LaunchedEffect(Unit) {
        Log.d("FavoriteScreen", "Binding MediaPlayerManager service")
        try {
            radioViewModel.bindService(context)
        } catch (e: Exception) {
            Log.e("FavoriteScreen", "Error binding service: ${e.message}", e)
            scope.launch {
                snackbarHostState.showSnackbar("Failed to initialize playback service")
            }
        }
    }

    // Unbind service when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            Log.d("FavoriteScreen", "Unbinding MediaPlayerManager service")
            try {
                radioViewModel.unbindService(context)
            } catch (e: Exception) {
                Log.e("FavoriteScreen", "Error unbinding service: ${e.message}", e)
            }
        }
    }

    // Show error messages from radioState
    LaunchedEffect(radioState) {
        if (radioState is RadioUiState.Error) {
            val errorMessage = (radioState as RadioUiState.Error).message
            Log.e("FavoriteScreen", "Radio error: $errorMessage")
            scope.launch {
                snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // TabRow for switching between News and Radio
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                Log.d("FavoriteScreen", "Switching to tab: $title")
                                selectedTab = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            modifier = Modifier
                                .background(
                                    if (selectedTab == index)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else
                                        Color.Transparent
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on selected tab
                when (selectedTab) {
                    0 -> { // News Tab
                        when {
                            favoriteNews.isEmpty() -> {
                                EmptyFavoriteScreen(message = "Chưa có tin tức yêu thích")
                            }
                            else -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "Tin tức yêu thích",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp
                                            ),
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                    items(favoriteNews) { news ->
                                        NewsCard(
                                            news = news,
                                            onClick = { onNewsClick(news) },
                                            onBookmarkClick = { isBookmarked ->
                                                if (!isBookmarked) {
                                                    favoriteViewModel.removeFavoriteNews(news.article_id)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            accentColor = MaterialTheme.colorScheme.secondary,
                                            cardHeight = 220.dp,
                                            shadowElevation = 6.dp,
                                            showBookmarkButton = true,
                                            isBookmarked = true,
                                            showCategoryBadge = true,
                                            isTrending = false
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Radio Tab
                        when {
                            favoriteRadioStations.isEmpty() -> {
                                EmptyFavoriteScreen(message = "Chưa có đài radio yêu thích")
                            }
                            else -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "Đài radio yêu thích",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp
                                            ),
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                    items(favoriteRadioStations) { station ->
                                        // Check for valid station data
                                        if (station.stationuuid.isBlank() || station.name.isBlank()) {
                                            Log.w("FavoriteScreen", "Invalid station data: uuid=${station.stationuuid}, name=${station.name}")
                                            ErrorRadioCard(
                                                message = "Invalid station data",
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            RadioCard(
                                                station = station,
                                                isPlaying = playingStation?.stationuuid == station.stationuuid && playbackState == PlaybackState.Playing,
                                                onClick = {
                                                    Log.d("FavoriteScreen", "Clicked station: ${station.name}, URL: ${station.url}")
                                                    try {
                                                        onRadioClick(station)
                                                    } catch (e: Exception) {
                                                        Log.e("FavoriteScreen", "Error in onRadioClick: ${e.message}", e)
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Failed to handle station click")
                                                        }
                                                    }
                                                },
                                                onPlayPauseClick = {
                                                    Log.d("FavoriteScreen", "Play/Pause clicked for station: ${station.name}, Current state: $playbackState")
                                                    try {
                                                        if (playingStation?.stationuuid == station.stationuuid && playbackState == PlaybackState.Playing) {
                                                            radioViewModel.pauseStation()
                                                        } else {
                                                            radioViewModel.playStation(context, station)
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("FavoriteScreen", "Error in play/pause: ${e.message}", e)
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Failed to play/pause station")
                                                        }
                                                    }
                                                },
                                                onFavoriteClick = { isFavorited ->
                                                    Log.d("FavoriteScreen", "Favorite clicked for station: ${station.name}, isFavorited: $isFavorited")
                                                    try {
                                                        radioViewModel.toggleFavoriteStation(station, isFavorited)
                                                        if (!isFavorited) {
                                                            favoriteViewModel.removeFavoriteRadioStation(station.stationuuid)
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("FavoriteScreen", "Error toggling favorite: ${e.message}", e)
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Failed to update favorite")
                                                        }
                                                    }
                                                },
                                                isFavorited = favoriteRadioStations.any { it.stationuuid == station.stationuuid },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoriteScreen(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No favorites",
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Thêm mục yêu thích từ trang khám phá hoặc radio",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorRadioCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}