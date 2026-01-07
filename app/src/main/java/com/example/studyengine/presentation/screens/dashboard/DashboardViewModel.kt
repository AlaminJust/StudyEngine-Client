package com.example.studyengine.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyengine.core.util.Resource
import com.example.studyengine.domain.model.Book
import com.example.studyengine.domain.model.StudySession
import com.example.studyengine.domain.repository.BookRepository
import com.example.studyengine.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val todaySessions: List<StudySession> = emptyList(),
    val recentBooks: List<Book> = emptyList(),
    val upcomingSessionsCount: Int = 0,
    val todayCompletedCount: Int = 0,
    val totalPagesReadToday: Int = 0,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeData()
    }

    private fun observeData() {
        // Observe today's sessions
        viewModelScope.launch {
            sessionRepository.getTodaySessions().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val sessions = resource.data
                        val completedPages = sessions
                            .filter { it.isCompleted }
                            .sumOf { it.completedPages }

                        _uiState.update { state ->
                            state.copy(
                                todaySessions = sessions,
                                totalPagesReadToday = completedPages,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = resource.message, isLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }

        // Observe books
        viewModelScope.launch {
            bookRepository.getBooks().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                recentBooks = resource.data.take(5),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = resource.message, isLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        // Already handling loading
                    }
                }
            }
        }

        // Observe upcoming sessions count
        viewModelScope.launch {
            sessionRepository.getUpcomingSessionsCount().collect { count ->
                _uiState.update { it.copy(upcomingSessionsCount = count) }
            }
        }

        // Observe today's completed count
        viewModelScope.launch {
            sessionRepository.getTodayCompletedCount().collect { count ->
                _uiState.update { it.copy(todayCompletedCount = count) }
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Refresh from remote
            bookRepository.refreshBooks()
            sessionRepository.refreshSessions()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            bookRepository.refreshBooks()
            sessionRepository.refreshSessions()

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

