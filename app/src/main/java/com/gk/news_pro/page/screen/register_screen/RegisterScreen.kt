package com.gk.news_pro.page.screen.auth

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gk.news_pro.R
import com.gk.news_pro.data.repository.UserRepository
import com.gk.news_pro.page.main_viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    userRepository: UserRepository,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: RegisterViewModel = viewModel(
        factory = ViewModelFactory(userRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = (uiState as RegisterUiState.Error).message,
                    actionLabel = "Thử lại",
                    duration = SnackbarDuration.Long
                ).let { result ->
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.register()
                    }
                }
            }
        } else if (uiState is RegisterUiState.Success) {
            onRegisterSuccess()
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
                    value = username,
                    onValueChange = viewModel::updateUsername,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    placeholder = {
                        Text(
                            "Nhập tên người dùng",
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
                    value = email,
                    onValueChange = viewModel::updateEmail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    placeholder = {
                        Text(
                            "Nhập email của bạn",
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    placeholder = {
                        Text(
                            "Xác nhận mật khẩu",
                            color = Color.Gray.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Text(
                                text = if (confirmPasswordVisible) "Ẩn" else "Hiển",
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
                            viewModel.register()
                            focusManager.clearFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.register()
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
                        "Đăng ký",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Đã có tài khoản? Đăng nhập",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF1A3C34),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(12.dp)
                )

                if (uiState is RegisterUiState.Loading) {
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