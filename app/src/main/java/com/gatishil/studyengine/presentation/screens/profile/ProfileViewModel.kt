package com.gatishil.studyengine.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.UserProfile
import com.gatishil.studyengine.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val editNameDialogVisible: Boolean = false,
    val editName: String = "",
    val isUpdating: Boolean = false
)

sealed class ProfileEvent {
    data object ProfileUpdated : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            profileRepository.getProfile().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                profile = resource.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = resource.message ?: "Failed to load profile"
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadProfile()
    }

    fun showEditNameDialog() {
        _uiState.update {
            it.copy(
                editNameDialogVisible = true,
                editName = it.profile?.name ?: ""
            )
        }
    }

    fun hideEditNameDialog() {
        _uiState.update { it.copy(editNameDialogVisible = false) }
    }

    fun updateEditName(name: String) {
        _uiState.update { it.copy(editName = name) }
    }

    fun saveProfileName() {
        val name = _uiState.value.editName
        val timeZone = _uiState.value.profile?.timeZone ?: "UTC"

        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }

            when (val result = profileRepository.updateProfile(name, timeZone)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            editNameDialogVisible = false,
                            profile = result.data
                        )
                    }
                    _events.emit(ProfileEvent.ProfileUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(ProfileEvent.Error(result.message ?: "Failed to update profile"))
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

