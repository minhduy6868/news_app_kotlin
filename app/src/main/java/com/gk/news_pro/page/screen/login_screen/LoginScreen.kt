package com.gk.news_pro.page.screen.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gk.news_pro.R
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userRepository: UserRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory(userRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val TAG = "LoginScreen"

    // Lấy Activity từ Context
    val activity = (context as? Activity) ?: run {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "Không tìm thấy Activity, vui lòng thử lại",
                duration = SnackbarDuration.Long
            )
        }
        return@LoginScreen
    }

    // Kiểm tra Google Play Services
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val playServicesAvailable = googleApiAvailability.isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS

    LaunchedEffect(playServicesAvailable) {
        if (!playServicesAvailable) {
            snackbarHostState.showSnackbar(
                message = "Google Play Services không khả dụng. Vui lòng kiểm tra thiết bị.",
                duration = SnackbarDuration.Long
            )
        }
    }

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google Sign-In result: resultCode=${result.resultCode}, data=${result.data}")
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                Log.d(TAG, "Google ID Token: $idToken")
                viewModel.signInWithGoogle(idToken)
            } ?: run {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Không lấy được ID token từ Google",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed: statusCode=${e.statusCode}, message=${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Đăng nhập Google thất bại: ${e.statusCode} - ${e.message}",
                    duration = SnackbarDuration.Long
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Lỗi không xác định: ${e.message}",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // Hàm khởi động Google Sign-In
    fun startGoogleSignIn() {
        if (!playServicesAvailable) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Google Play Services không khả dụng",
                    duration = SnackbarDuration.Long
                )
            }
            return
        }

        // Kiểm tra trạng thái lifecycle
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            try {
                Log.d(TAG, "Building GoogleSignInOptions")
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(activity.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                Log.d(TAG, "Creating GoogleSignInClient")
                val googleSignInClient = GoogleSignIn.getClient(activity, gso)
                Log.d(TAG, "Launching Google Sign-In Intent")
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start Google Sign-In: ${e.message}", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Lỗi khởi tạo Google Sign-In: ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        } else {
            Log.w(TAG, "Activity is not in STARTED state, cannot launch Google Sign-In")
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Không thể khởi động đăng nhập Google: Activity không hoạt động",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = (uiState as LoginUiState.Error).message,
                    actionLabel = "Thử lại",
                    duration = SnackbarDuration.Long
                ).let { result ->
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.login()
                    }
                }
            }
        } else if (uiState is LoginUiState.Success) {
            onLoginSuccess()
            viewModel.resetUiState()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White,
                        actionContentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF1F8E9),
                            Color(0xFFA0E7A5),
                            Color(0xFFE0F2F1),
                            Color(0xFF84F08C),
                            Color(0xFFE0F7FA)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.only_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 32.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Text(
                    text = "Chào mừng bạn trở lại, vui lòng đăng nhập để tận hưởng News Pro",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A3C34).copy(alpha = 0.8f),
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = viewModel::updateEmail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    placeholder = {
                        Text(
                            "test@gmail.com",
                            color = Color.Gray.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF1A3C34),
                        unfocusedBorderColor = Color(0xFF1A3C34).copy(alpha = 0.3f),
                        containerColor = Color.White.copy(alpha = 0.95f),
                        cursorColor = Color(0xFF1A3C34),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::updatePassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    placeholder = {
                        Text(
                            "Nhập mật khẩu của bạn",
                            color = Color.Gray.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Ẩn" else "Hiển",
                                color = Color(0xFF1A3C34).copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF1A3C34),
                        unfocusedBorderColor = Color(0xFF1A3C34).copy(alpha = 0.3f),
                        containerColor = Color.White.copy(alpha = 0.95f),
                        cursorColor = Color(0xFF1A3C34),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.login()
                            focusManager.clearFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.login()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A3C34),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "Đăng nhập",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-In Button
                Button(
                    onClick = { startGoogleSignIn() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    enabled = playServicesAvailable
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Đăng nhập với Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Chưa có tài khoản? Đăng ký",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF1A3C34),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .padding(12.dp)
                )

                if (uiState is LoginUiState.Loading) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
                        color = Color(0xFF1A3C34),
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    }
}