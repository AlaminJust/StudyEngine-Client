package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.Subject
import com.gatishil.studyengine.domain.repository.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectSubjectsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectIds: Set<String> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class SelectSubjectsViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectSubjectsUiState())
    val uiState: StateFlow<SelectSubjectsUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = examRepository.getSubjects()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        subjects = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load subjects"
                    )
                }
                is Resource.Loading -> { /* Already handled */ }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadSubjects()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun toggleSubjectSelection(subjectId: String) {
        val currentSelection = _uiState.value.selectedSubjectIds.toMutableSet()
        if (currentSelection.contains(subjectId)) {
            currentSelection.remove(subjectId)
        } else {
            currentSelection.add(subjectId)
        }
        _uiState.value = _uiState.value.copy(selectedSubjectIds = currentSelection)
    }

    fun selectAllSubjects() {
        _uiState.value = _uiState.value.copy(
            selectedSubjectIds = _uiState.value.subjects.map { it.id }.toSet()
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedSubjectIds = emptySet())
    }

    fun getSelectedSubjectIds(): List<String> {
        return _uiState.value.selectedSubjectIds.toList()
    }

    fun getTotalQuestionCount(): Int {
        return _uiState.value.subjects
            .filter { it.id in _uiState.value.selectedSubjectIds }
            .sumOf { it.questionCount }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

