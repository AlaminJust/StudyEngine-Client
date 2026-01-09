package com.gatishil.studyengine.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.CreateScheduleOverrideRequestDto
import com.gatishil.studyengine.data.remote.dto.ScheduleOverrideDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ScheduleOverridesUiState(
    val isLoading: Boolean = false,
    val overrides: List<ScheduleOverrideDto> = emptyList(),
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val isOff: Boolean = true,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isAddingNew: Boolean = false
)

@HiltViewModel
class ScheduleOverridesViewModel @Inject constructor(
    private val api: StudyEngineApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleOverridesUiState())
    val uiState: StateFlow<ScheduleOverridesUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    init {
        loadOverrides()
    }

    fun loadOverrides() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getScheduleOverrides()
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            overrides = response.body()?.sortedByDescending { o -> o.overrideDate } ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                selectedDate = LocalDate.now(),
                isOff = true,
                startTime = null,
                endTime = null
            )
        }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun updateSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun updateIsOff(isOff: Boolean) {
        _uiState.update {
            it.copy(
                isOff = isOff,
                startTime = if (isOff) null else LocalTime.of(9, 0),
                endTime = if (isOff) null else LocalTime.of(17, 0)
            )
        }
    }

    fun updateStartTime(time: LocalTime) {
        _uiState.update { it.copy(startTime = time) }
    }

    fun updateEndTime(time: LocalTime) {
        _uiState.update { it.copy(endTime = time) }
    }

    fun addOverride() {
        val state = _uiState.value

        if (!state.isOff && state.startTime != null && state.endTime != null) {
            if (state.endTime <= state.startTime) {
                _uiState.update { it.copy(error = "End time must be after start time") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingNew = true, error = null) }
            try {
                val request = CreateScheduleOverrideRequestDto(
                    overrideDate = state.selectedDate.format(dateFormatter),
                    startTime = state.startTime?.format(timeFormatter),
                    endTime = state.endTime?.format(timeFormatter),
                    isOff = state.isOff
                )

                val response = api.createScheduleOverride(request)
                if (response.isSuccessful) {
                    hideAddDialog()
                    loadOverrides()
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    _uiState.update {
                        it.copy(isAddingNew = false, error = "Failed to add: $errorBody")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isAddingNew = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun deleteOverride(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = api.deleteScheduleOverride(id)
                if (response.isSuccessful) {
                    loadOverrides()
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to delete")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

