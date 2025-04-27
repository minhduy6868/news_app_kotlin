package com.gk.news_pro.page.screen.detail_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.gk.news_pro.R
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.repository.GeminiRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewsDetailScreen(
    navController: NavController,
    news: News,
    geminiRepository: GeminiRepository
) {
    val viewModel = remember { NewsDetailViewModel(geminiRepository) }
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(news) {
        viewModel.analyzeNewsContent(news)
    }

    Scaffold(
        topBar = { NewsDetailAppBar(navController, news) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        viewModel.analyzeNewsContent(news)
                        snackbarHostState.showSnackbar("Đang phân tích nội dung...")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Phân tích AI") },
                text = { Text("Phân tích AI") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = rememberImagePainter(
                    data = news.image_url ?: "",
                    builder = {
                        crossfade(true)
                        error(R.drawable.ic_launcher_foreground)
                    }
                ),
                contentDescription = news.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = news.title ?: "Không có tiêu đề",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                NewsMetaInfo(news)

                if (!news.category.isNullOrEmpty()) {
                    NewsCategories(news)
                }

                Text(
                    text = news.description ?: "Không có mô tả",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Justify
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                AICard(
                    isLoading = isLoading,
                    error = error,
                    analysis = aiAnalysis,
                    onRetry = { viewModel.analyzeNewsContent(news) }
                )

                NewsFullContent(news)

                Button(
                    onClick = {
                        news.link?.takeIf { it.isNotEmpty() }?.let { uriHandler.openUri(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    enabled = !news.link.isNullOrEmpty()
                ) {
                    Text("Đọc bài viết gốc")
                }
            }
        }
    }
}

@Composable
private fun NewsMetaInfo(news: News) {
    Column(
        modifier = Modifier.padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!news.source_name.isNullOrEmpty()) {
            MetaRow(Icons.Default.DateRange, "Nguồn", news.source_name)
        }
        if (!news.pubDate.isNullOrEmpty()) {
            MetaRow(Icons.Default.DateRange, "Ngày đăng", formatDate(news.pubDate))
        }
        if (!news.country.isNullOrEmpty()) {
            MetaRow(Icons.Default.Check, "Quốc gia", news.country.joinToString(", "))
        }
        if (!news.language.isNullOrEmpty()) {
            MetaRow(Icons.Default.Info, "Ngôn ngữ", news.language)
        }
    }
}

@Composable
private fun MetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewsCategories(news: News) {
    FlowRow(
        modifier = Modifier.padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        news.category.forEach { category ->
            FilterChip(
                selected = true,
                onClick = {},
                label = { Text(category.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

@Composable
private fun AICard(
    isLoading: Boolean,
    error: String?,
    analysis: String?,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "AI Analysis",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Phân tích AI",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        Text("Đang phân tích nội dung...")
                    }
                }

                !error.isNullOrEmpty() -> {
                    Column {
                        Text(
                            text = "Lỗi: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }

                !analysis.isNullOrEmpty() -> {
                    Text(
                        text = analysis,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Justify
                        )
                    )
                }

                else -> {
                    Text(
                        text = "Nhấn nút 'Phân tích AI' để đánh giá nội dung bài viết",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsFullContent(news: News) {
    Text(
        text = generateFullContent(news),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Justify
        ),
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsDetailAppBar(navController: NavController, news: News) {
    TopAppBar(
        title = {
            Text(
                text = news.title ?: "Chi tiết tin tức",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
            }
        }
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun generateFullContent(news: News): String {
    val builder = StringBuilder()
    builder.append(news.title ?: "")
    builder.append("\n\n")
    builder.append(news.description ?: "")
    builder.append("\n\n")
    builder.append(news.description ?: "")
    return builder.toString()
}
