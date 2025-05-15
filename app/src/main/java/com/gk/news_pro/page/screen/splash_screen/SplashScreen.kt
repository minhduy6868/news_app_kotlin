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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
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
    // Animation for fade-in
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 200),
        label = "FadeIn"
    )

    // Animation for scaling
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 200),
        label = "Scale"
    )

    // Animation for subtle rotation
    val rotation by animateFloatAsState(
        targetValue = 360f,
        animationSpec = tween(durationMillis = 2000),
        label = "Rotation"
    )

    // Delay 2.5 seconds before navigating
    LaunchedEffect(Unit) {
        delay(2500)
        onSplashFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Deep Indigo
                        Color(0xFF3F51B5), // Indigo
                        Color(0xFF7986CB)  // Light Indigo
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_round),
            contentDescription = "Splash Logo",
            modifier = Modifier
                .fillMaxSize(0.6f)
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                }
                .rotate(rotation),
            contentScale = ContentScale.Fit
        )
    }
}