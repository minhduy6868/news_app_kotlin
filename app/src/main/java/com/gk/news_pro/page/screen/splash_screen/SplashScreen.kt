package com.gk.news_pro.page.screen.splash_screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Ease
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.gk.news_pro.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinish: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Animation for fade-in
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 300, easing = Ease),
        label = "FadeIn"
    )

    // Animation for scaling
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 200, easing = Ease),
        label = "Scale"
    )

    // Animation for zoom out effect
    val zoomScale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 1f,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        label = "ZoomOut"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        onSplashFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF1F8E9), // Very light green
                        Color(0xFFA0E7A5), // Light green
                        Color(0xFFE0F2F1), // Mint white
                        Color(0xFF84F08C), // Light green
                        Color(0xFFE0F7FA)  // Light cyan
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle gradient overlay for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x1A4CAF50), // Very subtle green
                            Color.Transparent,
                            Color(0x0F81C784)  // Very subtle green
                        ),
                        radius = 800f
                    )
                )
        )

        // Main logo with animations
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Splash Logo",
            modifier = Modifier
                .fillMaxSize(0.75f)
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale * zoomScale
                    this.scaleY = scale * zoomScale
                    this.shadowElevation = 8f
                },
            contentScale = ContentScale.Fit
        )
    }
}