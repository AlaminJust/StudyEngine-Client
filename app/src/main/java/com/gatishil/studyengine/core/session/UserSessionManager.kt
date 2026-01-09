package com.gatishil.studyengine.core.session

import com.gatishil.studyengine.data.local.datastore.AuthPreferences
import com.gatishil.studyengine.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized user session manager that provides:
 * - Current user state across the app
 * - Token management
 * - Login/Logout state
 * - User profile data caching
 */
@Singleton
class UserSessionManager @Inject constructor(
    private val authPreferences: AuthPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Current user state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Login state
    val isLoggedIn: Flow<Boolean> = authPreferences.isLoggedIn()

    // Access token for API calls
    val accessToken: Flow<String?> = authPreferences.getAccessToken()

    init {
        // Load user from preferences on initialization
        scope.launch {
            loadUserFromPreferences()
        }
    }

    private suspend fun loadUserFromPreferences() {
        combine(
            authPreferences.getUserId(),
            authPreferences.getUserEmail(),
            authPreferences.getUserName(),
            authPreferences.getUserProfilePicture()
        ) { userId, email, name, profilePicture ->
            if (userId != null && email != null && name != null) {
                User(
                    id = userId,
                    name = name,
                    email = email,
                    timeZone = java.util.TimeZone.getDefault().id,
                    profilePictureUrl = profilePicture,
                    createdAt = LocalDateTime.now()
                )
            } else {
                null
            }
        }.collect { user ->
            _currentUser.value = user
        }
    }

    /**
     * Set the current user after successful login
     */
    fun setCurrentUser(user: User) {
        _currentUser.value = user
        scope.launch {
            authPreferences.saveUser(
                userId = user.id,
                email = user.email,
                name = user.name,
                profilePictureUrl = user.profilePictureUrl
            )
        }
    }

    /**
     * Set the current user and tokens after successful login
     * Use this for test login or when you need to save both user and tokens
     */
    fun setCurrentUserWithToken(user: User, accessToken: String, refreshToken: String = "") {
        _currentUser.value = user
        scope.launch {
            authPreferences.saveUser(
                userId = user.id,
                email = user.email,
                name = user.name,
                profilePictureUrl = user.profilePictureUrl
            )
            authPreferences.saveTokens(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }
    }

    /**
     * Update user profile
     */
    fun updateUserProfile(
        name: String? = null,
        profilePictureUrl: String? = null
    ) {
        _currentUser.value?.let { currentUser ->
            val updatedUser = currentUser.copy(
                name = name ?: currentUser.name,
                profilePictureUrl = profilePictureUrl ?: currentUser.profilePictureUrl
            )
            setCurrentUser(updatedUser)
        }
    }

    /**
     * Clear session on logout
     */
    fun clearSession() {
        _currentUser.value = null
        scope.launch {
            authPreferences.clearAll()
        }
    }

    /**
     * Check if access token is valid (not expired)
     */
    suspend fun isTokenValid(): Boolean {
        val token = authPreferences.getAccessToken().first()
        return !token.isNullOrEmpty()
    }

    /**
     * Get current access token synchronously (blocking)
     * Use sparingly, prefer the Flow-based accessToken
     */
    suspend fun getAccessTokenSync(): String? {
        return authPreferences.getAccessToken().first()
    }
}

