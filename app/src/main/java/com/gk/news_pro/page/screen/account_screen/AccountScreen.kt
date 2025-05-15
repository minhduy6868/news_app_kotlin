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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gk.news_pro.data.model.User
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    userRepository: UserRepository,
    onSignOut: () -> Unit,
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
    val uiState by viewModel.uiState.collectAsState()
    val user by viewModel.user.collectAsState()
    val isUpdatingProfile by viewModel.isUpdatingProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                snackbarHostState.showSnackbar("Permission denied")
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
                    avatarUri = null // Reset after successful update
                    snackbarHostState.showSnackbar("Profile updated successfully")
                }
            }
            is AccountUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as AccountUiState.Error).message)
            }
            else -> {}
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
            when (uiState) {
                is AccountUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AccountUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = 24.dp,
                                bottom = innerPadding.calculateBottomPadding(),
                                start = 16.dp,
                                end = 16.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
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
                                            snackbarHostState.showSnackbar("Username cannot be empty")
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
                                isUpdatingProfile = isUpdatingProfile
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }

                        item {
                            Text(
                                text = "Preferences",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            )

                            SettingsCard {
                                SettingsItem(
                                    icon = Icons.Default.Person,
                                    title = "Favorite Categories",
                                    subtitle = "Manage your news preferences",
                                    onClick = {
                                        coroutineScope.launch {
                                            userRepository.updateFavoriteTopics(mapOf("Tech" to 1, "Sports" to 2))
                                        }
                                    }
                                )
                                SettingsItem(
                                    icon = Icons.Default.Refresh,
                                    title = "Reading History",
                                    subtitle = "Your recently viewed articles",
                                    onClick = { /* Handle click */ }
                                )
                            }

                            SettingsCard {
                                SwitchSettingsItem(
                                    icon = Icons.Outlined.Notifications,
                                    title = "Notifications",
                                    checked = notificationsEnabled,
                                    onCheckedChange = { notificationsEnabled = it },
                                    showDivider = false
                                )
                            }
                        }

                        item {
                            Text(
                                text = "Support",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            )

                            SettingsCard {
                                SettingsItem(
                                    icon = Icons.Outlined.Email,
                                    title = "Contact Us",
                                    subtitle = "Send feedback or get help",
                                    onClick = { /* Handle click */ }
                                )
                                SettingsItem(
                                    icon = Icons.Outlined.Info,
                                    title = "About App",
                                    subtitle = "Version 1.0.0 • Privacy Policy",
                                    onClick = { /* Handle click */ },
                                    showDivider = false
                                )
                            }
                        }

                        item {
                            SignOutButton(onClick = {
                                viewModel.signOut()
                                onSignOut()
                            })
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                is AccountUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (uiState as AccountUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
    isUpdatingProfile: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
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
                            contentDescription = "Avatar",
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
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = android.R.drawable.ic_menu_report_image)
                            )
                        }
                    } else {
                        Text(
                            text = user?.username?.take(2)?.uppercase() ?: "NA",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        singleLine = true
                    )
                    Text(
                        text = user?.email ?: "Sign in for full access",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdatingProfile
            ) {
                if (isUpdatingProfile) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Changes",
                        style = MaterialTheme.typography.labelLarge.copy(
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
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(12.dp),
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
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
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp
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
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
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
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier
                    .size(width = 48.dp, height = 28.dp)
                    .clickable { onCheckedChange(!checked) }
            )
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp
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
            .padding(vertical = 8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ExitToApp,
            contentDescription = "Sign Out",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Sign Out",
            style = MaterialTheme.typography.labelLarge.copy(
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