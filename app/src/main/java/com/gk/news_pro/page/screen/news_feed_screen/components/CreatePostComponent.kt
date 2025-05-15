package com.gk.news_pro.page.screen.create_post

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreatePostComponent(
    createPostViewModel: CreatePostViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val maxImages = 5
    val uiState by createPostViewModel.uiState.collectAsState()

    // State for post creation
    var content by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var tempImageFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var showConfirmDialog by remember { mutableStateOf(-1) } // Index of image to remove
    var focused by remember { mutableStateOf(false) }

    // FocusRequester for TextField
    val focusRequester = remember { FocusRequester() }

    // Animation states
    var buttonClicked by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonClicked) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 150)
    )

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            scope.launch {
                snackbarHostState.showSnackbar("Cần quyền truy cập để chọn ảnh")
            }
        }
    }

    // Image picker launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val remainingSlots = maxImages - selectedImageUris.size
            if (uris.size > remainingSlots) {
                scope.launch {
                    snackbarHostState.showSnackbar("Chỉ được chọn tối đa $maxImages ảnh")
                }
                return@rememberLauncherForActivityResult
            }
            val newUris = selectedImageUris + uris
            val newFiles = mutableListOf<File>()
            uris.forEach { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = createTempImageFile(context)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                newFiles.add(file)
            }
            selectedImageUris = newUris
            tempImageFiles = tempImageFiles + newFiles
        }
    }

    // Confirmation dialog for removing images
    if (showConfirmDialog >= 0) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = -1 },
            title = { Text("Xóa ảnh", style = MaterialTheme.typography.titleSmall) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = selectedImageUris[showConfirmDialog],
                        contentDescription = "Image to delete",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Bạn có chắc muốn xóa ảnh này?",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedImageUris = selectedImageUris.filterIndexed { i, _ -> i != showConfirmDialog }
                        tempImageFiles = tempImageFiles.filterIndexed { i, _ -> i != showConfirmDialog }
                        showConfirmDialog = -1
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = -1 }) {
                    Text("Hủy")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .shadow(
                    elevation = if (focused) 4.dp else 0.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    ),
                    RoundedCornerShape(8.dp)
                )
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    focused = focusState.isFocused
                },
            placeholder = {
                Text(
                    "Bạn đang nghĩ gì?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            shape = RoundedCornerShape(8.dp)
        )

        TextField(
            value = location,
            onValueChange = { location = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp)),
            placeholder = {
                Text(
                    "Địa điểm",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )

        // Image selection and preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        if (ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            pickImageLauncher.launch("image/*")
                        } else {
                            permissionLauncher.launch(permission)
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add photo",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            selectedImageUris.forEachIndexed { index, uri ->
                var imageClicked by remember { mutableStateOf(false) }
                val imageScale by animateFloatAsState(
                    targetValue = if (imageClicked) 1.1f else 1.0f,
                    animationSpec = tween(durationMillis = 200)
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(imageScale)
                        .clip(RoundedCornerShape(8.dp))
                        .shadow(2.dp, RoundedCornerShape(8.dp))
                        .clickable { imageClicked = !imageClicked }
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.9f))
                            .clickable { showConfirmDialog = index }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove image",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }

            if (selectedImageUris.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { selectedImageUris.size.toFloat() / maxImages },
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "${selectedImageUris.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Post button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    buttonClicked = true
                    scope.launch {
                        if (content.isBlank()) {
                            snackbarHostState.showSnackbar("Vui lòng nhập nội dung")
                            buttonClicked = false
                            return@launch
                        }
                        try {
                            createPostViewModel.createPost(
                                content = content,
                                imageFiles = if (tempImageFiles.isEmpty()) null else tempImageFiles,
                                location = if (location.isBlank()) null else location
                            )
                            content = ""
                            location = ""
                            selectedImageUris = emptyList()
                            tempImageFiles = emptyList()
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Lỗi khi đăng bài: ${e.message}")
                        } finally {
                            buttonClicked = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .scale(buttonScale)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                enabled = uiState !is CreatePostViewModel.CreatePostUiState.Loading
            ) {
                Text(
                    "Đăng bài",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                )
            }
            if (uiState is CreatePostViewModel.CreatePostUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

private fun createTempImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}