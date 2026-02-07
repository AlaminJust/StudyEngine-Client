package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.domain.repository.ExamRepository
import com.gatishil.studyengine.domain.repository.LiveExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val categories: List<CategoryWithSubjects> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val recentAttempts: List<ExamAttemptSummary> = emptyList(),
    val currentExam: ExamQuestionSet? = null,
    val liveExams: List<LiveExam> = emptyList(),
    val isJoiningLiveExam: Boolean = false,
    val joinedExam: ExamQuestionSet? = null,
    val joinError: String? = null,
    val error: String? = null
)

@HiltViewModel
class ExamListViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val liveExamRepository: LiveExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamListUiState())
    val uiState: StateFlow<ExamListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Load categories with subjects
            val categoriesResult = examRepository.getCategoriesWithSubjects()

            // Load all subjects as fallback and for counting
            val subjectsResult = examRepository.getSubjects()

            // Load recent attempts
            val historyResult = examRepository.getExamHistory(pageSize = 5)

            // Check for in-progress exam
            val currentExamResult = examRepository.getCurrentExam()

            // Load live exams
            val liveExamsResult = liveExamRepository.getLiveExams()

            val categories = categoriesResult.getOrNull() ?: emptyList()
            val subjects = subjectsResult.getOrNull() ?: emptyList()

            // Show active and scheduled live exams
            val liveExams = (liveExamsResult.getOrNull() ?: emptyList())
                .filter { it.status == LiveExamStatus.ACTIVE || it.status == LiveExamStatus.SCHEDULED }
                .sortedWith(compareBy<LiveExam> { it.status != LiveExamStatus.ACTIVE }.thenBy { it.scheduledStartTime })

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                categories = categories,
                subjects = subjects,
                recentAttempts = (historyResult as? Resource.Success)?.data?.items ?: emptyList(),
                currentExam = currentExamResult.getOrNull(),
                liveExams = liveExams,
                error = when {
                    categoriesResult is Resource.Error -> categoriesResult.message ?: categoriesResult.exception.message
                    subjectsResult is Resource.Error -> subjectsResult.message ?: subjectsResult.exception.message
                    else -> null
                }
            )
        }
    }

    fun joinLiveExam(liveExamId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isJoiningLiveExam = true, joinError = null)

            when (val result = liveExamRepository.joinLiveExam(liveExamId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isJoiningLiveExam = false,
                        joinedExam = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isJoiningLiveExam = false,
                        joinError = result.message ?: "Failed to join live exam"
                    )
                }
                else -> {}
            }
        }
    }

    fun clearJoinedExam() {
        _uiState.value = _uiState.value.copy(joinedExam = null)
    }

    fun clearJoinError() {
        _uiState.value = _uiState.value.copy(joinError = null)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
