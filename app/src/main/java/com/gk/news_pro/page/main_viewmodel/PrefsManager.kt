package com.gk.news_pro.page.main_viewmodel

import android.content.Context
import android.content.SharedPreferences

class PrefsManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_LAST_VIDEO_CREATION_TIME = "last_video_creation_time"
        private const val KEY_VIDEO_ID = "daily_video_id"
        private const val KEY_LAST_SUCCESSFUL_VIDEO_URL = "last_successful_video_url"

        @Volatile
        private var instance: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            return instance ?: synchronized(this) {
                instance ?: PrefsManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun shouldCreateDailyVideo(): Boolean {
        val lastCreationTime = sharedPreferences.getLong(KEY_LAST_VIDEO_CREATION_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        return lastCreationTime == 0L || (currentTime - lastCreationTime) >= oneDayInMillis
    }

    fun saveVideoCreationTime(time: Long) {
        sharedPreferences.edit()
            .putLong(KEY_LAST_VIDEO_CREATION_TIME, time)
            .apply()
    }

    fun saveVideoId(videoId: String) {
        sharedPreferences.edit()
            .putString(KEY_VIDEO_ID, videoId)
            .apply()
    }

    fun getVideoId(): String? {
        return sharedPreferences.getString(KEY_VIDEO_ID, null)
    }

    fun clearVideoId() {
        sharedPreferences.edit()
            .remove(KEY_VIDEO_ID)
            .remove(KEY_LAST_VIDEO_CREATION_TIME)
            .apply()
    }

    fun saveLastSuccessfulVideoUrl(url: String) {
        sharedPreferences.edit()
            .putString(KEY_LAST_SUCCESSFUL_VIDEO_URL, url)
            .apply()
    }

    fun getLastSuccessfulVideoUrl(): String? {
        return sharedPreferences.getString(KEY_LAST_SUCCESSFUL_VIDEO_URL, null)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_FIRST_LAUNCH, false)
            .apply()
    }
}