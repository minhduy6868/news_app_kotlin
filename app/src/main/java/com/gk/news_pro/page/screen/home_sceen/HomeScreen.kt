package com.gk.news_pro.page.screen.home_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.NewsRepository
import com.gk.news_pro.page.components.NewsCard
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(NewsRepository())
    ),
    onNewsClick: (News) -> Unit = {}
) {
    val newsState by viewModel.newsState.collectAsState()
    val latestNews = remember { mutableStateOf<List<News>>(emptyList()) }
    val trendingNews = remember { mutableStateOf<List<News>>(emptyList()) }

    LaunchedEffect(newsState) {
        when (newsState) {
            is NewsUiState.Success -> {
                val allNews = (newsState as NewsUiState.Success).news
                latestNews.value = allNews.take(7)
                trendingNews.value = allNews
                    .sortedByDescending { it.title }
                    .take(3)
            }
            else -> {}
        }
    }

    Scaffold(
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting Section
            item {
                GreetingSection()
            }

            // Breaking News Banner
            if (latestNews.value.isNotEmpty()) {
                item {
                    BreakingNewsBanner(news = latestNews.value.first(), onNewsClick = onNewsClick)
                }
            }

            // Trending Section
            if (trendingNews.value.isNotEmpty()) {
                item {
                    SectionHeader(title = "Trending Now")
                }
                item {
                    TrendingNewsSection(newsList = trendingNews.value, onNewsClick = onNewsClick)
                }
            }

            // Latest News Section
            item {
                SectionHeader(title = "Latest News")
            }

            when (newsState) {
                is NewsUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                is NewsUiState.Success -> {
                    if (latestNews.value.isEmpty()) {
                        item {
                            EmptyState(message = "No news found")
                        }
                    } else {
                        items(latestNews.value) { news ->
                            NewsCard(
                                news = news,
                                onClick = { onNewsClick(news) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                                accentColor = MaterialTheme.colorScheme.primary,
                                cardHeight = 200.dp,
                                shadowElevation = 4.dp,
                                showBookmarkButton = false,
                                showCategoryBadge = true,
                                isTrending = trendingNews.value.contains(news)
                            )
                        }
                    }
                }

                is NewsUiState.Error -> {
                    item {
                        ErrorState(
                            message = (newsState as NewsUiState.Error).message,
                            onRetry = { viewModel.retry() }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
@Composable
private fun GreetingSection() {
    val greeting = getGreetingMessage()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // First line with greeting and icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            IconButton(
                onClick = { /* Handle profile click */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Subtitle with nice gradient effect
        Text(
            text = "Stay updated with today's news",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            ),
            modifier = Modifier.alpha(0.9f)
        )

        // Decorative divider
        Spacer(modifier = Modifier.height(12.dp))
        Divider(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}
private fun getGreetingMessage(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning!"
        in 12..16 -> "Good Afternoon!"
        in 17..23 -> "Good Evening!"
        else -> "Hello!"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "NewsPro",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 28.sp
                )
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier.shadow(4.dp)
    )
}

@Composable
private fun BreakingNewsBanner(news: News, onNewsClick: (News) -> Unit) {
    NewsCard(
        news = news,
        onClick = { onNewsClick(news) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        accentColor = MaterialTheme.colorScheme.primary,
        cardHeight = 220.dp,
        shadowElevation = 6.dp,
        showBookmarkButton = false,
        showCategoryBadge = true,
        isTrending = false
    )
}

@Composable
private fun TrendingNewsSection(newsList: List<News>, onNewsClick: (News) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(newsList) { news ->
            NewsCard(
                news = news,
                onClick = { onNewsClick(news) },
                modifier = Modifier.width(300.dp),
                accentColor = MaterialTheme.colorScheme.primary,
                cardHeight = 180.dp,
                shadowElevation = 4.dp,
                showBookmarkButton = false,
                showCategoryBadge = true,
                isTrending = true
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Empty",
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
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
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}