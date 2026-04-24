package com.gatishil.studyengine.presentation.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.domain.repository.ExamRepository
import com.gatishil.studyengine.domain.repository.LiveExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val completedLiveExams: List<CompletedLiveExam> = emptyList(),
    val isJoiningLiveExam: Boolean = false,
    val joinedExam: ExamQuestionSet? = null,
    val joinError: String? = null,
    val selectedStandings: LiveExamStandings? = null,
    val isLoadingStandings: Boolean = false,
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
        viewModelScope.launch { fetchData() }
    }

    private suspend fun fetchData() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        coroutineScope {
            // Fire all 6 requests in parallel
            val categoriesDeferred    = async { examRepository.getCategoriesWithSubjects() }
            val subjectsDeferred      = async { examRepository.getSubjects() }
            val historyDeferred       = async { examRepository.getExamHistory(pageSize = 5) }
            val currentExamDeferred   = async { examRepository.getCurrentExam() }
            val liveExamsDeferred     = async { liveExamRepository.getLiveExams() }
            val completedDeferred     = async { liveExamRepository.getCompletedLiveExams() }

            val categoriesResult  = categoriesDeferred.await()
            val subjectsResult    = subjectsDeferred.await()
            val historyResult     = historyDeferred.await()
            val currentExamResult = currentExamDeferred.await()
            val liveExamsResult   = liveExamsDeferred.await()
            val completedResult   = completedDeferred.await()

            val categories = categoriesResult.getOrNull() ?: emptyList()
            val subjects   = subjectsResult.getOrNull() ?: emptyList()

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
                completedLiveExams = completedResult.getOrNull() ?: emptyList(),
                error = when {
                    categoriesResult is Resource.Error -> categoriesResult.message ?: categoriesResult.exception.message
                    subjectsResult is Resource.Error -> subjectsResult.message ?: subjectsResult.exception.message
                    else -> null
                }
            )
        }
    }

    fun joinLiveExam(liveExamId: String, accessCode: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isJoiningLiveExam = true, joinError = null)

            when (val result = liveExamRepository.joinLiveExam(liveExamId, accessCode)) {
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

    fun loadStandings(examId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStandings = true)
            when (val result = liveExamRepository.getLiveExamStandings(examId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingStandings = false,
                        selectedStandings = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingStandings = false
                    )
                }
                else -> {}
            }
        }
    }

    fun dismissStandings() {
        _uiState.value = _uiState.value.copy(selectedStandings = null)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            fetchData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
