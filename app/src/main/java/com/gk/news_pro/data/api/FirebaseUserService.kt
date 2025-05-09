package com.gk.news_pro.data.repository

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.gk.news_pro.data.model.News
import com.gk.news_pro.data.model.RadioStation
import com.gk.news_pro.data.model.User
import kotlinx.coroutines.tasks.await

class FirebaseUserService {

    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val usersRef: DatabaseReference = db.child("users")
    private val TAG = "FirebaseUserService"

    suspend fun addUser(uid: String, user: User) {
        try {
            usersRef.child(uid).setValue(user).await()
            Log.d(TAG, "addUser: Successfully saved user $uid to Realtime Database")
        } catch (e: Exception) {
            Log.e(TAG, "addUser: Failed to save user $uid: ${e.message}", e)
            throw e
        }
    }

    suspend fun getUser(uid: String): User? {
        try {
            val snapshot = usersRef.child(uid).get().await()
            val user = snapshot.getValue(User::class.java)
            Log.d(TAG, "getUser: Retrieved user $uid: ${user?.email}")
            return user
        } catch (e: Exception) {
            Log.e(TAG, "getUser: Failed to retrieve user $uid: ${e.message}", e)
            return null
        }
    }

    suspend fun getAllUsers(): List<User> {
        try {
            val snapshot = usersRef.get().await()
            val userList = mutableListOf<User>()
            snapshot.children.forEach {
                it.getValue(User::class.java)?.let { user ->
                    userList.add(user)
                }
            }
            Log.d(TAG, "getAllUsers: Retrieved ${userList.size} users")
            return userList
        } catch (e: Exception) {
            Log.e(TAG, "getAllUsers: Failed to retrieve users: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun updateUser(uid: String, username: String?, email: String?, avatar: String?, password: String?) {
        try {
            val updates = mutableMapOf<String, Any>()
            username?.let { updates["username"] = it }
            email?.let { updates["email"] = it }
            avatar?.let { updates["avatar"] = it }
            password?.let { updates["password"] = it }
            if (updates.isNotEmpty()) {
                usersRef.child(uid).updateChildren(updates).await()
                Log.d(TAG, "updateUser: Successfully UPDATED user $uid")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateUser: Failed to update user $uid: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteUser(uid: String) {
        try {
            usersRef.child(uid).removeValue().await()
            Log.d(TAG, "deleteUser: Successfully deleted user $uid")
        } catch (e: Exception) {
            Log.e(TAG, "deleteUser: Failed to delete user $uid: ${e.message}", e)
            throw e
        }
    }

    suspend fun addFavoriteNews(uid: String, news: News) {
        try {
            usersRef.child(uid)
                .child("favoriteNews")
                .child(news.article_id)
                .setValue(news)
                .await()
            Log.d(TAG, "addFavoriteNews: Successfully added news ${news.article_id} for user $uid")
        } catch (e: Exception) {
            Log.e(TAG, "addFavoriteNews: Failed to add news for user $uid: ${e.message}", e)
            throw e
        }
    }

    suspend fun getFavoriteNews(uid: String): List<News> {
        try {
            val snapshot = usersRef.child(uid).child("favoriteNews").get().await()
            val newsList = mutableListOf<News>()
            snapshot.children.forEach { child ->
                try {
                    val news = child.getValue(News::class.java)
                    if (news != null) {
                        newsList.add(news)
                    } else {
                        Log.w(TAG, "getFavoriteNews: Failed to parse news for key ${child.key}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getFavoriteNews: Error parsing news for key ${child.key}: ${e.message}", e)
                }
            }
            Log.d(TAG, "getFavoriteNews: Retrieved ${newsList.size} favorite news for user $uid")
            return newsList
        } catch (e: Exception) {
            Log.e(TAG, "getFavoriteNews: Failed to retrieve favorite news for user $uid: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun removeFavoriteNews(uid: String, newsId: String) {
        try {
            usersRef.child(uid)
                .child("favoriteNews")
                .child(newsId)
                .removeValue()
                .await()
            Log.d(TAG, "removeFavoriteNews: Successfully removed news $newsId for user $uid")
        } catch (e: Exception) {
            Log.e(TAG, "removeFavoriteNews: Failed to remove news $newsId for user $uid: ${e.message}", e)
            throw e
        }
    }

    suspend fun addFavoriteRadioStation(uid: String, station: RadioStation) {
        try {
            usersRef.child(uid)
                .child("favoriteRadioStations")
                .child(station.stationuuid)
                .setValue(station)
                .await()
            Log.d(TAG, "addFavoriteRadioStation: Successfully added station ${station.stationuuid} for user $uid")
        } catch (e: Exception) {
            Log.e(TAG, "addFavoriteRadioStation: Failed to add station for user $uid: ${e.message}", e)
            throw e
        }
    }

    suspend fun getFavoriteRadioStations(uid: String): List<RadioStation> {
        try {
            val snapshot = usersRef.child(uid).child("favoriteRadioStations").get().await()
            val stationList = mutableListOf<RadioStation>()
            snapshot.children.forEach { child ->
                try {
                    val station = child.getValue(RadioStation::class.java)
                    if (station != null) {
                        stationList.add(station)
                    } else {
                        Log.w(TAG, "getFavoriteRadioStations: Failed to parse station for key ${child.key}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getFavoriteRadioStations: Error parsing station for key ${child.key}: ${e.message}", e)
                }
            }
            Log.d(TAG, "getFavoriteRadioStations: Retrieved ${stationList.size} favorite stations for user $uid")
            return stationList
        } catch (e: Exception) {
            Log.e(TAG, "getFavoriteRadioStations: Failed to retrieve favorite stations for user $uid: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun removeFavoriteRadioStation(uid: String, stationId: String) {
        try {
            usersRef.child(uid)
                .child("favoriteRadioStations")
                .child(stationId)
                .removeValue()
                .await()
            Log.d(TAG, "removeFavoriteRadioStation: Successfully removed station $stationId for user $uid")
        } catch (e: Exception) {
            Log.e(TAG, "removeFavoriteRadioStation: Failed to remove station $stationId for user $uid: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateFavoriteTopics(uid: String, topics: Map<String, Int>) {
        try {
            usersRef.child(uid).child("favoriteTopics").setValue(topics).await()
            Log.d(TAG, "updateFavoriteTopics: Successfully updated topics for user $uid")
        } catch (e: Exception) {
            Log.e(TAG, "updateFavoriteTopics: Failed to update topics for user $uid: ${e.message}", e)
            throw e
        }
    }
}