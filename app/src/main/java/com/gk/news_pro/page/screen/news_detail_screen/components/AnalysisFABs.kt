package com.gk.news_pro.page.screen.news_detail_screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnalysisFABs(
    onContentAnalysis: () -> Unit,
    onUrlAnalysis: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExtendedFloatingActionButton(
            onClick = onContentAnalysis,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = "Phân tích AI") },
            text = { Text("Phân tích nội dung") }
        )

        ExtendedFloatingActionButton(
            onClick = onUrlAnalysis,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(imageVector = Icons.Default.Share, contentDescription = "Phân tích URL") },
            text = { Text("Phân tích từ URL") }
        )
    }
}