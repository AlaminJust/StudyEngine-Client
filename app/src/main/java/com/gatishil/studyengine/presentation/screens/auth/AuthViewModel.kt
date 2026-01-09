package com.gatishil.studyengine.presentation.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.BuildConfig
import com.gatishil.studyengine.core.session.UserSessionManager
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.User
import com.gatishil.studyengine.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

sealed class AuthEvent {
    data object SignInSuccess : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setNonce(generateNonce())
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Sign in with the backend
                when (val authResult = authRepository.signInWithGoogle(idToken)) {
                    is Resource.Success -> {
                        // Save user to centralized session manager
                        userSessionManager.setCurrentUser(authResult.data.user)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = authResult.data.user,
                                error = null
                            )
                        }
                        _events.emit(AuthEvent.SignInSuccess)
                    }
                    is Resource.Error -> {
                        val errorMessage = authResult.message ?: "Sign in failed"
                        _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                        _events.emit(AuthEvent.ShowError(errorMessage))
                    }
                    is Resource.Loading -> {
                        // Already handling loading state
                    }
                }

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("DEVELOPER_ERROR") == true ||
                    e.message?.contains("28444") == true -> {
                        "Developer console setup error. Check:\n" +
                        "1. SHA-1 fingerprint registered in Google Cloud Console\n" +
                        "2. Package name is 'com.gatishil.studyengine'\n" +
                        "3. Using Web Client ID (not Android Client ID)\n" +
                        "Error: ${e.message}"
                    }
                    e.message?.contains("USER_CANCELLED") == true -> {
                        "Sign-in cancelled"
                    }
                    e.message?.contains("NETWORK_ERROR") == true -> {
                        "Network error. Check your internet connection and backend server."
                    }
                    else -> e.message ?: "Sign in failed"
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                _events.emit(AuthEvent.ShowError(errorMessage))
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.logout()
            userSessionManager.clearSession()
            _uiState.update { it.copy(isLoading = false, user = null) }
        }
    }

    /**
     * Test Sign-In that bypasses both Google Sign-In form and the backend.
     * Creates a hardcoded mock user directly for testing purposes.
     * No network calls or UI prompts are made.
     *
     * NOTE: This saves a mock token for API calls. For real testing with backend,
     * you should use signInWithGoogle() instead.
     */
    fun testSignInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Simulate a small delay to mimic network call (optional)
            kotlinx.coroutines.delay(500)

            // Create a hardcoded test user without any Google or backend calls
            val testUser = User(
                id = "test_user_123",
                name = "Test User",
                email = "testuser@example.com",
                timeZone = java.util.TimeZone.getDefault().id,
                profilePictureUrl = null,
                createdAt = LocalDateTime.now()
            )

            // Save user AND a mock token to session manager
            // This mock token allows API calls to include Authorization header
            // Note: The backend still needs to accept this token or use a test mode
            userSessionManager.setCurrentUserWithToken(
                user = testUser,
                accessToken = "test_access_token_for_development",
                refreshToken = "test_refresh_token"
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = testUser,
                    error = null
                )
            }
            _events.emit(AuthEvent.SignInSuccess)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

