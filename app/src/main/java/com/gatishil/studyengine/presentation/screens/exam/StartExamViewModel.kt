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

data class SubjectWithChapterSelection(
    val subject: Subject,
    val chapters: List<SubjectChapter> = emptyList(),
    val selectedChapterIds: Set<String> = emptySet(),
    val isExpanded: Boolean = false
)

data class StartExamUiState(
    val isLoading: Boolean = true,
    val isLoadingChapters: Boolean = false,
    val isStarting: Boolean = false,
    val subjectsWithChapters: List<SubjectWithChapterSelection> = emptyList(),
    val questionCount: Int = 10,
    val selectedDifficulty: QuestionDifficulty? = null,
    val timeLimitMinutes: Int? = null,
    val totalAvailableQuestionCount: Int = 0,
    val error: String? = null,
    val examStarted: ExamQuestionSet? = null
) {
    // Convenience property for compatibility
    val subjects: List<Subject>
        get() = subjectsWithChapters.map { it.subject }
}

@HiltViewModel
class StartExamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val subjectIdsString: String = savedStateHandle.get<String>("subjectIds") ?: ""
    private val subjectIds: List<String> = subjectIdsString.split(",").filter { it.isNotBlank() }

    // Parse chapter selections: format is "subjectId1:chapterId1,chapterId2|subjectId2:chapterId3"
    private val chapterSelectionsString: String = try {
        java.net.URLDecoder.decode(savedStateHandle.get<String>("chapterSelections") ?: "", "UTF-8")
    } catch (e: Exception) {
        ""
    }
    private val preselectedChapters: Map<String, Set<String>> = parseChapterSelections(chapterSelectionsString)

    private fun parseChapterSelections(selectionsString: String): Map<String, Set<String>> {
        if (selectionsString.isBlank()) return emptyMap()

        return selectionsString.split("|")
            .filter { it.isNotBlank() }
            .mapNotNull { selection ->
                val parts = selection.split(":")
                if (parts.size == 2) {
                    val subjectId = parts[0]
                    val chapterIds = parts[1].split(",").filter { it.isNotBlank() }.toSet()
                    if (chapterIds.isNotEmpty()) subjectId to chapterIds else null
                } else null
            }
            .toMap()
    }

    private val _uiState = MutableStateFlow(StartExamUiState())
    val uiState: StateFlow<StartExamUiState> = _uiState.asStateFlow()

    init {
        loadSubjectsWithChapters()
    }

    private fun loadSubjectsWithChapters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val subjectsWithChaptersList = mutableListOf<SubjectWithChapterSelection>()
            var totalQuestions = 0

            // Load each subject with its chapters
            for (subjectId in subjectIds) {
                val subjectWithChaptersResult = examRepository.getSubjectWithChapters(subjectId)

                // Get preselected chapters for this subject
                val preselectedChapterIds = preselectedChapters[subjectId] ?: emptySet()

                when (subjectWithChaptersResult) {
                    is Resource.Success -> {
                        val subjectWithChapters = subjectWithChaptersResult.data
                        val subject = Subject(
                            id = subjectWithChapters.id,
                            name = subjectWithChapters.name,
                            categoryId = subjectWithChapters.categoryId,
                            categoryName = subjectWithChapters.categoryName,
                            description = subjectWithChapters.description,
                            iconUrl = subjectWithChapters.iconUrl,
                            displayOrder = subjectWithChapters.displayOrder,
                            questionCount = subjectWithChapters.questionCount,
                            chapterCount = subjectWithChapters.chapters.size,
                            isActive = subjectWithChapters.isActive
                        )

                        // Use preselected chapters if available
                        val selectedChapters = if (preselectedChapterIds.isNotEmpty()) {
                            preselectedChapterIds
                        } else {
                            emptySet() // Empty means all chapters
                        }

                        // Calculate questions based on selection
                        val questionCount = if (selectedChapters.isEmpty()) {
                            subjectWithChapters.questionCount
                        } else {
                            subjectWithChapters.chapters
                                .filter { it.id in selectedChapters }
                                .sumOf { it.questionCount }
                        }

                        subjectsWithChaptersList.add(
                            SubjectWithChapterSelection(
                                subject = subject,
                                chapters = subjectWithChapters.chapters,
                                selectedChapterIds = selectedChapters,
                                isExpanded = preselectedChapterIds.isNotEmpty() // Expand if has preselected chapters
                            )
                        )
                        totalQuestions += questionCount
                    }
                    is Resource.Error -> {
                        // Try to load just the subject
                        val subjectResult = examRepository.getSubjectById(subjectId)
                        if (subjectResult is Resource.Success) {
                            subjectsWithChaptersList.add(
                                SubjectWithChapterSelection(
                                    subject = subjectResult.data,
                                    chapters = emptyList(),
                                    selectedChapterIds = emptySet(),
                                    isExpanded = false
                                )
                            )
                            totalQuestions += subjectResult.data.questionCount
                        }
                    }
                    is Resource.Loading -> { /* Skip */ }
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                subjectsWithChapters = subjectsWithChaptersList,
                totalAvailableQuestionCount = totalQuestions,
                questionCount = minOf(10, maxOf(1, totalQuestions))
            )
        }
    }

    fun toggleSubjectExpanded(subjectId: String) {
        _uiState.value = _uiState.value.copy(
            subjectsWithChapters = _uiState.value.subjectsWithChapters.map { swc ->
                if (swc.subject.id == subjectId) {
                    swc.copy(isExpanded = !swc.isExpanded)
                } else {
                    swc
                }
            }
        )
    }

    fun toggleChapterSelection(subjectId: String, chapterId: String) {
        _uiState.value = _uiState.value.copy(
            subjectsWithChapters = _uiState.value.subjectsWithChapters.map { swc ->
                if (swc.subject.id == subjectId) {
                    val newSelection = swc.selectedChapterIds.toMutableSet()
                    if (newSelection.contains(chapterId)) {
                        newSelection.remove(chapterId)
                    } else {
                        newSelection.add(chapterId)
                    }
                    swc.copy(selectedChapterIds = newSelection)
                } else {
                    swc
                }
            }
        )
        recalculateQuestionCount()
    }

    fun selectAllChapters(subjectId: String) {
        _uiState.value = _uiState.value.copy(
            subjectsWithChapters = _uiState.value.subjectsWithChapters.map { swc ->
                if (swc.subject.id == subjectId) {
                    swc.copy(selectedChapterIds = swc.chapters.map { it.id }.toSet())
                } else {
                    swc
                }
            }
        )
        recalculateQuestionCount()
    }

    fun deselectAllChapters(subjectId: String) {
        _uiState.value = _uiState.value.copy(
            subjectsWithChapters = _uiState.value.subjectsWithChapters.map { swc ->
                if (swc.subject.id == subjectId) {
                    swc.copy(selectedChapterIds = emptySet())
                } else {
                    swc
                }
            }
        )
        recalculateQuestionCount()
    }

    private fun recalculateQuestionCount() {
        var total = 0
        _uiState.value.subjectsWithChapters.forEach { swc ->
            if (swc.selectedChapterIds.isEmpty()) {
                // No chapters selected = include all questions from subject
                total += swc.subject.questionCount
            } else {
                // Sum question counts from selected chapters
                total += swc.chapters
                    .filter { it.id in swc.selectedChapterIds }
                    .sumOf { it.questionCount }
            }
        }
        _uiState.value = _uiState.value.copy(
            totalAvailableQuestionCount = total,
            questionCount = minOf(_uiState.value.questionCount, maxOf(1, total))
        )
    }

    fun setQuestionCount(count: Int) {
        val maxCount = maxOf(1, _uiState.value.totalAvailableQuestionCount)
        _uiState.value = _uiState.value.copy(
            questionCount = count.coerceIn(1, minOf(50, maxCount))
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

            // Build ExamSubjectSelection list with chapter filtering
            val subjectSelections = _uiState.value.subjectsWithChapters.map { swc ->
                ExamSubjectSelection(
                    subjectId = swc.subject.id,
                    chapterIds = if (swc.selectedChapterIds.isEmpty()) null else swc.selectedChapterIds.toList()
                )
            }

            val request = StartExamRequest(
                subjects = subjectSelections,
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
