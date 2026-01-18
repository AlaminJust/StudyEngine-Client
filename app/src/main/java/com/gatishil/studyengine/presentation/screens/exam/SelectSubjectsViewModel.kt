package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.Category
import com.gatishil.studyengine.domain.model.CategoryWithSubjects
import com.gatishil.studyengine.domain.model.Subject
import com.gatishil.studyengine.domain.model.SubjectChapter
import com.gatishil.studyengine.domain.model.SubjectWithChapters
import com.gatishil.studyengine.domain.repository.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectSelectionItem(
    val subject: Subject,
    val chapters: List<SubjectChapter> = emptyList(),
    val selectedChapterIds: Set<String> = emptySet(),
    val isExpanded: Boolean = false,
    val isSelected: Boolean = false
)

data class CategorySelectionItem(
    val category: CategoryWithSubjects,
    val subjectItems: List<SubjectSelectionItem>,
    val isExpanded: Boolean = true
)

data class SelectSubjectsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val categoryItems: List<CategorySelectionItem> = emptyList(),
    val subjectItems: List<SubjectSelectionItem> = emptyList(), // For non-categorized subjects
    val error: String? = null
) {
    // Convenience properties
    val categories: List<CategoryWithSubjects>
        get() = categoryItems.map { it.category }

    val subjects: List<Subject>
        get() = if (categoryItems.isNotEmpty()) {
            categoryItems.flatMap { it.subjectItems.map { si -> si.subject } }
        } else {
            subjectItems.map { it.subject }
        }

    val selectedSubjectIds: Set<String>
        get() = if (categoryItems.isNotEmpty()) {
            categoryItems.flatMap { ci -> ci.subjectItems.filter { it.isSelected }.map { it.subject.id } }.toSet()
        } else {
            subjectItems.filter { it.isSelected }.map { it.subject.id }.toSet()
        }

    val expandedCategoryIds: Set<String>
        get() = categoryItems.filter { it.isExpanded }.map { it.category.id }.toSet()
}

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
                categoriesResult is Resource.Success && categoriesResult.data.isNotEmpty() -> {
                    val categories = categoriesResult.data

                    // Load chapters for each subject
                    val categoryItems = categories.map { category ->
                        val subjectItems = category.subjects.map { subject ->
                            // Try to load chapters for this subject
                            val chaptersResult = examRepository.getSubjectChapters(subject.id)
                            val chapters = if (chaptersResult is Resource.Success) {
                                chaptersResult.data
                            } else {
                                emptyList()
                            }

                            SubjectSelectionItem(
                                subject = subject,
                                chapters = chapters,
                                selectedChapterIds = emptySet(),
                                isExpanded = false,
                                isSelected = false
                            )
                        }

                        CategorySelectionItem(
                            category = category,
                            subjectItems = subjectItems,
                            isExpanded = true
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categoryItems = categoryItems
                    )
                }
                subjectsResult is Resource.Success -> {
                    // Load chapters for each subject
                    val subjectItems = subjectsResult.data.map { subject ->
                        val chaptersResult = examRepository.getSubjectChapters(subject.id)
                        val chapters = if (chaptersResult is Resource.Success) {
                            chaptersResult.data
                        } else {
                            emptyList()
                        }

                        SubjectSelectionItem(
                            subject = subject,
                            chapters = chapters,
                            selectedChapterIds = emptySet(),
                            isExpanded = false,
                            isSelected = false
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        subjectItems = subjectItems
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
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                if (ci.category.id == categoryId) {
                    ci.copy(isExpanded = !ci.isExpanded)
                } else ci
            }
        )
    }

    fun toggleSubjectSelection(subjectId: String) {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                ci.copy(
                    subjectItems = ci.subjectItems.map { si ->
                        if (si.subject.id == subjectId) {
                            si.copy(isSelected = !si.isSelected)
                        } else si
                    }
                )
            },
            subjectItems = _uiState.value.subjectItems.map { si ->
                if (si.subject.id == subjectId) {
                    si.copy(isSelected = !si.isSelected)
                } else si
            }
        )
    }

    fun toggleSubjectExpanded(subjectId: String) {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                ci.copy(
                    subjectItems = ci.subjectItems.map { si ->
                        if (si.subject.id == subjectId) {
                            si.copy(isExpanded = !si.isExpanded)
                        } else si
                    }
                )
            },
            subjectItems = _uiState.value.subjectItems.map { si ->
                if (si.subject.id == subjectId) {
                    si.copy(isExpanded = !si.isExpanded)
                } else si
            }
        )
    }

    fun toggleChapterSelection(subjectId: String, chapterId: String) {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                ci.copy(
                    subjectItems = ci.subjectItems.map { si ->
                        if (si.subject.id == subjectId) {
                            val newSelection = si.selectedChapterIds.toMutableSet()
                            if (newSelection.contains(chapterId)) {
                                newSelection.remove(chapterId)
                            } else {
                                newSelection.add(chapterId)
                            }
                            si.copy(selectedChapterIds = newSelection)
                        } else si
                    }
                )
            },
            subjectItems = _uiState.value.subjectItems.map { si ->
                if (si.subject.id == subjectId) {
                    val newSelection = si.selectedChapterIds.toMutableSet()
                    if (newSelection.contains(chapterId)) {
                        newSelection.remove(chapterId)
                    } else {
                        newSelection.add(chapterId)
                    }
                    si.copy(selectedChapterIds = newSelection)
                } else si
            }
        )
    }

    fun selectAllChapters(subjectId: String) {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                ci.copy(
                    subjectItems = ci.subjectItems.map { si ->
                        if (si.subject.id == subjectId) {
                            si.copy(selectedChapterIds = si.chapters.map { it.id }.toSet())
                        } else si
                    }
                )
            },
            subjectItems = _uiState.value.subjectItems.map { si ->
                if (si.subject.id == subjectId) {
                    si.copy(selectedChapterIds = si.chapters.map { it.id }.toSet())
                } else si
            }
        )
    }

    fun deselectAllChapters(subjectId: String) {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                ci.copy(
                    subjectItems = ci.subjectItems.map { si ->
                        if (si.subject.id == subjectId) {
                            si.copy(selectedChapterIds = emptySet())
                        } else si
                    }
                )
            },
            subjectItems = _uiState.value.subjectItems.map { si ->
                if (si.subject.id == subjectId) {
                    si.copy(selectedChapterIds = emptySet())
                } else si
            }
        )
    }

    fun selectAllSubjectsInCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                if (ci.category.id == categoryId) {
                    ci.copy(
                        subjectItems = ci.subjectItems.map { si ->
                            si.copy(isSelected = true)
                        }
                    )
                } else ci
            }
        )
    }

    fun deselectAllSubjectsInCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                if (ci.category.id == categoryId) {
                    ci.copy(
                        subjectItems = ci.subjectItems.map { si ->
                            si.copy(isSelected = false)
                        }
                    )
                } else ci
            }
        )
    }

    fun selectAllSubjects() {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                ci.copy(
                    subjectItems = ci.subjectItems.map { si ->
                        si.copy(isSelected = true)
                    }
                )
            },
            subjectItems = _uiState.value.subjectItems.map { si ->
                si.copy(isSelected = true)
            }
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            categoryItems = _uiState.value.categoryItems.map { ci ->
                ci.copy(
                    subjectItems = ci.subjectItems.map { si ->
                        si.copy(isSelected = false, selectedChapterIds = emptySet())
                    }
                )
            },
            subjectItems = _uiState.value.subjectItems.map { si ->
                si.copy(isSelected = false, selectedChapterIds = emptySet())
            }
        )
    }

    fun getSelectedSubjectIds(): List<String> {
        return _uiState.value.selectedSubjectIds.toList()
    }

    fun getSelectedSubjectsWithChapters(): List<Pair<String, List<String>?>> {
        val result = mutableListOf<Pair<String, List<String>?>>()

        if (_uiState.value.categoryItems.isNotEmpty()) {
            _uiState.value.categoryItems.forEach { ci ->
                ci.subjectItems.filter { it.isSelected }.forEach { si ->
                    val chapterIds = if (si.selectedChapterIds.isEmpty()) null else si.selectedChapterIds.toList()
                    result.add(Pair(si.subject.id, chapterIds))
                }
            }
        } else {
            _uiState.value.subjectItems.filter { it.isSelected }.forEach { si ->
                val chapterIds = if (si.selectedChapterIds.isEmpty()) null else si.selectedChapterIds.toList()
                result.add(Pair(si.subject.id, chapterIds))
            }
        }

        return result
    }

    fun getTotalQuestionCount(): Int {
        var total = 0

        if (_uiState.value.categoryItems.isNotEmpty()) {
            _uiState.value.categoryItems.forEach { ci ->
                ci.subjectItems.filter { it.isSelected }.forEach { si ->
                    total += if (si.selectedChapterIds.isEmpty()) {
                        si.subject.questionCount
                    } else {
                        si.chapters.filter { it.id in si.selectedChapterIds }.sumOf { it.questionCount }
                    }
                }
            }
        } else {
            _uiState.value.subjectItems.filter { it.isSelected }.forEach { si ->
                total += if (si.selectedChapterIds.isEmpty()) {
                    si.subject.questionCount
                } else {
                    si.chapters.filter { it.id in si.selectedChapterIds }.sumOf { it.questionCount }
                }
            }
        }

        return total
    }

    fun isAllSelectedInCategory(categoryId: String): Boolean {
        val category = _uiState.value.categoryItems.find { it.category.id == categoryId }
        return category?.subjectItems?.all { it.isSelected } ?: false
    }

    fun getSelectedCountInCategory(categoryId: String): Int {
        val category = _uiState.value.categoryItems.find { it.category.id == categoryId }
        return category?.subjectItems?.count { it.isSelected } ?: 0
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
