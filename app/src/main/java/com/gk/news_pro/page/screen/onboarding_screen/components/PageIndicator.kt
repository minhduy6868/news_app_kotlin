package com.loc.newsapp.screen.onboarding_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gk.news_pro.ui.theme.BlueGray

@Composable
fun PageIndicator(
    modifier: Modifier = Modifier,
    pageSize: Int,
    selectPage: Int,
    selectColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = BlueGray
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Adds spacing between indicators
    ) {
        repeat(pageSize) { index ->
            Box(
                modifier = Modifier
                    .size(10.dp) // Set a fixed indicator size
                    .clip(CircleShape)
                    .background(color = if (index == selectPage) selectColor else unselectedColor)
            )
        }
    }
}
