package com.gatishil.studyengine.data.remote.interceptor

import com.gatishil.studyengine.data.local.datastore.AuthPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor that adds the Authorization header to all authenticated requests
 */
class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_TYPE = "Bearer"

        // Endpoints that don't require authentication
        private val PUBLIC_ENDPOINTS = listOf(
            "auth/google",
            "auth/refresh",
            "health"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Check if this is a public endpoint
        val path = originalRequest.url.encodedPath
        if (PUBLIC_ENDPOINTS.any { path.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        // Get access token
        val accessToken = runBlocking {
            authPreferences.getAccessToken().first()
        }

        // If no token, proceed without auth header
        if (accessToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Add authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}

