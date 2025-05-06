package com.gk.news_pro.data.repository

import FirebaseUserService
import android.util.Log
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firebaseService: FirebaseUserService = FirebaseUserService(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val TAG = "UserRepository"

    init {
        auth.setLanguageCode("en") // Avoid X-Firebase-Locale warning
    }

    suspend fun createUser(username: String, email: String, password: String): User? {
        try {
            Log.d(TAG, "createUser: Attempting to create user with email $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Log.d(TAG, "createUser: Authentication successful, UID: ${firebaseUser.uid}")
                val user = User(
                    email = email,
                    username = username,
                    password = password
                )
                firebaseService.addUser(firebaseUser.uid, user)
                Log.d(TAG, "createUser: User data saved for ${user.email}")
                return user
            } else {
                Log.e(TAG, "createUser: Firebase user is null")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "createUser: Failed to create user: ${e.message}", e)
            throw Exception("Tạo tài khoản thất bại: ${e.message}")
        }
    }

    suspend fun loginUser(email: String, password: String): User? {
        try {
            Log.d(TAG, "loginUser: Attempting to login with email $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Log.d(TAG, "loginUser: Login successful, UID: ${firebaseUser.uid}")
                return firebaseService.getUser(firebaseUser.uid)
            } else {
                Log.e(TAG, "loginUser: Firebase user is null")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "loginUser: Failed to login: ${e.message}", e)
            throw Exception("Đăng nhập thất bại: ${e.message}")
        }
    }

    suspend fun updateUser(username: String?, email: String?, avatar: String?, password: String?) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "updateUser: No user logged in")
            throw Exception("Chưa đăng nhập")
        }
        try {
            Log.d(TAG, "updateUser: Updating user ${firebaseUser.uid}")
            if (!email.isNullOrBlank() && email != firebaseUser.email) {
                firebaseUser.updateEmail(email).await()
                Log.d(TAG, "updateUser: Email updated to $email")
            }
            if (!password.isNullOrBlank()) {
                firebaseUser.updatePassword(password).await()
                Log.d(TAG, "updateUser: Password updated")
            }
            firebaseService.updateUser(firebaseUser.uid, username, email, avatar, password)
            Log.d(TAG, "updateUser: User data updated")
        } catch (e: Exception) {
            Log.e(TAG, "updateUser: Failed to update user: ${e.message}", e)
            throw Exception("Cập nhật tài khoản thất bại: ${e.message}")
        }
    }

    suspend fun deleteUser() {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "deleteUser: No user logged in")
            throw Exception("Chưa đăng nhập")
        }
        try {
            Log.d(TAG, "deleteUser: Deleting user ${firebaseUser.uid}")
            firebaseService.deleteUser(firebaseUser.uid)
            firebaseUser.delete().await()
            Log.d(TAG, "deleteUser: User deleted")
        } catch (e: Exception) {
            Log.e(TAG, "deleteUser: Failed to delete user: ${e.message}", e)
            throw Exception("Xóa tài khoản thất bại: ${e.message}")
        }
    }

    suspend fun getUser(): User? {
        val firebaseUser = auth.currentUser ?: run {
            Log.d(TAG, "getUser: No user logged in")
            return null
        }
        return firebaseService.getUser(firebaseUser.uid)
    }

    suspend fun getAllUsers(): List<User> {
        return firebaseService.getAllUsers()
    }

    suspend fun saveFavoriteNews(news: News) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "saveFavoriteNews: No user logged in")
            throw Exception("Chưa đăng nhập")
        }
        try {
            firebaseService.addFavoriteNews(firebaseUser.uid, news)
            Log.d(TAG, "saveFavoriteNews: Successfully saved news ${news.article_id}")
        } catch (e: Exception) {
            Log.e(TAG, "saveFavoriteNews: Failed to save news: ${e.message}")
            throw e
        }
    }

    suspend fun getFavoriteNewsList(): List<News> {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "getFavoriteNewsList: No user logged in")
            throw Exception("Chưa đăng nhập")
        }
        try {
            val newsList = firebaseService.getFavoriteNews(firebaseUser.uid)
            Log.d(TAG, "getFavoriteNewsList: Retrieved ${newsList.size} news")
            return newsList
        } catch (e: Exception) {
            Log.e(TAG, "getFavoriteNewsList: Failed to retrieve news: ${e.message}")
            throw e
        }
    }

    suspend fun removeFavoriteNews(newsId: String) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "removeFavoriteNews: No user logged in")
            throw Exception("Chưa đăng nhập")
        }
        try {
            Log.d(TAG, "removeFavoriteNews: Removing news $newsId for user ${firebaseUser.uid}")
            firebaseService.removeFavoriteNews(firebaseUser.uid, newsId)
            Log.d(TAG, "removeFavoriteNews: News removed")
        } catch (e: Exception) {
            Log.e(TAG, "removeFavoriteNews: Failed to remove news: ${e.message}", e)
            throw Exception("Xóa tin tức yêu thích thất bại: ${e.message}")
        }
    }

    suspend fun updateFavoriteTopics(topics: Map<String, Int>) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "updateFavoriteTopics: No user logged in")
            throw Exception("Chưa đăng nhập")
        }
        try {
            firebaseService.updateFavoriteTopics(firebaseUser.uid, topics)
            Log.d(TAG, "updateFavoriteTopics: Successfully updated topics")
        } catch (e: Exception) {
            Log.e(TAG, "updateFavoriteTopics: Failed to update topics: ${e.message}")
            throw e
        }
    }

    fun signOut() {
        Log.d(TAG, "signOut: Signing out user")
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}