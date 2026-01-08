package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.AuthResult
import com.gatishil.studyengine.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {

    /**
     * Sign in with Google ID token
     */
    suspend fun signInWithGoogle(idToken: String): Resource<AuthResult>

    /**
     * Refresh the access token using the refresh token
     */
    suspend fun refreshToken(): Resource<AuthResult>

    /**
     * Revoke a specific refresh token
     */
    suspend fun revokeToken(refreshToken: String): Resource<Boolean>

    /**
     * Revoke all tokens (logout from all devices)
     */
    suspend fun revokeAllTokens(): Resource<Boolean>

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): Resource<User>

    /**
     * Validate current access token
     */
    suspend fun validateToken(): Resource<Boolean>

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Flow<Boolean>

    /**
     * Get saved access token
     */
    suspend fun getAccessToken(): String?

    /**
     * Clear all auth data (logout)
     */
    suspend fun logout()
}

