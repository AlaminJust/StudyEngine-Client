package com.example.studyengine.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * DataStore preferences for authentication tokens and user data
 */
@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.authDataStore

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val ACCESS_TOKEN_EXPIRATION = longPreferencesKey("access_token_expiration")
        private val REFRESH_TOKEN_EXPIRATION = longPreferencesKey("refresh_token_expiration")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_PROFILE_PICTURE = stringPreferencesKey("user_profile_picture")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    // ==================== Token Operations ====================

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        accessTokenExpiration: Long? = null,
        refreshTokenExpiration: Long? = null
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            accessTokenExpiration?.let { preferences[ACCESS_TOKEN_EXPIRATION] = it }
            refreshTokenExpiration?.let { preferences[REFRESH_TOKEN_EXPIRATION] = it }
        }
    }

    fun getAccessToken(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    fun getRefreshToken(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(ACCESS_TOKEN_EXPIRATION)
            preferences.remove(REFRESH_TOKEN_EXPIRATION)
        }
    }

    // ==================== User Operations ====================

    suspend fun saveUser(
        userId: String,
        email: String,
        name: String,
        profilePictureUrl: String?
    ) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_EMAIL] = email
            preferences[USER_NAME] = name
            profilePictureUrl?.let { preferences[USER_PROFILE_PICTURE] = it }
        }
    }

    fun isLoggedIn(): Flow<Boolean> = dataStore.data.map { preferences ->
        !preferences[ACCESS_TOKEN].isNullOrEmpty()
    }

    fun getUserId(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    fun getUserEmail(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    fun getUserName(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    fun getUserProfilePicture(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_PROFILE_PICTURE]
    }

    // ==================== Sync Operations ====================

    suspend fun saveLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = timestamp
        }
    }

    fun getLastSyncTime(): Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIME] ?: 0L
    }

    // ==================== Clear All ====================

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

