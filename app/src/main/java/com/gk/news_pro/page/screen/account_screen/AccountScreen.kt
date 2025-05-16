package com.gk.news_pro.page.screen.account_screen

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gk.news_pro.data.model.User
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    userRepository: UserRepository,
    onSignOut: () -> Unit,
    onNavigateToOfflineNews: () -> Unit,
    onNavigateToFavoriteScreen: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    context: Context = LocalContext.current,
    viewModel: AccountViewModel = viewModel(
        factory = ViewModelFactory(
            repositories = userRepository,
            context = context
        )
    )
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val user by viewModel.user.collectAsState()
    val isUpdatingProfile by viewModel.isUpdatingProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Quyền truy cập bị từ chối")
            }
        }
    }

    // Update username when user data loads
    LaunchedEffect(user) {
        user?.let {
            username = it.username ?: ""
        }
    }

    // Show snackbar for UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is AccountUiState.Success -> {
                if (user != null && avatarUri != null) {
                    avatarUri = null
                    snackbarHostState.showSnackbar("Cập nhật hồ sơ thành công")
                } else if (uiState is AccountUiState.Success && user == null) {
                    snackbarHostState.showSnackbar("Xóa tài khoản thành công")
                }
            }
            is AccountUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as AccountUiState.Error).message)
            }
            else -> {}
        }
    }

    // Delete account confirmation dialog
    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Xác nhận xóa tài khoản",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hành động này không thể hoàn tác. Vui lòng nhập mật khẩu để xác nhận.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Mật khẩu") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Hủy", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (passwordInput.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Vui lòng nhập mật khẩu")
                                    }
                                } else {
                                    viewModel.deleteAccount(passwordInput)
                                    showDeleteDialog = false
                                    passwordInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Xóa", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 12.dp,
                        bottom = innerPadding.calculateBottomPadding(),
                        start = 10.dp,
                        end = 10.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    if (uiState is AccountUiState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        ProfileHeader(
                            user = user,
                            username = username,
                            onUsernameChange = { username = it },
                            avatarUri = avatarUri,
                            onAvatarClick = {
                                permissionLauncher.launch(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        android.Manifest.permission.READ_MEDIA_IMAGES
                                    } else {
                                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                                    }
                                )
                            },
                            onSave = {
                                if (username.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Tên người dùng không được để trống")
                                    }
                                } else {
                                    val avatarFile = avatarUri?.let { uri ->
                                        uriToFile(uri, context)
                                    }
                                    viewModel.updateUserProfile(
                                        username = username,
                                        email = null,
                                        password = null,
                                        avatarFile = avatarFile
                                    )
                                }
                            },
                            isUpdatingProfile = isUpdatingProfile,
                            onDeleteClick = { showDeleteDialog = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    Text(
                        text = "Tùy chọn",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 6.dp, top = 6.dp)
                    )

                    SettingsCard {
                        SettingsItem(
                            icon = Icons.Outlined.Favorite,
                            title = "Kho yêu thích của bạn",
                            subtitle = "Xem các bài viết bạn đã yêu thích",
                            onClick = { onNavigateToFavoriteScreen() }
                        )
                        SettingsItem(
                            icon = Icons.Default.Refresh,
                            title = "Lịch sử đọc",
                            subtitle = "Các bài viết bạn đã xem gần đây",
                            onClick = { onNavigateToOfflineNews() }
                        )
                    }

                    SettingsCard {
                        SwitchSettingsItem(
                            icon = Icons.Outlined.Notifications,
                            title = "Thông báo",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            showDivider = false
                        )
                    }
                }

                item {
                    Text(
                        text = "Hỗ trợ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 6.dp, top = 6.dp)
                    )

                    SettingsCard {
                        SettingsItem(
                            icon = Icons.Outlined.Email,
                            title = "Liên hệ",
                            subtitle = "Gửi phản hồi hoặc nhận trợ giúp",
                            onClick = {
                                try {
                                    val emailUri = Uri.parse(
                                        "mailto:duynm.23it@gmail.com?subject=${
                                            URLEncoder.encode("Gửi phản hồi từ app News Pro", "UTF-8")
                                        }"
                                    )
                                    uriHandler.openUri(emailUri.toString())
                                } catch (e: Exception) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Không tìm thấy ứng dụng email")
                                    }
                                }
                            }
                        )
                        SettingsItem(
                            icon = Icons.Outlined.Info,
                            title = "Về ứng dụng",
                            subtitle = "Phiên bản 1.0.0 • Chính sách bảo mật",
                            onClick = { onNavigateToAboutScreen() },
                            showDivider = false
                        )
                    }
                }

                item {
                    SignOutButton(onClick = {
                        viewModel.signOut()
                        onSignOut()
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: User?,
    username: String,
    onUsernameChange: (String) -> Unit,
    avatarUri: Uri?,
    onAvatarClick: () -> Unit,
    onSave: () -> Unit,
    isUpdatingProfile: Boolean,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable { onAvatarClick() }
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Ảnh đại diện",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.ic_menu_report_image)
                        )
                    } else if (!user?.avatar.isNullOrBlank()) {
                        if (user != null) {
                            AsyncImage(
                                model = user.avatar,
                                contentDescription = "Ảnh đại diện",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = android.R.drawable.ic_menu_report_image)
                            )
                        }
                    } else {
                        Text(
                            text = user?.username?.take(2)?.uppercase() ?: "KH",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = { Text("Tên người dùng") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = user?.email ?: "Đăng nhập để truy cập đầy đủ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Xóa tài khoản",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isUpdatingProfile
            ) {
                if (isUpdatingProfile) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    Text(
                        text = "Lưu thay đổi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 56.dp, end = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
fun SwitchSettingsItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier
                    .size(width = 40.dp, height = 24.dp)
                    .clickable { onCheckedChange(!checked) }
            )
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 56.dp, end = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
fun SignOutButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ExitToApp,
            contentDescription = "Đăng xuất",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Đăng xuất",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private fun uriToFile(uri: Uri, context: Context): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        null
    }
}