package com.gatishil.studyengine.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.CreateUserAvailabilityRequestDto
import com.gatishil.studyengine.data.remote.dto.UserAvailabilityDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AvailabilityUiState(
    val isLoading: Boolean = false,
    val availabilities: List<UserAvailabilityDto> = emptyList(),
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val selectedDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(17, 0),
    val isAddingNew: Boolean = false
)

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val api: StudyEngineApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailabilityUiState())
    val uiState: StateFlow<AvailabilityUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    init {
        loadAvailabilities()
    }

    fun loadAvailabilities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getAvailabilities()
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            availabilities = response.body() ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load availabilities: ${response.code()}"
                        )
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
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                selectedDayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0)
            )
        }
    }

    fun updateSelectedDay(day: DayOfWeek) {
        _uiState.update { it.copy(selectedDayOfWeek = day) }
    }

    fun updateStartTime(time: LocalTime) {
        _uiState.update { it.copy(startTime = time) }
    }

    fun updateEndTime(time: LocalTime) {
        _uiState.update { it.copy(endTime = time) }
    }

    fun addAvailability() {
        val state = _uiState.value

        if (state.endTime <= state.startTime) {
            _uiState.update { it.copy(error = "End time must be after start time") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingNew = true, error = null) }
            try {
                // Convert DayOfWeek to C# format (Sunday=0, Monday=1, etc.)
                val dayOfWeekValue = when (state.selectedDayOfWeek.value) {
                    7 -> 0  // Sunday
                    else -> state.selectedDayOfWeek.value
                }

                val request = CreateUserAvailabilityRequestDto(
                    dayOfWeek = dayOfWeekValue,
                    startTime = state.startTime.format(timeFormatter),
                    endTime = state.endTime.format(timeFormatter)
                )

                val response = api.createAvailability(request)
                if (response.isSuccessful) {
                    hideAddDialog()
                    loadAvailabilities()
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

    fun deleteAvailability(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = api.deleteAvailability(id)
                if (response.isSuccessful) {
                    loadAvailabilities()
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

