package com.gk.news_pro.data.repository

import android.util.Log
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.data.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firebaseService: FirebaseUserService = FirebaseUserService(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val TAG = "UserRepository"

    init {
        auth.setLanguageCode("vi") // Đặt ngôn ngữ thành tiếng Việt
    }

    suspend fun createUser(username: String, email: String, password: String): User? {
        try {
            Log.d(TAG, "createUser: Đang cố gắng tạo người dùng với email $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Log.d(TAG, "createUser: Xác thực thành công, UID: ${firebaseUser.uid}")
                val user = User(
                    email = email,
                    username = username,
                    password = password
                )
                firebaseService.addUser(firebaseUser.uid, user)
                Log.d(TAG, "createUser: Dữ liệu người dùng đã được lưu cho ${user.email}")
                return user
            } else {
                Log.e(TAG, "createUser: Người dùng Firebase là null")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "createUser: Không thể tạo người dùng: ${e.message}", e)
            throw Exception("Tạo tài khoản thất bại: ${e.message}")
        }
    }

    suspend fun loginUser(email: String, password: String): User? {
        try {
            Log.d(TAG, "loginUser: Đang cố gắng đăng nhập với email $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Log.d(TAG, "loginUser: Đăng nhập thành công, UID: ${firebaseUser.uid}")
                return firebaseService.getUser(firebaseUser.uid)
            } else {
                Log.e(TAG, "loginUser: Người dùng Firebase là null")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "loginUser: Đăng nhập thất bại: ${e.message}", e)
            throw Exception("Đăng nhập thất bại: ${e.message}")
        }
    }

    suspend fun updateUser(username: String?, email: String?, avatar: String?, password: String?) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "updateUser: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            Log.d(TAG, "updateUser: Đang cập nhật người dùng ${firebaseUser.uid}")
            if (!email.isNullOrBlank() && email != firebaseUser.email) {
                firebaseUser.updateEmail(email).await()
                Log.d(TAG, "updateUser: Email đã được cập nhật thành $email")
            }
            if (!password.isNullOrBlank()) {
                firebaseUser.updatePassword(password).await()
                Log.d(TAG, "updateUser: Mật khẩu đã được cập nhật")
            }
            firebaseService.updateUser(firebaseUser.uid, username, email, avatar, password)
            Log.d(TAG, "updateUser: Dữ liệu người dùng đã được cập nhật")
        } catch (e: Exception) {
            Log.e(TAG, "updateUser: Cập nhật người dùng thất bại: ${e.message}", e)
            throw Exception("Cập nhật tài khoản thất bại: ${e.message}")
        }
    }

    suspend fun deleteUser() {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "deleteUser: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            Log.d(TAG, "deleteUser: Đang xóa người dùng ${firebaseUser.uid}")
            firebaseService.deleteUser(firebaseUser.uid)
            firebaseUser.delete().await()
            Log.d(TAG, "deleteUser: Người dùng đã được xóa")
        } catch (e: Exception) {
            Log.e(TAG, "deleteUser: Xóa người dùng thất bại: ${e.message}", e)
            throw Exception("Xóa tài khoản thất bại: ${e.message}")
        }
    }

    suspend fun getUser(): User? {
        val firebaseUser = auth.currentUser ?: run {
            Log.d(TAG, "getUser: Không có người dùng nào đăng nhập")
            return null
        }
        return firebaseService.getUser(firebaseUser.uid)
    }

    suspend fun getAllUsers(): List<User> {
        return firebaseService.getAllUsers()
    }

    suspend fun saveFavoriteNews(news: News) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "saveFavoriteNews: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            firebaseService.addFavoriteNews(firebaseUser.uid, news)
            Log.d(TAG, "saveFavoriteNews: Đã lưu tin tức thành công ${news.article_id}")
        } catch (e: Exception) {
            Log.e(TAG, "saveFavoriteNews: Lưu tin tức thất bại: ${e.message}")
            throw e
        }
    }

    suspend fun getFavoriteNewsList(): List<News> {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "getFavoriteNewsList: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            val newsList = firebaseService.getFavoriteNews(firebaseUser.uid)
            Log.d(TAG, "getFavoriteNewsList: Đã lấy được ${newsList.size} tin tức")
            return newsList
        } catch (e: Exception) {
            Log.e(TAG, "getFavoriteNewsList: Lấy tin tức thất bại: ${e.message}")
            throw e
        }
    }

    suspend fun removeFavoriteNews(newsId: String) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "removeFavoriteNews: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            Log.d(TAG, "removeFavoriteNews: Đang xóa tin tức $newsId cho người dùng ${firebaseUser.uid}")
            firebaseService.removeFavoriteNews(firebaseUser.uid, newsId)
            Log.d(TAG, "removeFavoriteNews: Tin tức đã được xóa")
        } catch (e: Exception) {
            Log.e(TAG, "removeFavoriteNews: Xóa tin tức thất bại: ${e.message}", e)
            throw Exception("Xóa tin tức yêu thích thất bại: ${e.message}")
        }
    }

    suspend fun saveFavoriteRadioStation(station: RadioStation) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "saveFavoriteRadioStation: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            firebaseService.addFavoriteRadioStation(firebaseUser.uid, station)
            Log.d(TAG, "saveFavoriteRadioStation: Đã lưu đài radio thành công ${station.stationuuid}")
        } catch (e: Exception) {
            Log.e(TAG, "saveFavoriteRadioStation: Lưu đài radio thất bại: ${e.message}")
            throw e
        }
    }

    suspend fun getFavoriteRadioStations(): List<RadioStation> {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "getFavoriteRadioStations: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            val stationList = firebaseService.getFavoriteRadioStations(firebaseUser.uid)
            Log.d(TAG, "getFavoriteRadioStations: Đã lấy được ${stationList.size} đài radio")
            return stationList
        } catch (e: Exception) {
            Log.e(TAG, "getFavoriteRadioStations: Lấy đài radio thất bại: ${e.message}")
            throw e
        }
    }

    suspend fun removeFavoriteRadioStation(stationId: String) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "removeFavoriteRadioStation: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            Log.d(TAG, "removeFavoriteRadioStation: Đang xóa đài radio $stationId cho người dùng ${firebaseUser.uid}")
            firebaseService.removeFavoriteRadioStation(firebaseUser.uid, stationId)
            Log.d(TAG, "removeFavoriteRadioStation: Đài radio đã được xóa")
        } catch (e: Exception) {
            Log.e(TAG, "removeFavoriteRadioStation: Xóa đài radio thất bại: ${e.message}", e)
            throw Exception("Xóa đài radio yêu thích thất bại: ${e.message}")
        }
    }

    suspend fun updateFavoriteTopics(topics: Map<String, Int>) {
        val firebaseUser = auth.currentUser ?: run {
            Log.e(TAG, "updateFavoriteTopics: Không có người dùng nào đăng nhập")
            throw Exception("Chưa đăng nhập")
        }
        try {
            firebaseService.updateFavoriteTopics(firebaseUser.uid, topics)
            Log.d(TAG, "updateFavoriteTopics: Đã cập nhật chủ đề thành công")
        } catch (e: Exception) {
            Log.e(TAG, "updateFavoriteTopics: Cập nhật chủ đề thất bại: ${e.message}")
            throw e
        }
    }

    fun signOut() {
        Log.d(TAG, "signOut: Đang đăng xuất người dùng")
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}