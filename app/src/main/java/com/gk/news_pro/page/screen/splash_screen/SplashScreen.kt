package com.gk.news_pro.page.screen.splash_screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import com.gk.news_pro.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinish: () -> Unit
) {
    // Hiệu ứng fade-in cho logo
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "FadeIn"
    )

    // Delay 2 giây trước khi chuyển màn hình
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Splash Logo",
            modifier = Modifier
                .fillMaxSize(0.5f)
                .graphicsLayer { this.alpha = alpha },
            contentScale = ContentScale.Fit
        )
    }
}