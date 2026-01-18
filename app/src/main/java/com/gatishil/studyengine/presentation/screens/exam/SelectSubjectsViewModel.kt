package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.Category
import com.gatishil.studyengine.domain.model.CategoryWithSubjects
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
    val categories: List<CategoryWithSubjects> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectIds: Set<String> = emptySet(),
    val expandedCategoryIds: Set<String> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class SelectSubjectsViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectSubjectsUiState())
    val uiState: StateFlow<SelectSubjectsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Load categories with subjects
            val categoriesResult = examRepository.getCategoriesWithSubjects()
            // Also load all subjects as fallback
            val subjectsResult = examRepository.getSubjects()

            when {
                categoriesResult is Resource.Success -> {
                    val categories = categoriesResult.data
                    // Expand all categories by default
                    val expandedIds = categories.map { it.id }.toSet()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = categories,
                        subjects = subjectsResult.getOrNull() ?: emptyList(),
                        expandedCategoryIds = expandedIds
                    )
                }
                subjectsResult is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = emptyList(),
                        subjects = subjectsResult.data
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = (categoriesResult as? Resource.Error)?.message
                            ?: (subjectsResult as? Resource.Error)?.message
                            ?: "Failed to load subjects"
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun toggleCategoryExpanded(categoryId: String) {
        val currentExpanded = _uiState.value.expandedCategoryIds.toMutableSet()
        if (currentExpanded.contains(categoryId)) {
            currentExpanded.remove(categoryId)
        } else {
            currentExpanded.add(categoryId)
        }
        _uiState.value = _uiState.value.copy(expandedCategoryIds = currentExpanded)
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

    fun selectAllSubjectsInCategory(categoryId: String) {
        val category = _uiState.value.categories.find { it.id == categoryId }
        if (category != null) {
            val currentSelection = _uiState.value.selectedSubjectIds.toMutableSet()
            currentSelection.addAll(category.subjects.map { it.id })
            _uiState.value = _uiState.value.copy(selectedSubjectIds = currentSelection)
        }
    }

    fun deselectAllSubjectsInCategory(categoryId: String) {
        val category = _uiState.value.categories.find { it.id == categoryId }
        if (category != null) {
            val currentSelection = _uiState.value.selectedSubjectIds.toMutableSet()
            currentSelection.removeAll(category.subjects.map { it.id }.toSet())
            _uiState.value = _uiState.value.copy(selectedSubjectIds = currentSelection)
        }
    }

    fun selectAllSubjects() {
        val allSubjectIds = if (_uiState.value.categories.isNotEmpty()) {
            _uiState.value.categories.flatMap { it.subjects }.map { it.id }.toSet()
        } else {
            _uiState.value.subjects.map { it.id }.toSet()
        }
        _uiState.value = _uiState.value.copy(selectedSubjectIds = allSubjectIds)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedSubjectIds = emptySet())
    }

    fun getSelectedSubjectIds(): List<String> {
        return _uiState.value.selectedSubjectIds.toList()
    }

    fun getTotalQuestionCount(): Int {
        val selectedIds = _uiState.value.selectedSubjectIds
        return if (_uiState.value.categories.isNotEmpty()) {
            _uiState.value.categories
                .flatMap { it.subjects }
                .filter { it.id in selectedIds }
                .sumOf { it.questionCount }
        } else {
            _uiState.value.subjects
                .filter { it.id in selectedIds }
                .sumOf { it.questionCount }
        }
    }

    fun isAllSelectedInCategory(categoryId: String): Boolean {
        val category = _uiState.value.categories.find { it.id == categoryId }
        return category?.subjects?.all { it.id in _uiState.value.selectedSubjectIds } ?: false
    }

    fun getSelectedCountInCategory(categoryId: String): Int {
        val category = _uiState.value.categories.find { it.id == categoryId }
        return category?.subjects?.count { it.id in _uiState.value.selectedSubjectIds } ?: 0
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
