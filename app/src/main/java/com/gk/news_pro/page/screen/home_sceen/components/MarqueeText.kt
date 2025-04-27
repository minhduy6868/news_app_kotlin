package com.gk.news_pro.page.screen.home_screen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gk.news_pro.data.model.News
@Composable
fun MarqueeText(
    newsList: List<News>,
    modifier: Modifier = Modifier
) {
    if (newsList.isEmpty()) {
        Text(
            text = "No news available",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        )
        return
    }

    // Lấy tối đa 3 bài viết có tiêu đề hợp lệ
    val topNews = newsList
        .filter { !it.title.isNullOrBlank() }
        .take(3)

    if (topNews.isEmpty()) {
        Text(
            text = "No valid titles available",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        )
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current

    // Animation để tạo hiệu ứng chạy ngang
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f, // Di chuyển từ phải sang trái
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Chuyển đổi bài viết khi animation hoàn thành
    LaunchedEffect(offset) {
        if (offset <= -1f) {
            currentIndex = (currentIndex + 1) % topNews.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        // Calculate the width based on text length
        val textWidth = remember { mutableStateOf(0f) }

        Text(
            text = topNews[currentIndex].title ?: "Untitled",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            onTextLayout = { textLayoutResult ->
                textWidth.value = textLayoutResult.size.width.toFloat()
            },
            modifier = Modifier
                .wrapContentWidth()
                .offset(x = with(density) { (offset * textWidth.value).toDp() })
        )
    }
}
