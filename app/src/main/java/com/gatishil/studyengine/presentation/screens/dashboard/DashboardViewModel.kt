package com.gatishil.studyengine.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.mapper.StatsMapper
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.domain.model.Book
import com.gatishil.studyengine.domain.model.ExamAttemptSummary
import com.gatishil.studyengine.domain.model.PublicProfileCard
import com.gatishil.studyengine.domain.model.QuickStats
import com.gatishil.studyengine.domain.model.StudySession
import com.gatishil.studyengine.domain.model.Subject
import com.gatishil.studyengine.domain.repository.BookRepository
import com.gatishil.studyengine.domain.repository.ExamRepository
import com.gatishil.studyengine.domain.repository.ProfileRepository
import com.gatishil.studyengine.domain.repository.ReminderRepository
import com.gatishil.studyengine.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val todaySessions: List<StudySession> = emptyList(),
    val recentBooks: List<Book> = emptyList(),
    val upcomingSessionsCount: Int = 0,
    val todayCompletedCount: Int = 0,
    val totalPagesReadToday: Int = 0,
    val quickStats: QuickStats? = null,
    val relatedProfiles: List<PublicProfileCard> = emptyList(),
    val upcomingRemindersCount: Int = 0,
    // Exam data
    val recentExamAttempts: List<ExamAttemptSummary> = emptyList(),
    val popularSubjects: List<Subject> = emptyList(),
    val hasInProgressExam: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val reminderRepository: ReminderRepository,
    private val examRepository: ExamRepository,
    private val api: StudyEngineApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeData()
        syncDeviceTimezone()
    }

    /**
     * Sync device timezone with server to ensure session notifications are sent at correct times
     */
    private fun syncDeviceTimezone() {
        viewModelScope.launch {
            try {
                val deviceTimeZone = java.util.TimeZone.getDefault().id
                profileRepository.getProfile().collect { resource ->
                    if (resource is Resource.Success) {
                        val profile = resource.data
                        // Only update if timezone is different
                        if (profile.timeZone != deviceTimeZone) {
                            profileRepository.updateProfile(profile.name, deviceTimeZone)
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently ignore timezone sync errors
            }
        }
    }

    private fun observeData() {
        // Observe today's sessions
        viewModelScope.launch {
            sessionRepository.getTodaySessions().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val sessions = resource.data
                        val completedPages = sessions
                            .filter { it.isCompleted }
                            .sumOf { it.completedPages }

                        _uiState.update { state ->
                            state.copy(
                                todaySessions = sessions,
                                totalPagesReadToday = completedPages,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = resource.message, isLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }

        // Observe books
        viewModelScope.launch {
            bookRepository.getBooks().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                recentBooks = resource.data.take(5),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = resource.message, isLoading = false)
                        }
                    }
                    is Resource.Loading -> {
                        // Already handling loading
                    }
                }
            }
        }

        // Observe upcoming sessions count
        viewModelScope.launch {
            sessionRepository.getUpcomingSessionsCount().collect { count ->
                _uiState.update { it.copy(upcomingSessionsCount = count) }
            }
        }

        // Observe today's completed count
        viewModelScope.launch {
            sessionRepository.getTodayCompletedCount().collect { count ->
                _uiState.update { it.copy(todayCompletedCount = count) }
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Refresh from remote
            bookRepository.refreshBooks()
            sessionRepository.refreshSessions()
            loadQuickStats()
            loadRelatedProfiles()
            loadUpcomingReminders()
            loadExamData()
        }
    }

    private fun loadExamData() {
        viewModelScope.launch {
            try {
                // Check for in-progress exam
                val currentExamResult = examRepository.getCurrentExam()
                val hasInProgress = currentExamResult is Resource.Success && currentExamResult.data != null

                // Load recent exam attempts
                val historyResult = examRepository.getExamHistory(page = 1, pageSize = 3)
                val recentAttempts = if (historyResult is Resource.Success) {
                    historyResult.data.items
                } else {
                    emptyList()
                }

                // Load popular subjects
                val subjectsResult = examRepository.getSubjects()
                val subjects = if (subjectsResult is Resource.Success) {
                    subjectsResult.data.take(4)
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        hasInProgressExam = hasInProgress,
                        recentExamAttempts = recentAttempts,
                        popularSubjects = subjects
                    )
                }
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }

    private fun loadQuickStats() {
        viewModelScope.launch {
            try {
                val response = api.getQuickStats()
                if (response.isSuccessful) {
                    response.body()?.let { dto ->
                        val quickStats = with(StatsMapper) { dto.toDomain() }
                        _uiState.update {
                            it.copy(
                                quickStats = quickStats,
                                totalPagesReadToday = quickStats.todayPages
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fail for stats - not critical
            }
        }
    }

    private fun loadRelatedProfiles() {
        viewModelScope.launch {
            try {
                val result = profileRepository.discoverProfiles(
                    page = 1,
                    pageSize = 10
                )
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(relatedProfiles = result.data.profiles.take(5))
                        }
                    }
                    else -> {
                        // Silently fail - not critical
                    }
                }
            } catch (e: Exception) {
                // Silently fail for profiles - not critical
            }
        }
    }

    private fun loadUpcomingReminders() {
        viewModelScope.launch {
            try {
                val result = reminderRepository.getUpcomingReminders(10)
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(upcomingRemindersCount = result.data.totalCount)
                        }
                    }
                    else -> {
                        // Silently fail - not critical
                    }
                }
            } catch (e: Exception) {
                // Silently fail for reminders - not critical
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            bookRepository.refreshBooks()
            sessionRepository.refreshSessions()
            loadQuickStats()
            loadRelatedProfiles()
            loadUpcomingReminders()
            loadExamData()

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

