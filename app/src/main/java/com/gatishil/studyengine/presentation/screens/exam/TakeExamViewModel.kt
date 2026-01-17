package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.domain.repository.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

data class TakeExamUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val exam: ExamQuestionSet? = null,
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, List<String>> = emptyMap(),
    val remainingSeconds: Long? = null,
    val showSubmitDialog: Boolean = false,
    val showExitDialog: Boolean = false,
    val error: String? = null,
    val result: ExamResult? = null
)

@HiltViewModel
class TakeExamViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TakeExamUiState())
    val uiState: StateFlow<TakeExamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadExam()
    }

    fun loadExam() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = examRepository.getCurrentExam()

            when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            exam = result.data
                        )
                        startTimer()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No exam in progress"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "No exam in progress"
                    )
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    private fun startTimer() {
        val exam = _uiState.value.exam ?: return
        val expiresAt = exam.expiresAt ?: return

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val remaining = Duration.between(now, expiresAt).seconds

                if (remaining <= 0) {
                    // Time's up - auto submit
                    submitExam()
                    break
                }

                _uiState.value = _uiState.value.copy(remainingSeconds = remaining)
                delay(1000)
            }
        }
    }

    fun goToQuestion(index: Int) {
        val totalQuestions = _uiState.value.exam?.questions?.size ?: 0
        if (index in 0 until totalQuestions) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = index)
        }
    }

    fun nextQuestion() {
        goToQuestion(_uiState.value.currentQuestionIndex + 1)
    }

    fun previousQuestion() {
        goToQuestion(_uiState.value.currentQuestionIndex - 1)
    }

    fun selectOption(questionId: String, optionId: String, allowMultiple: Boolean) {
        val currentAnswers = _uiState.value.answers.toMutableMap()
        val currentSelection = currentAnswers[questionId]?.toMutableList() ?: mutableListOf()

        if (allowMultiple) {
            if (optionId in currentSelection) {
                currentSelection.remove(optionId)
            } else {
                currentSelection.add(optionId)
            }
        } else {
            currentSelection.clear()
            currentSelection.add(optionId)
        }

        currentAnswers[questionId] = currentSelection
        _uiState.value = _uiState.value.copy(answers = currentAnswers)
    }

    fun showSubmitConfirmation() {
        _uiState.value = _uiState.value.copy(showSubmitDialog = true)
    }

    fun hideSubmitConfirmation() {
        _uiState.value = _uiState.value.copy(showSubmitDialog = false)
    }

    fun showExitConfirmation() {
        _uiState.value = _uiState.value.copy(showExitDialog = true)
    }

    fun hideExitConfirmation() {
        _uiState.value = _uiState.value.copy(showExitDialog = false)
    }

    fun submitExam() {
        val exam = _uiState.value.exam ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                showSubmitDialog = false
            )
            timerJob?.cancel()

            val answers = _uiState.value.answers.map { (questionId, optionIds) ->
                SubmitAnswer(questionId, optionIds)
            }

            val request = SubmitExamRequest(
                examAttemptId = exam.examAttemptId,
                answers = answers
            )

            val result = examRepository.submitExam(request)

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        result = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = result.message ?: "Failed to submit exam"
                    )
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun cancelExam() {
        val exam = _uiState.value.exam ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showExitDialog = false)
            timerJob?.cancel()

            examRepository.cancelExam(exam.examAttemptId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

