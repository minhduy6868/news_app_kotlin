package com.gk.news_pro.page.screen.offline_list_news_screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavController
import com.gk.news_pro.data.local.AppDatabase
import com.gk.news_pro.data.local.entity.NewsEntity
import com.gk.news_pro.data.local.entity.toNews
import com.gk.news_pro.data.model.News
import com.gk.news_pro.page.components.NewsCard
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import com.gk.news_pro.page.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineListNewsScreen(
    navController: NavController,
    onNewsClick: (News) -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val viewModel: OfflineListNewsViewModel = viewModel(factory = ViewModelFactory(database))
    val savedNews by viewModel.savedNews.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Refresh saved news when screen is loaded
    LaunchedEffect(Unit) {
        viewModel.refreshSavedNews()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tin tức đã lưu") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Account.route) {
                            popUpTo(Screen.OfflineNews.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Account"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp),
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        action = {
                            data.visuals.actionLabel?.let { actionLabel ->
                                TextButton(onClick = { data.performAction() }) {
                                    Text(
                                        text = actionLabel,
                                        color = MaterialTheme.colorScheme.inversePrimary
                                    )
                                }
                            }
                        }
                    ) {
                        Text(text = data.visuals.message)
                    }
                }
            )
        },
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
                when {
                    savedNews.isEmpty() -> {
                        EmptySavedNewsScreen(message = "Chưa có tin tức đã lưu")
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "Tin tức đã lưu",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                            items(savedNews) { newsEntity ->
                                NewsCard(
                                    news = newsEntity.toNews(),
                                    onClick = { onNewsClick(newsEntity.toNews()) },
                                    onBookmarkClick = { isBookmarked ->
                                        if (!isBookmarked) {
                                            scope.launch {
                                                viewModel.removeSavedNews(newsEntity.link)
                                                snackbarHostState.showSnackbar(
                                                    message = "Đã xóa bài báo khỏi danh sách lưu",
                                                    withDismissAction = true,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    accentColor = MaterialTheme.colorScheme.secondary,
                                    cardHeight = 220.dp,
                                    shadowElevation = 6.dp,
                                    showBookmarkButton = true,
                                    isBookmarked = true,
                                    showCategoryBadge = false,
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
    }
}

@Composable
private fun EmptySavedNewsScreen(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No saved news",
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
            text = "Lưu bài báo từ trang chi tiết để xem sau",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}