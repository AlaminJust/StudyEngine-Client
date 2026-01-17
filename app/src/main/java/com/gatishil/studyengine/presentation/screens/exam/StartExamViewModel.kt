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
    val subject: Subject? = null,
    val questionCount: Int = 10,
    val selectedDifficulty: QuestionDifficulty? = null,
    val timeLimitMinutes: Int? = null,
    val availableQuestionCount: Int = 0,
    val error: String? = null,
    val examStarted: ExamQuestionSet? = null
)

@HiltViewModel
class StartExamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val subjectId: String = savedStateHandle.get<String>("subjectId") ?: ""

    private val _uiState = MutableStateFlow(StartExamUiState())
    val uiState: StateFlow<StartExamUiState> = _uiState.asStateFlow()

    init {
        loadSubject()
    }

    private fun loadSubject() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = examRepository.getSubjectById(subjectId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        subject = result.data,
                        availableQuestionCount = result.data.questionCount
                    )
                    updateAvailableCount()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load subject"
                    )
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun setQuestionCount(count: Int) {
        _uiState.value = _uiState.value.copy(
            questionCount = count.coerceIn(5, minOf(50, _uiState.value.availableQuestionCount))
        )
    }

    fun setDifficulty(difficulty: QuestionDifficulty?) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
        updateAvailableCount()
    }

    fun setTimeLimit(minutes: Int?) {
        _uiState.value = _uiState.value.copy(timeLimitMinutes = minutes)
    }

    private fun updateAvailableCount() {
        viewModelScope.launch {
            val result = examRepository.getAvailableQuestionCount(
                subjectId = subjectId,
                difficulty = _uiState.value.selectedDifficulty
            )

            if (result.isSuccess) {
                val availableCount = result.getOrNull() ?: 0
                _uiState.value = _uiState.value.copy(
                    availableQuestionCount = availableCount,
                    questionCount = minOf(_uiState.value.questionCount, maxOf(5, availableCount))
                )
            }
        }
    }

    fun startExam() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isStarting = true, error = null)

            val request = StartExamRequest(
                subjectId = subjectId,
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

