package com.gatishil.studyengine.data.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.local.datastore.AuthPreferences
import com.gatishil.studyengine.data.mapper.AuthMapper
import com.gatishil.studyengine.data.mapper.UserMapper
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.GoogleSignInRequestDto
import com.gatishil.studyengine.data.remote.dto.RefreshTokenRequestDto
import com.gatishil.studyengine.domain.model.AuthResult
import com.gatishil.studyengine.domain.model.User
import com.gatishil.studyengine.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Resource<AuthResult> {
        return try {
            val response = api.googleSignIn(GoogleSignInRequestDto(idToken))

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    // Save tokens and user info
                    authPreferences.saveTokens(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                        accessTokenExpiration = with(AuthMapper) { authResponse.toDomain() }
                            .tokens.accessTokenExpiration.toEpochSecond(ZoneOffset.UTC),
                        refreshTokenExpiration = with(AuthMapper) { authResponse.toDomain() }
                            .tokens.refreshTokenExpiration.toEpochSecond(ZoneOffset.UTC)
                    )

                    authPreferences.saveUser(
                        userId = authResponse.user.id,
                        email = authResponse.user.email,
                        name = authResponse.user.name,
                        profilePictureUrl = authResponse.user.profilePictureUrl
                    )

                    Resource.success(with(AuthMapper) { authResponse.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Sign in failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun refreshToken(): Resource<AuthResult> {
        return try {
            val refreshToken = authPreferences.getRefreshToken().first()
                ?: return Resource.error(Exception("No refresh token available"))

            val response = api.refreshToken(RefreshTokenRequestDto(refreshToken))

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    authPreferences.saveTokens(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken
                    )

                    Resource.success(with(AuthMapper) { authResponse.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Token refresh failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun revokeToken(refreshToken: String): Resource<Boolean> {
        return try {
            val response = api.revokeToken(RefreshTokenRequestDto(refreshToken))

            if (response.isSuccessful) {
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Revoke failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun revokeAllTokens(): Resource<Boolean> {
        return try {
            val response = api.revokeAllTokens()

            if (response.isSuccessful) {
                authPreferences.clearAll()
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Revoke all failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val response = api.getAuthUser()

            if (response.isSuccessful) {
                response.body()?.let { userDto ->
                    Resource.success(with(UserMapper) { userDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Get user failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun validateToken(): Resource<Boolean> {
        return try {
            val response = api.validateToken()

            if (response.isSuccessful) {
                response.body()?.let { validation ->
                    Resource.success(validation.isValid)
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.success(false)
            }
        } catch (e: Exception) {
            Resource.success(false)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = authPreferences.isLoggedIn()

    override suspend fun getAccessToken(): String? = authPreferences.getAccessToken().first()

    override suspend fun logout() {
        // Try to revoke token on server
        try {
            val refreshToken = authPreferences.getRefreshToken().first()
            if (!refreshToken.isNullOrEmpty()) {
                api.revokeToken(RefreshTokenRequestDto(refreshToken))
            }
        } catch (e: Exception) {
            // Ignore errors during logout
        }

        // Clear local data
        authPreferences.clearAll()
    }
}

