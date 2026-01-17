package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.ExamResult
import com.gatishil.studyengine.domain.repository.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamResultUiState(
    val isLoading: Boolean = true,
    val result: ExamResult? = null,
    val error: String? = null,
    val showAllAnswers: Boolean = false
)

@HiltViewModel
class ExamResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val examAttemptId: String = savedStateHandle.get<String>("examAttemptId") ?: ""

    private val _uiState = MutableStateFlow(ExamResultUiState())
    val uiState: StateFlow<ExamResultUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    private fun loadResult() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = examRepository.getExamResult(examAttemptId)

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        result = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load result"
                    )
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun toggleShowAllAnswers() {
        _uiState.value = _uiState.value.copy(
            showAllAnswers = !_uiState.value.showAllAnswers
        )
    }
}

