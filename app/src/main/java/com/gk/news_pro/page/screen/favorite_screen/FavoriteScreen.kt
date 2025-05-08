package com.gk.news_pro.page.screen.favorite_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.components.NewsCard
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory

@Composable
fun FavoriteScreen(
    userRepository: UserRepository,
    viewModel: FavoriteViewModel = viewModel(factory = ViewModelFactory(userRepository)),
    onNewsClick: (News) -> Unit = {}
) {
    val favoriteNews by viewModel.favoriteNews.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            favoriteNews.isEmpty() -> {
                EmptyFavoriteScreen()
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
                                fontSize = 30.sp
                            ),
                            modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
                        )
                    }
                    items(favoriteNews) { news ->
                        NewsCard(
                            news = news,
                            onClick = { onNewsClick(news) },
                            onBookmarkClick = { isBookmarked ->
                                if (!isBookmarked) {
                                    viewModel.removeFavoriteNews(news.article_id)
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
}

@Composable
private fun EmptyFavoriteScreen() {
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
            text = "Chưa có tin tức yêu thích",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Thêm tin tức yêu thích từ trang khám phá",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}