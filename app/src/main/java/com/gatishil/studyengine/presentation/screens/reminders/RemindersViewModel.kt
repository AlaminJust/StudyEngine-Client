package com.gatishil.studyengine.presentation.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.CustomReminder
import com.gatishil.studyengine.domain.model.ReminderStatus
import com.gatishil.studyengine.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class RemindersUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val reminders: List<CustomReminder> = emptyList(),
    val upcomingReminders: List<CustomReminder> = emptyList(),
    val pastReminders: List<CustomReminder> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingReminder: CustomReminder? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = reminderRepository.getAllReminders(50)) {
                is Resource.Success -> {
                    val allReminders = result.data.reminders
                    val now = LocalDateTime.now()

                    val upcoming = allReminders.filter {
                        it.status == ReminderStatus.Pending && it.scheduledFor.isAfter(now)
                    }.sortedBy { it.scheduledFor }

                    val past = allReminders.filter {
                        it.status != ReminderStatus.Pending || it.scheduledFor.isBefore(now)
                    }.sortedByDescending { it.scheduledFor }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reminders = allReminders,
                            upcomingReminders = upcoming,
                            pastReminders = past
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
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
            loadReminders()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(reminder: CustomReminder) {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                editingReminder = reminder
            )
        }
    }

    fun hideEditDialog() {
        _uiState.update {
            it.copy(
                showEditDialog = false,
                editingReminder = null
            )
        }
    }

    fun createReminder(title: String, message: String, scheduledFor: LocalDateTime) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            when (val result = reminderRepository.createReminder(title, message, scheduledFor)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showAddDialog = false,
                            successMessage = "Reminder created successfully"
                        )
                    }
                    loadReminders()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun updateReminder(id: String, title: String?, message: String?, scheduledFor: LocalDateTime?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            when (val result = reminderRepository.updateReminder(id, title, message, scheduledFor)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showEditDialog = false,
                            editingReminder = null,
                            successMessage = "Reminder updated successfully"
                        )
                    }
                    loadReminders()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            when (val result = reminderRepository.deleteReminder(id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Reminder deleted successfully"
                        )
                    }
                    loadReminders()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

