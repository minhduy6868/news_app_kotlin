package com.gk.news_pro.page.screen.account_screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.news_pro.data.model.User
import com.gk.news_pro.data.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class StringHelper {
    fun uploadImageToCloudinary(file: File): String? {
        // Giả lập upload ảnh, thay bằng logic thực tế
        return "https://example.com/avatar.jpg"
    }
}

class AccountViewModel(
    private val userRepository: UserRepository,
    private val context: Context,
    private val stringHelper: StringHelper = StringHelper()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile

    init {
        loadUser()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                if (!isNetworkAvailable()) {
                    _user.value = User(
                        username = "Không tìm thấy tài khoản",
                        email = "không tìm thấy gmail",
                        avatar = null.toString()
                    )
                    _uiState.value = AccountUiState.Success
                    Log.d("AccountViewModel", "No network, loaded default user")
                    return@launch
                }

                val currentUser = userRepository.getUser()
                _user.value = currentUser
                _uiState.value = AccountUiState.Success
                Log.d("AccountViewModel", "Loaded user: ${currentUser?.email}")
            } catch (e: Exception) {
                _user.value = User(
                    username = "Không tìm thấy tài khoản",
                    email = "không tìm thấy gmail",
                    avatar = null.toString()
                )
                _uiState.value = AccountUiState.Error("Không thể tải thông tin người dùng: ${e.message}")
                Log.e("AccountViewModel", "Error loading user: ${e.message}", e)
            }
        }
    }

    fun updateUserProfile(
        username: String?,
        email: String?,
        password: String?,
        avatarFile: File?
    ) {
        viewModelScope.launch {
            _isUpdatingProfile.value = true
            try {
                var avatarUrl: String? = null
                if (avatarFile != null) {
                    avatarUrl = stringHelper.uploadImageToCloudinary(avatarFile)
                    if (avatarUrl == null) {
                        _uiState.value = AccountUiState.Error("Không thể tải ảnh đại diện lên")
                        Log.e("AccountViewModel", "Failed to upload avatar")
                        return@launch
                    }
                }
                userRepository.updateUser(
                    username = username?.takeIf { it.isNotBlank() },
                    email = email?.takeIf { it.isNotBlank() },
                    avatar = avatarUrl ?: _user.value?.avatar,
                    password = password?.takeIf { it.isNotBlank() }
                )
                val updatedUser = userRepository.getUser()
                _user.value = updatedUser
                _uiState.value = AccountUiState.Success
                Log.d("AccountViewModel", "User profile updated successfully")
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error("Cập nhật thất bại: ${e.message}")
                Log.e("AccountViewModel", "Error updating user: ${e.message}", e)
            } finally {
                _isUpdatingProfile.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                _user.value = null
                _uiState.value = AccountUiState.Success
                Log.d("AccountViewModel", "User signed out")
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error("Đăng xuất thất bại: ${e.message}")
                Log.e("AccountViewModel", "Error signing out: ${e.message}", e)
            }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) {
                    _uiState.value = AccountUiState.Error("Chưa đăng nhập")
                    Log.e("AccountViewModel", "No user logged in")
                    return@launch
                }

                // Re-authenticate user
                val credential = EmailAuthProvider.getCredential(firebaseUser.email ?: "", password)
                firebaseUser.reauthenticate(credential).await()

                // Delete account
                userRepository.deleteUser()
                _user.value = null
                _uiState.value = AccountUiState.Success
                Log.d("AccountViewModel", "Account deleted successfully")
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error("Xóa tài khoản thất bại: ${e.message}")
                Log.e("AccountViewModel", "Error deleting account: ${e.message}", e)
            }
        }
    }
}

sealed class AccountUiState {
    object Loading : AccountUiState()
    object Success : AccountUiState()
    data class Error(val message: String) : AccountUiState()
}