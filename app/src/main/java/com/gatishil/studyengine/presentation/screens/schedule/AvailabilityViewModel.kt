package com.gatishil.studyengine.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.BulkUpdateUserAvailabilityRequestDto
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
    val selectedDaysOfWeek: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY),
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
                selectedDaysOfWeek = setOf(DayOfWeek.MONDAY),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0)
            )
        }
    }

    fun toggleDaySelection(day: DayOfWeek) {
        _uiState.update { state ->
            val newSelection = if (state.selectedDaysOfWeek.contains(day)) {
                // Don't allow deselecting the last day
                if (state.selectedDaysOfWeek.size > 1) {
                    state.selectedDaysOfWeek - day
                } else {
                    state.selectedDaysOfWeek
                }
            } else {
                state.selectedDaysOfWeek + day
            }
            state.copy(selectedDaysOfWeek = newSelection)
        }
    }

    fun selectAllDays() {
        _uiState.update { it.copy(selectedDaysOfWeek = DayOfWeek.entries.toSet()) }
    }

    fun selectWeekdays() {
        _uiState.update {
            it.copy(selectedDaysOfWeek = setOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            ))
        }
    }

    fun selectWeekends() {
        _uiState.update {
            it.copy(selectedDaysOfWeek = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        }
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

        if (state.selectedDaysOfWeek.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one day") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingNew = true, error = null) }
            try {
                // Create availability for each selected day
                val requests = state.selectedDaysOfWeek.map { day ->
                    // Convert DayOfWeek to C# format (Sunday=0, Monday=1, etc.)
                    val dayOfWeekValue = when (day.value) {
                        7 -> 0  // Sunday
                        else -> day.value
                    }
                    CreateUserAvailabilityRequestDto(
                        dayOfWeek = dayOfWeekValue,
                        startTime = state.startTime.format(timeFormatter),
                        endTime = state.endTime.format(timeFormatter)
                    )
                }

                var successCount = 0
                var failCount = 0

                // Create each availability
                for (request in requests) {
                    try {
                        val response = api.createAvailability(request)
                        if (response.isSuccessful) {
                            successCount++
                        } else {
                            failCount++
                        }
                    } catch (e: Exception) {
                        failCount++
                    }
                }

                if (successCount > 0) {
                    hideAddDialog()
                    loadAvailabilities()
                    if (failCount > 0) {
                        _uiState.update {
                            it.copy(error = "Added $successCount slots, $failCount failed")
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(isAddingNew = false, error = "Failed to add time slots")
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

    fun updateAvailability(
        id: String,
        dayOfWeek: java.time.DayOfWeek,
        startTime: java.time.LocalTime,
        endTime: java.time.LocalTime
    ) {
        if (endTime <= startTime) {
            _uiState.update { it.copy(error = "End time must be after start time") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dayOfWeekValue = when (dayOfWeek.value) {
                    7 -> 0  // Sunday
                    else -> dayOfWeek.value
                }

                val request = CreateUserAvailabilityRequestDto(
                    dayOfWeek = dayOfWeekValue,
                    startTime = startTime.format(timeFormatter),
                    endTime = endTime.format(timeFormatter)
                )

                val response = api.updateAvailability(id, request)
                if (response.isSuccessful) {
                    loadAvailabilities()
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to update: $errorBody")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Bulk update all availabilities at once.
     * This replaces all existing availabilities with the provided list.
     */
    fun bulkUpdateAvailabilities(availabilities: List<AvailabilitySlot>) {
        // Validate no overlapping slots on the same day
        val hasOverlaps = hasOverlappingSlots(availabilities)
        if (hasOverlaps) {
            _uiState.update { it.copy(error = "Time slots cannot overlap on the same day") }
            return
        }

        // Validate each slot
        for (slot in availabilities) {
            if (slot.endTime <= slot.startTime) {
                _uiState.update { it.copy(error = "End time must be after start time for ${slot.dayOfWeek.name}") }
                return
            }
            val durationMinutes = java.time.Duration.between(slot.startTime, slot.endTime).toMinutes()
            if (durationMinutes < 15) {
                _uiState.update { it.copy(error = "Availability slot must be at least 15 minutes long") }
                return
            }
            if (durationMinutes > 480) { // 8 hours
                _uiState.update { it.copy(error = "Availability slot cannot exceed 8 hours") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val requests = availabilities.map { slot ->
                    val dayOfWeekValue = when (slot.dayOfWeek.value) {
                        7 -> 0  // Sunday
                        else -> slot.dayOfWeek.value
                    }
                    CreateUserAvailabilityRequestDto(
                        dayOfWeek = dayOfWeekValue,
                        startTime = slot.startTime.format(timeFormatter),
                        endTime = slot.endTime.format(timeFormatter)
                    )
                }

                val request = BulkUpdateUserAvailabilityRequestDto(availabilities = requests)
                val response = api.bulkUpdateAvailabilities(request)

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            availabilities = response.body() ?: emptyList()
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to update: $errorBody")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    private fun hasOverlappingSlots(availabilities: List<AvailabilitySlot>): Boolean {
        val groupedByDay = availabilities.groupBy { it.dayOfWeek }
        for ((_, slots) in groupedByDay) {
            if (slots.size < 2) continue
            val sortedSlots = slots.sortedBy { it.startTime }
            for (i in 0 until sortedSlots.size - 1) {
                if (sortedSlots[i].endTime > sortedSlots[i + 1].startTime) {
                    return true
                }
            }
        }
        return false
    }
}

/**
 * Data class representing an availability slot for bulk updates
 */
data class AvailabilitySlot(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)

