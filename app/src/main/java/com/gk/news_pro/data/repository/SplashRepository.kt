//package com.gk.news_pro.data.repository
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.booleanPreferencesKey
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.core.emptyPreferences
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.flow.map
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class SplashRepository
//@Inject constructor(
//    private val dataStore: DataStore<Preferences>
//) {
//    companion object {
//        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
//    }
//
//    val hasSeenOnboarding: Flow<Boolean> = dataStore.data
//        .catch { emit(emptyPreferences()) }
//        .map { preferences ->
//            preferences[HAS_SEEN_ONBOARDING] ?: false
//        }
//
//    suspend fun setHasSeenOnboarding() {
//        try {
//            dataStore.edit { preferences ->
//                preferences[HAS_SEEN_ONBOARDING] = true
//            }
//        } catch (e: Exception) {
//            // Log error
//        }
//    }
//}