package com.gk.news_pro.page.screen.radio_screen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.page.screen.radio_screen.RadioViewModel
import com.gk.news_pro.page.utils.ImageFromUrl
import com.gk.news_pro.page.utils.service.PlaybackState
import com.gk.news_pro.utils.MediaPlayerManager

@Composable
fun MiniPlayer(
    viewModel: RadioViewModel,
    onStationClick: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val playingStation by viewModel.playingStation.collectAsState()
    val playbackState by MediaPlayerManager.getPlaybackState()?.collectAsState() ?: return

    if (playingStation == null || playbackState == PlaybackState.Idle) return

    Row(
        modifier = modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { playingStation?.let { onStationClick(it) } }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Marquee text for long titles
        MarqueeText(
            text = playingStation?.name ?: "Unknown Station",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.widthIn(max = 80.dp)
        )

        // Circular image with play/pause button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ImageFromUrl(
                url = playingStation?.favicon ?: "",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                placeholder = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                    )
                }
            )
            IconButton(
                onClick = {
                    if (playbackState == PlaybackState.Playing) {
                        viewModel.pauseStation()
                    } else {
                        viewModel.resumeStation()
                    }
                },
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (playbackState == PlaybackState.Playing) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = if (playbackState == PlaybackState.Playing) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MarqueeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val textWidth = remember { mutableStateOf(0f) }
    val containerWidth = remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val offset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -textWidth.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (textWidth.value * 20).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset((textWidth.value * 10).toInt())
        )
    )

    Box(
        modifier = modifier
            .onSizeChanged { containerWidth.value = it.width.toFloat() }
    ) {
        Text(
            text = text,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier
                .offset(x = if (textWidth.value > containerWidth.value) offset.value.dp else 0.dp)
                .onSizeChanged { textWidth.value = it.width.toFloat() }
        )
    }
}