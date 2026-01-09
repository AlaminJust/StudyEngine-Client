package com.gatishil.studyengine.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.data.mapper.StatsMapper
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val stats: StudyStats? = null,
    val achievements: List<Achievement> = emptyList(),
    val calendarMonth: CalendarMonth? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val error: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val api: StudyEngineApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load full stats
                val statsResponse = api.getStats()
                if (statsResponse.isSuccessful) {
                    statsResponse.body()?.let { dto ->
                        val stats = with(StatsMapper) { dto.toDomain() }
                        _uiState.update { it.copy(stats = stats) }
                    }
                }

                // Load achievements
                val achievementsResponse = api.getAchievements()
                if (achievementsResponse.isSuccessful) {
                    val achievements = achievementsResponse.body()?.map { dto ->
                        with(StatsMapper) { dto.toDomain() }
                    } ?: emptyList()
                    _uiState.update { it.copy(achievements = achievements) }
                }

                // Load calendar for current month
                loadCalendarMonth(_uiState.value.selectedMonth)

                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load stats"
                    )
                }
            }
        }
    }

    fun loadCalendarMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                val response = api.getCalendarMonth(yearMonth.year, yearMonth.monthValue)
                if (response.isSuccessful) {
                    response.body()?.let { dto ->
                        val calendar = with(StatsMapper) { dto.toDomain() }
                        _uiState.update {
                            it.copy(
                                calendarMonth = calendar,
                                selectedMonth = yearMonth
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fail for calendar
            }
        }
    }

    fun selectMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(selectedMonth = yearMonth) }
        loadCalendarMonth(yearMonth)
    }

    fun previousMonth() {
        selectMonth(_uiState.value.selectedMonth.minusMonths(1))
    }

    fun nextMonth() {
        val next = _uiState.value.selectedMonth.plusMonths(1)
        if (next <= YearMonth.now()) {
            selectMonth(next)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadStats()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

