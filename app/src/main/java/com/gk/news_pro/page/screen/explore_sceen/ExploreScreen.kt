package com.gk.news_pro.page.screen.explore_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.page.components.NewsCard
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import com.gk.news_pro.page.screen.explore_sceen.ExploreUiState
import com.gk.news_pro.page.screen.explore_sceen.ExploreViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel(
        factory = ViewModelFactory(NewsRepository())
    ),
    onNewsClick: (News) -> Unit = {},
    onBookmarkClick: (News, Boolean) -> Unit = { _, _ -> }
) {
    val newsState by viewModel.newsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = viewModel.categories
    val trendingNews by viewModel.trendingNews.collectAsState()
    val bookmarkedNews by viewModel.bookmarkedNews.collectAsState()

    Scaffold(

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Search Bar
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        onSearch = { viewModel.searchNews(searchQuery) },
                        onClear = viewModel::clearSearch
                    )
                }

                if (searchQuery.isEmpty()) {
                    // Trending Section
                    item {
                        TrendingSection(
                            news = trendingNews,
                            bookmarkedNews = bookmarkedNews,
                            onNewsClick = onNewsClick,
                            onBookmarkClick = onBookmarkClick
                        )
                    }

                    // Categories Section
                    item {
                        CategoryStrip(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = viewModel::fetchNewsByCategory
                        )
                    }

                    // News Content
                    when (newsState) {
                        is ExploreUiState.Success -> {
                            val newsList = (newsState as ExploreUiState.Success).news
                            items(newsList) { news ->
                                NewsCard(
                                    news = news,
                                    onClick = { onNewsClick(news) },
                                    onBookmarkClick = { isBookmarked -> onBookmarkClick(news, isBookmarked) },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    accentColor = MaterialTheme.colorScheme.secondary,
                                    cardHeight = 220.dp,
                                    shadowElevation = 6.dp,
                                    showBookmarkButton = true,
                                    isBookmarked = bookmarkedNews.contains(news),
                                    showCategoryBadge = true,
                                    isTrending = false
                                )
                            }
                        }
                        is ExploreUiState.Loading -> {
                            item { LoadingIndicator() }
                        }
                        is ExploreUiState.Error -> {
                            item {
                                ErrorMessage(
                                    message = (newsState as ExploreUiState.Error).message,
                                    onRetry = viewModel::retry
                                )
                            }
                        }
                    }
                } else {
                    // Search Results
                    when (newsState) {
                        is ExploreUiState.Success -> {
                            val results = (newsState as ExploreUiState.Success).news
                            if (results.isEmpty()) {
                                item { EmptySearchResults(query = searchQuery) }
                            } else {
                                items(results) { news ->
                                    NewsCard(
                                        news = news,
                                        onClick = { onNewsClick(news) },
                                        onBookmarkClick = { isBookmarked -> onBookmarkClick(news, isBookmarked) },
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        accentColor = MaterialTheme.colorScheme.secondary,
                                        cardHeight = 220.dp,
                                        shadowElevation = 6.dp,
                                        showBookmarkButton = true,
                                        isBookmarked = bookmarkedNews.contains(news),
                                        showCategoryBadge = true,
                                        isTrending = false
                                    )
                                }
                            }
                        }
                        is ExploreUiState.Loading -> {
                            item { LoadingIndicator() }
                        }
                        is ExploreUiState.Error -> {
                            item {
                                ErrorMessage(
                                    message = (newsState as ExploreUiState.Error).message,
                                    onRetry = { viewModel.searchNews(searchQuery) }
                                )
                            }
                        }
                    }
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            placeholder = {
                Text(
                    "Explore news, topics, or sources...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.secondary
            ),
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
    news: List<News>,
    bookmarkedNews: List<News>,
    onNewsClick: (News) -> Unit,
    onBookmarkClick: (News, Boolean) -> Unit
) {
    if (news.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "What's Hot",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(news) { trendingNews ->
                NewsCard(
                    news = trendingNews,
                    onClick = { onNewsClick(trendingNews) },
                    onBookmarkClick = { isBookmarked -> onBookmarkClick(trendingNews, isBookmarked) },
                    modifier = Modifier
                        .width(300.dp),
                    accentColor = MaterialTheme.colorScheme.secondary,
                    cardHeight = 200.dp,
                    shadowElevation = 8.dp,
                    showBookmarkButton = true,
                    isBookmarked = bookmarkedNews.contains(trendingNews),
                    showCategoryBadge = true,
                    isTrending = true
                )
            }
        }
    }
}

@Composable
private fun CategoryStrip(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categoryColors = mapOf(
        "general" to Color(0xFF0288D1),
        "business" to Color(0xFF388E3C),
        "entertainment" to Color(0xFFD81B60),
        "health" to Color(0xFFFBC02D),
        "science" to Color(0xFF7B1FA2),
        "sports" to Color(0xFFE64A19),
        "technology" to Color(0xFF455A64)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Discover Topics",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(categories) { category ->
                val color = categoryColors[category] ?: MaterialTheme.colorScheme.secondary

                CategoryPill(
                    category = category,
                    isSelected = category == selectedCategory,
                    color = color,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(
    category: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) color else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else color

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .height(40.dp)
            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected) BorderStroke(1.5.dp, color.copy(alpha = 0.4f)) else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = contentColor,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(56.dp),
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
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
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp
            )
        ) {
            Text(
                "Try Again",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 16.sp
            )
        }
    }
}

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
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No results found for",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "\"$query\"",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Try different keywords or topics",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
 public fun formatRelativeTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return ""

        val now = System.currentTimeMillis()
        val diff = now - date.time

        when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            diff < 48 * 60 * 60 * 1000 -> "Yesterday"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        dateString
    }
}