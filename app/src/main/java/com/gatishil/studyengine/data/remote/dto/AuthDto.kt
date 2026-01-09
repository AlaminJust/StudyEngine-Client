package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for Google Sign-In
 */
@Serializable
data class GoogleSignInRequestDto(
    @SerialName("IdToken")
    val idToken: String
)

/**
 * Response DTO for authentication
 */
@Serializable
data class AuthResponseDto(
    @SerialName("accessToken")
    val accessToken: String,
    @SerialName("refreshToken")
    val refreshToken: String,
    @SerialName("accessTokenExpiration")
    val accessTokenExpiration: String,
    @SerialName("refreshTokenExpiration")
    val refreshTokenExpiration: String,
    @SerialName("user")
    val user: UserDto
)

/**
 * Request DTO for refreshing token
 */
@Serializable
data class RefreshTokenRequestDto(
    @SerialName("RefreshToken")
    val refreshToken: String
)

/**
 * Response DTO for token validation
 */
@Serializable
data class TokenValidationResponseDto(
    @SerialName("isValid")
    val isValid: Boolean,
    @SerialName("userId")
    val userId: String?,
    @SerialName("email")
    val email: String?
)

/**
 * User DTO
 */
@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("timeZone")
    val timeZone: String,
    @SerialName("profilePictureUrl")
    val profilePictureUrl: String?,
    @SerialName("createdAt")
    val createdAt: String
)

/**
 * Request DTO for updating user
 */
@Serializable
data class UpdateUserRequestDto(
    @SerialName("Name")
    val name: String,
    @SerialName("Email")
    val email: String,
    @SerialName("TimeZone")
    val timeZone: String
)

