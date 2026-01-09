package com.gatishil.studyengine.presentation.screens.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.Book
import com.gatishil.studyengine.domain.model.CreateRecurrenceRuleRequest
import com.gatishil.studyengine.domain.model.CreateStudyPlanRequest
import com.gatishil.studyengine.domain.model.RecurrenceType
import com.gatishil.studyengine.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class CreateStudyPlanUiState(
    val isLoading: Boolean = false,
    val bookId: String = "",
    val book: Book? = null,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusWeeks(4),
    val recurrenceType: RecurrenceType = RecurrenceType.DAILY,
    val interval: Int = 1,
    val selectedDaysOfWeek: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    ),
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class CreateStudyPlanViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateStudyPlanUiState())
    val uiState: StateFlow<CreateStudyPlanUiState> = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("bookId")?.let { bookId ->
            _uiState.update { it.copy(bookId = bookId) }
            loadBook(bookId)
        }
    }

    private fun loadBook(bookId: String) {
        viewModelScope.launch {
            when (val result = bookRepository.getBookById(bookId)) {
                is Resource.Success -> {
                    val book = result.data
                    // If book has target end date, use it
                    val endDate = book.targetEndDate ?: LocalDate.now().plusWeeks(4)
                    _uiState.update {
                        it.copy(
                            book = book,
                            endDate = endDate
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> { /* Loading */ }
            }
        }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                startDate = date,
                // Ensure end date is after start date
                endDate = if (it.endDate.isBefore(date)) date.plusWeeks(1) else it.endDate
            )
        }
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun updateRecurrenceType(type: RecurrenceType) {
        _uiState.update { it.copy(recurrenceType = type) }
    }

    fun updateInterval(interval: Int) {
        _uiState.update { it.copy(interval = interval.coerceIn(1, 7)) }
    }

    fun toggleDayOfWeek(day: DayOfWeek) {
        _uiState.update {
            val currentDays = it.selectedDaysOfWeek.toMutableSet()
            if (currentDays.contains(day)) {
                // Don't allow removing the last day
                if (currentDays.size > 1) {
                    currentDays.remove(day)
                }
            } else {
                currentDays.add(day)
            }
            it.copy(selectedDaysOfWeek = currentDays)
        }
    }

    fun createStudyPlan() {
        val state = _uiState.value
        val today = LocalDate.now()

        // Validation
        if (state.startDate.isBefore(today)) {
            _uiState.update { it.copy(error = "Start date must be today or in the future") }
            return
        }

        if (state.endDate.isBefore(state.startDate)) {
            _uiState.update { it.copy(error = "End date must be after start date") }
            return
        }

        if (state.endDate.isEqual(state.startDate)) {
            _uiState.update { it.copy(error = "Study plan must span at least one day") }
            return
        }

        // For Weekly/Custom, ensure at least one day is selected
        if (state.recurrenceType != RecurrenceType.DAILY && state.selectedDaysOfWeek.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one day of the week") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val recurrenceRule = when (state.recurrenceType) {
                RecurrenceType.DAILY -> CreateRecurrenceRuleRequest(
                    type = RecurrenceType.DAILY,
                    interval = state.interval
                )
                RecurrenceType.WEEKLY -> CreateRecurrenceRuleRequest(
                    type = RecurrenceType.WEEKLY,
                    interval = state.interval,
                    daysOfWeek = state.selectedDaysOfWeek.toList()
                )
                RecurrenceType.CUSTOM -> CreateRecurrenceRuleRequest(
                    type = RecurrenceType.CUSTOM,
                    interval = state.interval,
                    daysOfWeek = state.selectedDaysOfWeek.toList()
                )
            }

            val request = CreateStudyPlanRequest(
                startDate = state.startDate,
                endDate = state.endDate,
                recurrenceRule = recurrenceRule
            )

            when (val result = bookRepository.createStudyPlan(state.bookId, request)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    val errorMessage = when {
                        result.message?.contains("400") == true ->
                            "Bad request. Please check dates and recurrence settings."
                        result.message?.contains("401") == true ->
                            "Authentication required. Please sign in again."
                        result.message?.contains("404") == true ->
                            "Book not found."
                        result.message?.contains("409") == true ->
                            "A study plan already exists for this book."
                        result.message?.contains("422") == true ->
                            "Invalid data. Please check the dates and try again."
                        else -> result.message ?: "Failed to create study plan"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
                is Resource.Loading -> { /* Already loading */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

