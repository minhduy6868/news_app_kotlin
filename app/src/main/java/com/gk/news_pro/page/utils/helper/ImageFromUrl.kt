package com.gk.news_pro.page.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

@Composable
fun ImageFromUrl(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    if (isLoading || hasError) {
        placeholder()
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
            onState = { state ->
                when (state) {
                    is AsyncImagePainter.State.Loading -> {
                        isLoading = true
                        hasError = false
                    }
                    is AsyncImagePainter.State.Error -> {
                        isLoading = false
                        hasError = true
                    }
                    is AsyncImagePainter.State.Empty -> {
                        isLoading = false
                        hasError = true
                    }
                    is AsyncImagePainter.State.Success -> {
                        isLoading = false
                        hasError = false
                    }
                }
            }
        )
    }
}