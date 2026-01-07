package com.example.studyengine.domain.model

import java.time.LocalDateTime

/**
 * Domain model for User
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val timeZone: String,
    val profilePictureUrl: String?,
    val createdAt: LocalDateTime
)

/**
 * Domain model for authentication tokens
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiration: LocalDateTime,
    val refreshTokenExpiration: LocalDateTime
)

/**
 * Domain model for complete auth response
 */
data class AuthResult(
    val tokens: AuthTokens,
    val user: User
)

