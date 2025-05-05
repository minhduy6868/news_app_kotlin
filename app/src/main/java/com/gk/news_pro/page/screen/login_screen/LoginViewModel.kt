package com.gk.news_pro.page.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _uiState.value = LoginUiState.Error("Email và mật khẩu không được để trống")
            return
        }

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val user = repository.loginUser(email.value, password.value)
                if (user != null) {
                    _uiState.value = LoginUiState.Success
                } else {
                    _uiState.value = LoginUiState.Error("Đăng nhập thất bại: Người dùng không tồn tại")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Đăng nhập thất bại")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = LoginUiState.Idle
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}