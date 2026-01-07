package com.example.studyengine.data.remote.interceptor

import com.example.studyengine.data.local.datastore.AuthPreferences
import com.example.studyengine.data.remote.api.StudyEngineApi
import com.example.studyengine.data.remote.dto.RefreshTokenRequestDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * Authenticator that handles 401 responses by refreshing the access token
 */
class TokenRefreshAuthenticator @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val apiProvider: Provider<StudyEngineApi>
) : Authenticator {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_TYPE = "Bearer"
        private const val MAX_RETRY_COUNT = 3
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops
        if (responseCount(response) >= MAX_RETRY_COUNT) {
            // Clear tokens and force re-login
            runBlocking {
                authPreferences.clearTokens()
            }
            return null
        }

        // Get current refresh token
        val refreshToken = runBlocking {
            authPreferences.getRefreshToken().first()
        }

        // If no refresh token, can't refresh
        if (refreshToken.isNullOrEmpty()) {
            runBlocking {
                authPreferences.clearTokens()
            }
            return null
        }

        // Try to refresh the token
        return synchronized(this) {
            // Double-check if token was already refreshed by another thread
            val currentAccessToken = runBlocking {
                authPreferences.getAccessToken().first()
            }

            val requestToken = response.request.header(HEADER_AUTHORIZATION)
                ?.removePrefix("$TOKEN_TYPE ")

            // If tokens don't match, another thread already refreshed
            if (currentAccessToken != requestToken && !currentAccessToken.isNullOrEmpty()) {
                return response.request.newBuilder()
                    .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $currentAccessToken")
                    .build()
            }

            // Refresh the token
            val newTokens = runBlocking {
                try {
                    val refreshResponse = apiProvider.get().refreshToken(
                        RefreshTokenRequestDto(refreshToken)
                    )

                    if (refreshResponse.isSuccessful) {
                        refreshResponse.body()?.let { authResponse ->
                            // Save new tokens
                            authPreferences.saveTokens(
                                accessToken = authResponse.accessToken,
                                refreshToken = authResponse.refreshToken
                            )
                            authResponse.accessToken
                        }
                    } else {
                        // Refresh failed, clear tokens
                        authPreferences.clearTokens()
                        null
                    }
                } catch (e: Exception) {
                    authPreferences.clearTokens()
                    null
                }
            }

            // If refresh successful, retry with new token
            newTokens?.let { token ->
                response.request.newBuilder()
                    .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $token")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}

