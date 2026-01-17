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

data class ExamListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val recentAttempts: List<ExamAttemptSummary> = emptyList(),
    val currentExam: ExamQuestionSet? = null,
    val error: String? = null
)

@HiltViewModel
class ExamListViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamListUiState())
    val uiState: StateFlow<ExamListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Load subjects
            val subjectsResult = examRepository.getSubjects()

            // Load recent attempts
            val historyResult = examRepository.getExamHistory(pageSize = 5)

            // Check for in-progress exam
            val currentExamResult = examRepository.getCurrentExam()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                subjects = subjectsResult.getOrNull() ?: emptyList(),
                recentAttempts = (historyResult as? Resource.Success)?.data?.items ?: emptyList(),
                currentExam = currentExamResult.getOrNull(),
                error = when (subjectsResult) {
                    is Resource.Error -> subjectsResult.message ?: subjectsResult.exception.message
                    else -> null
                }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}

