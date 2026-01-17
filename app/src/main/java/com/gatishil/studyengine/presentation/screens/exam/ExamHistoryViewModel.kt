package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.domain.repository.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamHistoryUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val attempts: List<ExamAttemptSummary> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: String? = null,
    val selectedStatus: ExamAttemptStatus? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExamHistoryViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamHistoryUiState())
    val uiState: StateFlow<ExamHistoryUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
        loadHistory()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            val result = examRepository.getSubjects()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    subjects = result.getOrNull() ?: emptyList()
                )
            }
        }
    }

    fun loadHistory(reset: Boolean = true) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.currentPage + 1

            _uiState.value = _uiState.value.copy(
                isLoading = reset,
                isLoadingMore = !reset
            )

            val result = examRepository.getExamHistory(
                subjectId = _uiState.value.selectedSubjectId,
                status = _uiState.value.selectedStatus,
                page = page,
                pageSize = 20
            )

            when (result) {
                is Resource.Success -> {
                    val data = result.data
                    val newAttempts = if (reset) data.items else _uiState.value.attempts + data.items

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        attempts = newAttempts,
                        currentPage = data.page,
                        totalPages = data.totalPages,
                        hasMore = data.page < data.totalPages
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.message ?: result.exception.message
                    )
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun setSubjectFilter(subjectId: String?) {
        _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId)
        loadHistory(reset = true)
    }

    fun setStatusFilter(status: ExamAttemptStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        loadHistory(reset = true)
    }

    fun loadMore() {
        if (!_uiState.value.isLoadingMore && _uiState.value.hasMore) {
            loadHistory(reset = false)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

