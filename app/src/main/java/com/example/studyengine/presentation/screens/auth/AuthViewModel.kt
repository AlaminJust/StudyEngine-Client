package com.example.studyengine.presentation.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyengine.BuildConfig
import com.example.studyengine.core.util.Resource
import com.example.studyengine.domain.model.User
import com.example.studyengine.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val authRepository: AuthRepository
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
                val errorMessage = e.message ?: "Sign in failed"
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                _events.emit(AuthEvent.ShowError(errorMessage))
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoading = false, user = null) }
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

