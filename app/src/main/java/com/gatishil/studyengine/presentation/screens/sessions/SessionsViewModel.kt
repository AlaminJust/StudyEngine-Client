package com.gatishil.studyengine.presentation.screens.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.CompleteSessionRequest
import com.gatishil.studyengine.domain.model.StudySession
import com.gatishil.studyengine.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodaySessionsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val sessions: List<StudySession> = emptyList(),
    val error: String? = null
)

data class SessionDetailUiState(
    val isLoading: Boolean = true,
    val session: StudySession? = null,
    val completedPages: String = "",
    val notes: String = "",
    val isCompleting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TodaySessionsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodaySessionsUiState())
    val uiState: StateFlow<TodaySessionsUiState> = _uiState.asStateFlow()

    init {
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            sessionRepository.getTodaySessions().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(sessions = resource.data, isLoading = false)
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
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            sessionRepository.refreshSessions()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionDetailUiState())
    val uiState: StateFlow<SessionDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>()
    val events = _events.asSharedFlow()

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = sessionRepository.getSessionById(sessionId)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            session = result.data,
                            completedPages = result.data.completedPages.toString(),
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                }
                is Resource.Loading -> {
                    // Already handling loading
                }
            }
        }
    }

    fun updateCompletedPages(pages: String) {
        _uiState.update { it.copy(completedPages = pages) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun startSession() {
        val session = _uiState.value.session ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }

            when (val result = sessionRepository.startSession(session.id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(session = result.data, isCompleting = false)
                    }
                    _events.emit(SessionEvent.SessionUpdated)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message, isCompleting = false)
                    }
                }
                is Resource.Loading -> {
                    // Already handling loading
                }
            }
        }
    }

    fun completeSession() {
        val session = _uiState.value.session ?: return
        val completedPages = _uiState.value.completedPages.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }

            val request = CompleteSessionRequest(
                completedPages = completedPages,
                notes = _uiState.value.notes.takeIf { it.isNotBlank() }
            )

            when (val result = sessionRepository.completeSession(session.id, request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(session = result.data, isCompleting = false)
                    }
                    _events.emit(SessionEvent.SessionCompleted)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message, isCompleting = false)
                    }
                }
                is Resource.Loading -> {
                    // Already handling loading
                }
            }
        }
    }

    fun markAsMissed() {
        val session = _uiState.value.session ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }

            when (val result = sessionRepository.markAsMissed(session.id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(session = result.data, isCompleting = false)
                    }
                    _events.emit(SessionEvent.SessionUpdated)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message, isCompleting = false)
                    }
                }
                is Resource.Loading -> {
                    // Already handling loading
                }
            }
        }
    }

    fun cancelSession() {
        val session = _uiState.value.session ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }

            when (val result = sessionRepository.cancelSession(session.id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(session = result.data, isCompleting = false)
                    }
                    _events.emit(SessionEvent.SessionUpdated)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message, isCompleting = false)
                    }
                }
                is Resource.Loading -> {
                    // Already handling loading
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

sealed class SessionEvent {
    data object SessionUpdated : SessionEvent()
    data object SessionCompleted : SessionEvent()
}

