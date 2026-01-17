package com.gatishil.studyengine.presentation.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.PublicProfileCard
import com.gatishil.studyengine.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverProfilesUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val profiles: List<PublicProfileCard> = emptyList(),
    val searchTerm: String = "",
    val selectedRole: String? = null,
    val selectedAcademicLevel: String? = null,
    val selectedInstitutionCountry: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val hasNextPage: Boolean = false,
    val error: String? = null,
    val showFilters: Boolean = false
)

@HiltViewModel
class DiscoverProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverProfilesUiState())
    val uiState: StateFlow<DiscoverProfilesUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    fun loadProfiles(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val page = if (isLoadMore) currentState.currentPage + 1 else 1

            _uiState.update {
                it.copy(
                    isLoading = !isLoadMore && it.profiles.isEmpty(),
                    isLoadingMore = isLoadMore,
                    error = null
                )
            }

            val result = profileRepository.discoverProfiles(
                searchTerm = currentState.searchTerm.takeIf { it.isNotBlank() },
                role = currentState.selectedRole,
                academicLevel = currentState.selectedAcademicLevel,
                institutionCountry = currentState.selectedInstitutionCountry,
                page = page,
                pageSize = 20
            )

            when (result) {
                is Resource.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            profiles = if (isLoadMore) it.profiles + data.profiles else data.profiles,
                            currentPage = data.page,
                            totalPages = data.totalPages,
                            totalCount = data.totalCount,
                            hasNextPage = data.hasNextPage
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadProfiles()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onSearchTermChange(term: String) {
        _uiState.update { it.copy(searchTerm = term) }
    }

    fun onSearch() {
        _uiState.update { it.copy(profiles = emptyList(), currentPage = 1) }
        loadProfiles()
    }

    fun onRoleSelected(role: String?) {
        _uiState.update { it.copy(selectedRole = role, profiles = emptyList(), currentPage = 1) }
        loadProfiles()
    }

    fun onAcademicLevelSelected(level: String?) {
        _uiState.update { it.copy(selectedAcademicLevel = level, profiles = emptyList(), currentPage = 1) }
        loadProfiles()
    }

    fun onInstitutionCountrySelected(country: String?) {
        _uiState.update { it.copy(selectedInstitutionCountry = country, profiles = emptyList(), currentPage = 1) }
        loadProfiles()
    }

    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchTerm = "",
                selectedRole = null,
                selectedAcademicLevel = null,
                selectedInstitutionCountry = null,
                profiles = emptyList(),
                currentPage = 1
            )
        }
        loadProfiles()
    }

    fun loadMore() {
        if (_uiState.value.hasNextPage && !_uiState.value.isLoadingMore) {
            loadProfiles(isLoadMore = true)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

