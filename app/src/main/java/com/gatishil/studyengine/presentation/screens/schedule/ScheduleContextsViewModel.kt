package com.gatishil.studyengine.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.CreateScheduleContextRequestDto
import com.gatishil.studyengine.data.remote.dto.ScheduleContextDto
import com.gatishil.studyengine.data.remote.dto.UpdateLoadMultiplierRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ScheduleContextsUiState(
    val isLoading: Boolean = false,
    val contexts: List<ScheduleContextDto> = emptyList(),
    val activeContext: ScheduleContextDto? = null,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val contextType: String = "Normal",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusWeeks(1),
    val loadMultiplier: Float = 1.0f,
    val isAddingNew: Boolean = false
)

@HiltViewModel
class ScheduleContextsViewModel @Inject constructor(
    private val api: StudyEngineApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleContextsUiState())
    val uiState: StateFlow<ScheduleContextsUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_DATE

    companion object {
        val CONTEXT_TYPES = listOf(
            "Normal" to 1.0f,
            "Exam Period" to 1.5f,
            "Light Study" to 0.5f,
            "Vacation" to 0.0f,
            "Intensive" to 2.0f
        )
    }

    init {
        loadContexts()
    }

    fun loadContexts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getScheduleContexts()
                if (response.isSuccessful) {
                    val contexts = response.body()?.sortedByDescending { c -> c.startDate } ?: emptyList()

                    // Try to get active context
                    val activeResponse = api.getActiveScheduleContext()
                    val activeContext = if (activeResponse.isSuccessful) activeResponse.body() else null

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            contexts = contexts,
                            activeContext = activeContext
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
                contextType = "Normal",
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusWeeks(1),
                loadMultiplier = 1.0f
            )
        }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun updateContextType(type: String) {
        val multiplier = CONTEXT_TYPES.find { it.first == type }?.second ?: 1.0f
        _uiState.update { it.copy(contextType = type, loadMultiplier = multiplier) }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun updateLoadMultiplier(multiplier: Float) {
        _uiState.update { it.copy(loadMultiplier = multiplier) }
    }

    fun addContext() {
        val state = _uiState.value

        if (state.endDate <= state.startDate) {
            _uiState.update { it.copy(error = "End date must be after start date") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingNew = true, error = null) }
            try {
                val request = CreateScheduleContextRequestDto(
                    contextType = state.contextType,
                    startDate = state.startDate.format(dateFormatter),
                    endDate = state.endDate.format(dateFormatter),
                    loadMultiplier = state.loadMultiplier
                )

                val response = api.createScheduleContext(request)
                if (response.isSuccessful) {
                    hideAddDialog()
                    loadContexts()
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

    fun updateContextLoadMultiplier(id: String, multiplier: Float) {
        viewModelScope.launch {
            try {
                val request = UpdateLoadMultiplierRequestDto(loadMultiplier = multiplier)
                val response = api.updateScheduleContextLoadMultiplier(id, request)
                if (response.isSuccessful) {
                    loadContexts()
                } else {
                    _uiState.update { it.copy(error = "Failed to update") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteContext(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = api.deleteScheduleContext(id)
                if (response.isSuccessful) {
                    loadContexts()
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

