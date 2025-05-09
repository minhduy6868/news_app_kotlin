package com.gk.news_pro.page.screen.news_detail_screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UrlAnalysisDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onAnalyze: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Phân tích bài viết từ URL") },
            text = {
                Column {
                    OutlinedTextField(
                        value = url,
                        onValueChange = {
                            url = it
                            isError = it.isBlank() || !it.startsWith("http")
                        },
                        label = { Text("URL bài viết") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text("Vui lòng nhập URL hợp lệ (bắt đầu bằng http:// hoặc https://)")
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nhập URL của bài báo để phân tích nội dung",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                            onAnalyze(url)
                            onDismiss()
                        } else {
                            isError = true
                        }
                    }
                ) {
                    Text("Phân tích")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Hủy")
                }
            }
        )
    }
}