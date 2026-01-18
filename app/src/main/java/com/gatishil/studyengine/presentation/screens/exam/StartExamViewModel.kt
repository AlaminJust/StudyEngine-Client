package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.SavedStateHandle
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

data class StartExamUiState(
    val isLoading: Boolean = true,
    val isStarting: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val questionCount: Int = 10,
    val selectedDifficulty: QuestionDifficulty? = null,
    val timeLimitMinutes: Int? = null,
    val totalAvailableQuestionCount: Int = 0,
    val error: String? = null,
    val examStarted: ExamQuestionSet? = null
)

@HiltViewModel
class StartExamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val subjectIdsString: String = savedStateHandle.get<String>("subjectIds") ?: ""
    private val subjectIds: List<String> = subjectIdsString.split(",").filter { it.isNotBlank() }

    private val _uiState = MutableStateFlow(StartExamUiState())
    val uiState: StateFlow<StartExamUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val allSubjectsResult = examRepository.getSubjects()

            when (allSubjectsResult) {
                is Resource.Success -> {
                    val selectedSubjects = allSubjectsResult.data.filter { it.id in subjectIds }
                    val totalQuestions = selectedSubjects.sumOf { it.questionCount }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        subjects = selectedSubjects,
                        totalAvailableQuestionCount = totalQuestions,
                        questionCount = minOf(10, totalQuestions)
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = allSubjectsResult.message ?: "Failed to load subjects"
                    )
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun setQuestionCount(count: Int) {
        _uiState.value = _uiState.value.copy(
            questionCount = count.coerceIn(5, minOf(50, _uiState.value.totalAvailableQuestionCount))
        )
    }

    fun setDifficulty(difficulty: QuestionDifficulty?) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
    }

    fun setTimeLimit(minutes: Int?) {
        _uiState.value = _uiState.value.copy(timeLimitMinutes = minutes)
    }

    fun startExam() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isStarting = true, error = null)

            val request = StartExamRequest(
                subjectIds = subjectIds,
                questionCount = _uiState.value.questionCount,
                difficultyFilter = _uiState.value.selectedDifficulty,
                timeLimitMinutes = _uiState.value.timeLimitMinutes
            )

            val result = examRepository.startExam(request)

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isStarting = false,
                        examStarted = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isStarting = false,
                        error = result.message ?: "Failed to start exam"
                    )
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

