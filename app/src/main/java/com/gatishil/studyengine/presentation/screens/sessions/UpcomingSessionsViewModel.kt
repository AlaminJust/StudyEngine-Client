package com.gatishil.studyengine.presentation.screens.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.domain.model.StudySession
import com.gatishil.studyengine.data.mapper.StudySessionMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class UpcomingSessionsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val sessions: List<StudySession> = emptyList(),
    val groupedSessions: Map<LocalDate, List<StudySession>> = emptyMap(),
    val error: String? = null,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(30),
    val selectedFilter: SessionFilter = SessionFilter.ALL,
    val showPastSessions: Boolean = false
)

enum class SessionFilter {
    ALL, THIS_WEEK, NEXT_WEEK, THIS_MONTH, PREVIOUS_7_DAYS, PREVIOUS_30_DAYS
}

@HiltViewModel
class UpcomingSessionsViewModel @Inject constructor(
    private val api: StudyEngineApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpcomingSessionsUiState())
    val uiState: StateFlow<UpcomingSessionsUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_DATE

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            val startDate = state.startDate.format(dateFormatter)
            val endDate = state.endDate.format(dateFormatter)

            try {
                val response = api.getSessions(startDate, endDate)
                if (response.isSuccessful) {
                    val sessionDtos = response.body() ?: emptyList()
                    val sessions: List<StudySession> = sessionDtos.map { dto ->
                        with(StudySessionMapper) { dto.toDomain() }
                    }

                    // Group sessions by date
                    val grouped = sessions
                        .sortedBy { session -> session.sessionDate }
                        .groupBy { session -> session.sessionDate }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            sessions = sessions,
                            groupedSessions = grouped
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = "Failed to load sessions"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadSessions()
        }
    }

    fun setFilter(filter: SessionFilter) {
        val today = LocalDate.now()
        val (newStartDate, newEndDate, isPast) = when (filter) {
            SessionFilter.ALL -> Triple(today, today.plusDays(30), false)
            SessionFilter.THIS_WEEK -> {
                val endOfWeek = today.plusDays((7 - today.dayOfWeek.value).toLong())
                Triple(today, endOfWeek, false)
            }
            SessionFilter.NEXT_WEEK -> {
                val startOfNextWeek = today.plusDays((8 - today.dayOfWeek.value).toLong())
                val endOfNextWeek = startOfNextWeek.plusDays(6)
                Triple(startOfNextWeek, endOfNextWeek, false)
            }
            SessionFilter.THIS_MONTH -> {
                val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                Triple(today, endOfMonth, false)
            }
            SessionFilter.PREVIOUS_7_DAYS -> {
                Triple(today.minusDays(7), today.minusDays(1), true)
            }
            SessionFilter.PREVIOUS_30_DAYS -> {
                Triple(today.minusDays(30), today.minusDays(1), true)
            }
        }

        _uiState.update {
            it.copy(
                selectedFilter = filter,
                startDate = newStartDate,
                endDate = newEndDate,
                showPastSessions = isPast
            )
        }
        loadSessions()
    }

    fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        _uiState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                selectedFilter = SessionFilter.ALL
            )
        }
        loadSessions()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

