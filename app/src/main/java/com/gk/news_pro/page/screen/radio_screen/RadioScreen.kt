package com.gk.news_pro.page.screen.radio_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gk.news_pro.data.model.Country
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.page.components.RadioCard
import com.gk.news_pro.data.model.Tag
import com.gk.news_pro.data.repository.RadioRepository
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import com.gk.news_pro.page.utils.ImageFromUrl
import com.gk.news_pro.page.utils.service.PlaybackState
import com.gk.news_pro.utils.MediaPlayerManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RadioScreen(
    userRepository: UserRepository,
    viewModel: RadioViewModel = viewModel(
        factory = ViewModelFactory(listOf(RadioRepository(), userRepository))
    ),
    onStationClick: (RadioStation) -> Unit = {}
) {
    val radioState by viewModel.radioState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val trendingStations by viewModel.trendingStations.collectAsState()
    val favoritedStations by viewModel.favoritedStations.collectAsState()
    val playingStation by viewModel.playingStation.collectAsState()
    val playbackState by MediaPlayerManager.getPlaybackState()?.collectAsState() ?: remember { mutableStateOf(PlaybackState.Idle) }
    val context = LocalContext.current
    var showLoginPrompt by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(radioState) {
        if (radioState is RadioUiState.Error) {
            val errorMessage = (radioState as RadioUiState.Error).message
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = "Thử lại",
                    duration = SnackbarDuration.Short
                ).let { result ->
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.retry()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    HeaderSection(
                        countries = countries,
                        selectedCountry = selectedCountry,
                        onCountrySelected = viewModel::selectCountry,
                        tags = tags,
                        selectedTag = selectedTag,
                        onTagSelected = viewModel::selectTag
                    )
                }

                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        onSearch = { viewModel.searchStations(searchQuery) },
                        onClear = viewModel::clearSearch
                    )
                }

                if (searchQuery.isEmpty()) {
                    item {
                        TrendingSection(
                            stations = trendingStations,
                            favoritedStations = favoritedStations,
                            playingStation = playingStation,
                            playbackState = playbackState,
                            onStationClick = onStationClick,
                            onPlayPauseClick = { station ->
                                if (playingStation == station && playbackState == PlaybackState.Playing) {
                                    viewModel.pauseStation()
                                } else {
                                    viewModel.playStation(context, station)
                                }
                            },
                            onFavoriteClick = viewModel::toggleFavoriteStation
                        )
                    }

                    when (radioState) {
                        is RadioUiState.Success -> {
                            val stationList = (radioState as RadioUiState.Success).stations
                            if (stationList.isEmpty()) {
                                item {
                                    EmptyContentMessage(message = "Không tìm thấy đài radio nào.")
                                }
                            } else {
                                items(stationList) { station ->
                                    RadioCard(
                                        station = station,
                                        isPlaying = playingStation == station && playbackState == PlaybackState.Playing,
                                        onClick = { onStationClick(station) },
                                        onPlayPauseClick = {
                                            if (playingStation == station && playbackState == PlaybackState.Playing) {
                                                viewModel.pauseStation()
                                            } else {
                                                viewModel.playStation(context, station)
                                            }
                                        },
                                        onFavoriteClick = { isFavorited ->
                                            if (!userRepository.isLoggedIn() && isFavorited) {
                                                showLoginPrompt = true
                                            } else {
                                                viewModel.toggleFavoriteStation(station, isFavorited)
                                            }
                                        },
                                        isFavorited = favoritedStations.contains(station)
                                    )
                                }
                            }
                        }
                        is RadioUiState.Loading -> {
                            item { LoadingIndicator() }
                        }
                        is RadioUiState.Error -> {
                            item {
                                ErrorMessage(
                                    message = (radioState as RadioUiState.Error).message,
                                    onRetry = viewModel::retry
                                )
                            }
                        }
                    }
                } else {
                    when (radioState) {
                        is RadioUiState.Success -> {
                            val results = (radioState as RadioUiState.Success).stations
                            if (results.isEmpty()) {
                                item { EmptySearchResults(query = searchQuery) }
                            } else {
                                items(results) { station ->
                                    RadioCard(
                                        station = station,
                                        isPlaying = playingStation == station && playbackState == PlaybackState.Playing,
                                        onClick = { onStationClick(station) },
                                        onPlayPauseClick = {
                                            if (playingStation == station && playbackState == PlaybackState.Playing) {
                                                viewModel.pauseStation()
                                            } else {
                                                viewModel.playStation(context, station)
                                            }
                                        },
                                        onFavoriteClick = { isFavorited ->
                                            if (!userRepository.isLoggedIn() && isFavorited) {
                                                showLoginPrompt = true
                                            } else {
                                                viewModel.toggleFavoriteStation(station, isFavorited)
                                            }
                                        },
                                        isFavorited = favoritedStations.contains(station)
                                    )
                                }
                            }
                        }
                        is RadioUiState.Loading -> {
                            item { LoadingIndicator() }
                        }
                        is RadioUiState.Error -> {
                            item {
                                ErrorMessage(
                                    message = (radioState as RadioUiState.Error).message,
                                    onRetry = { viewModel.searchStations(searchQuery) }
                                )
                            }
                        }
                    }
                }
            }

            if (showLoginPrompt) {
                AlertDialog(
                    onDismissRequest = { showLoginPrompt = false },
                    title = { Text("Yêu cầu đăng nhập", style = MaterialTheme.typography.titleMedium) },
                    text = { Text("Vui lòng đăng nhập để lưu đài radio yêu thích.", style = MaterialTheme.typography.bodyMedium) },
                    confirmButton = {
                        TextButton(onClick = { showLoginPrompt = false }) {
                            Text("Đóng")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

// Header Section with Country Dropdown and Tag Strip
@Composable
private fun HeaderSection(
    countries: List<Country>,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit,
    tags: List<Tag>,
    selectedTag: String,
    onTagSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CountryDropdown(
            countries = countries,
            selectedCountry = selectedCountry,
            onCountrySelected = onCountrySelected
        )
        TagStrip(
            tags = tags.map { it.name },
            selectedTag = selectedTag,
            onTagSelected = onTagSelected
        )
    }
}

// Country Dropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(
    countries: List<Country>,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    if (countries.isEmpty()) {
        LoadingIndicator(modifier = Modifier.height(56.dp))
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        OutlinedTextField(
            value = selectedCountry,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            label = { Text("Quốc gia", style = MaterialTheme.typography.bodyMedium) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                containerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            countries.forEach { country ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${country.name} (${country.stationcount})",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        onCountrySelected(country.name)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Tag Strip
@Composable
private fun TagStrip(
    tags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit
) {
    val tagColors = mapOf(
        "pop" to Color(0xFF0288D1),
        "rock" to Color(0xFFD81B60),
        "jazz" to Color(0xFF388E3C),
        "classical" to Color(0xFFFBC02D),
        "news" to Color(0xFF7B1FA2),
        "talk" to Color(0xFFE64A19),
        "sports" to Color(0xFF455A64)
    )

    if (tags.isEmpty()) {
        LoadingIndicator(modifier = Modifier.height(48.dp))
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Thể Loại",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(tags) { tag ->
                TagPill(
                    tag = tag,
                    isSelected = tag == selectedTag,
                    color = tagColors[tag] ?: MaterialTheme.colorScheme.primary,
                    onClick = { onTagSelected(tag) }
                )
            }
        }
    }
}

// Tag Pill
@Composable
private fun TagPill(
    tag: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) color else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else color

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .height(32.dp)
            .shadow(if (isSelected) 2.dp else 0.dp, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { if (isSelected) onClick() },
                    onTap = { onClick() }
                )
            },
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        border = if (!isSelected) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = tag.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Search Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            placeholder = {
                Text(
                    "Tìm kiếm đài radio...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                    focusManager.clearFocus()
                }
            )
        )
    }
}



@Composable
private fun TrendingSection(
    stations: List<RadioStation>,
    favoritedStations: List<RadioStation>,
    playingStation: RadioStation?,
    playbackState: PlaybackState,
    onStationClick: (RadioStation) -> Unit,
    onPlayPauseClick: (RadioStation) -> Unit,
    onFavoriteClick: (RadioStation, Boolean) -> Unit
) {
    if (stations.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Trending Stations",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(stations) { station ->
                TrendingRadioItem(
                    station = station,
                    isPlaying = playingStation == station && playbackState == PlaybackState.Playing,
                    isFavorited = favoritedStations.contains(station),
                    onClick = { onStationClick(station) },
                    onPlayPauseClick = { onPlayPauseClick(station) },
                    onFavoriteClick = { isFavorited -> onFavoriteClick(station, isFavorited) }
                )
            }
        }
    }
}

@Composable
private fun TrendingRadioItem(
    station: RadioStation,
    isPlaying: Boolean,
    isFavorited: Boolean,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onFavoriteClick(!isFavorited) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorited) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (!station.country.isNullOrEmpty()) {
                        Text(
                            text = station.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    if (!station.tags.isNullOrEmpty()) {
                        Text(
                            text = station.tags.split(",").joinToString(", ") { "#${it.trim()}" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Menu
                        else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Loading Indicator
@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
    }
}

// Error Message
@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Thử lại",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
            )
        }
    }
}

// Empty Search Results
@Composable
private fun EmptySearchResults(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "No results",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Không tìm thấy kết quả cho \"$query\"",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hãy thử từ khóa hoặc thể loại khác",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// Empty Content Message
@Composable
private fun EmptyContentMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "No content",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}