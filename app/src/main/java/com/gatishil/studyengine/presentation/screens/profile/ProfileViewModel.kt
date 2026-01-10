package com.gatishil.studyengine.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.UserPreferences
import com.gatishil.studyengine.domain.model.UserProfile
import com.gatishil.studyengine.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val isUpdating: Boolean = false,

    // Edit Name Dialog
    val editNameDialogVisible: Boolean = false,
    val editName: String = "",

    // Edit Study Goals Dialog
    val editStudyGoalsDialogVisible: Boolean = false,
    val editDailyPagesGoal: String = "",
    val editDailyMinutesGoal: String = "",
    val editWeeklyStudyDaysGoal: String = "",

    // Edit Reading Speed Dialog
    val editReadingSpeedDialogVisible: Boolean = false,
    val editPagesPerHour: String = "",

    // Edit Session Preferences Dialog
    val editSessionPrefsDialogVisible: Boolean = false,
    val editPreferredSessionDuration: String = "",
    val editMinSessionDuration: String = "",
    val editMaxSessionDuration: String = "",

    // Edit Notification Preferences Dialog
    val editNotificationPrefsDialogVisible: Boolean = false,
    val editEnableSessionReminders: Boolean = true,
    val editReminderMinutesBefore: String = "",
    val editEnableStreakReminders: Boolean = true,
    val editEnableWeeklyDigest: Boolean = true,
    val editEnableAchievementNotifications: Boolean = true,

    // Edit Privacy Settings Dialog
    val editPrivacyDialogVisible: Boolean = false,
    val editShowProfilePublicly: Boolean = false,
    val editShowStatsPublicly: Boolean = false
)

sealed class ProfileEvent {
    data object ProfileUpdated : ProfileEvent()
    data object PreferencesUpdated : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            profileRepository.getProfile().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                profile = resource.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = resource.message ?: "Failed to load profile"
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadProfile()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ==================== Name Edit ====================

    fun showEditNameDialog() {
        _uiState.update {
            it.copy(
                editNameDialogVisible = true,
                editName = it.profile?.name ?: ""
            )
        }
    }

    fun hideEditNameDialog() {
        _uiState.update { it.copy(editNameDialogVisible = false) }
    }

    fun updateEditName(name: String) {
        _uiState.update { it.copy(editName = name) }
    }

    fun saveProfileName() {
        val name = _uiState.value.editName
        val timeZone = _uiState.value.profile?.timeZone ?: "UTC"
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateProfile(name, timeZone)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isUpdating = false, editNameDialogVisible = false, profile = result.data)
                    }
                    _events.emit(ProfileEvent.ProfileUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(ProfileEvent.Error(result.message ?: "Failed to update profile"))
                }
                else -> {}
            }
        }
    }

    // ==================== Study Goals Edit ====================

    fun showEditStudyGoalsDialog() {
        val prefs = _uiState.value.profile?.preferences
        _uiState.update {
            it.copy(
                editStudyGoalsDialogVisible = true,
                editDailyPagesGoal = prefs?.dailyPagesGoal?.toString() ?: "20",
                editDailyMinutesGoal = prefs?.dailyMinutesGoal?.toString() ?: "60",
                editWeeklyStudyDaysGoal = prefs?.weeklyStudyDaysGoal?.toString() ?: "5"
            )
        }
    }

    fun hideEditStudyGoalsDialog() {
        _uiState.update { it.copy(editStudyGoalsDialogVisible = false) }
    }

    fun updateEditStudyGoals(dailyPages: String? = null, dailyMinutes: String? = null, weeklyDays: String? = null) {
        _uiState.update {
            it.copy(
                editDailyPagesGoal = dailyPages ?: it.editDailyPagesGoal,
                editDailyMinutesGoal = dailyMinutes ?: it.editDailyMinutesGoal,
                editWeeklyStudyDaysGoal = weeklyDays ?: it.editWeeklyStudyDaysGoal
            )
        }
    }

    fun saveStudyGoals() {
        val dailyPages = _uiState.value.editDailyPagesGoal.toIntOrNull() ?: return
        val dailyMinutes = _uiState.value.editDailyMinutesGoal.toIntOrNull() ?: return
        val weeklyDays = _uiState.value.editWeeklyStudyDaysGoal.toIntOrNull() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateStudyGoals(dailyPages, dailyMinutes, weeklyDays)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isUpdating = false,
                            editStudyGoalsDialogVisible = false,
                            profile = state.profile?.copy(preferences = result.data!!)
                        )
                    }
                    _events.emit(ProfileEvent.PreferencesUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(ProfileEvent.Error(result.message ?: "Failed to update study goals"))
                }
                else -> {}
            }
        }
    }

    // ==================== Reading Speed Edit ====================

    fun showEditReadingSpeedDialog() {
        val prefs = _uiState.value.profile?.preferences
        _uiState.update {
            it.copy(
                editReadingSpeedDialogVisible = true,
                editPagesPerHour = prefs?.pagesPerHour?.toString() ?: "20"
            )
        }
    }

    fun hideEditReadingSpeedDialog() {
        _uiState.update { it.copy(editReadingSpeedDialogVisible = false) }
    }

    fun updateEditReadingSpeed(pagesPerHour: String) {
        _uiState.update { it.copy(editPagesPerHour = pagesPerHour) }
    }

    fun saveReadingSpeed() {
        val pagesPerHour = _uiState.value.editPagesPerHour.toIntOrNull() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateReadingSpeed(pagesPerHour)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isUpdating = false,
                            editReadingSpeedDialogVisible = false,
                            profile = state.profile?.copy(preferences = result.data!!)
                        )
                    }
                    _events.emit(ProfileEvent.PreferencesUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(ProfileEvent.Error(result.message ?: "Failed to update reading speed"))
                }
                else -> {}
            }
        }
    }

    // ==================== Session Preferences Edit ====================

    fun showEditSessionPrefsDialog() {
        val prefs = _uiState.value.profile?.preferences
        _uiState.update {
            it.copy(
                editSessionPrefsDialogVisible = true,
                editPreferredSessionDuration = prefs?.preferredSessionDurationMinutes?.toString() ?: "45",
                editMinSessionDuration = prefs?.minSessionDurationMinutes?.toString() ?: "15",
                editMaxSessionDuration = prefs?.maxSessionDurationMinutes?.toString() ?: "120"
            )
        }
    }

    fun hideEditSessionPrefsDialog() {
        _uiState.update { it.copy(editSessionPrefsDialogVisible = false) }
    }

    fun updateEditSessionPrefs(preferred: String? = null, min: String? = null, max: String? = null) {
        _uiState.update {
            it.copy(
                editPreferredSessionDuration = preferred ?: it.editPreferredSessionDuration,
                editMinSessionDuration = min ?: it.editMinSessionDuration,
                editMaxSessionDuration = max ?: it.editMaxSessionDuration
            )
        }
    }

    fun saveSessionPrefs() {
        val preferred = _uiState.value.editPreferredSessionDuration.toIntOrNull() ?: return
        val min = _uiState.value.editMinSessionDuration.toIntOrNull() ?: return
        val max = _uiState.value.editMaxSessionDuration.toIntOrNull() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateSessionPreferences(preferred, min, max)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isUpdating = false,
                            editSessionPrefsDialogVisible = false,
                            profile = state.profile?.copy(preferences = result.data!!)
                        )
                    }
                    _events.emit(ProfileEvent.PreferencesUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(ProfileEvent.Error(result.message ?: "Failed to update session preferences"))
                }
                else -> {}
            }
        }
    }

    // ==================== Notification Preferences Edit ====================

    fun showEditNotificationPrefsDialog() {
        val prefs = _uiState.value.profile?.preferences?.notifications
        _uiState.update {
            it.copy(
                editNotificationPrefsDialogVisible = true,
                editEnableSessionReminders = prefs?.enableSessionReminders ?: true,
                editReminderMinutesBefore = prefs?.reminderMinutesBefore?.toString() ?: "15",
                editEnableStreakReminders = prefs?.enableStreakReminders ?: true,
                editEnableWeeklyDigest = prefs?.enableWeeklyDigest ?: true,
                editEnableAchievementNotifications = prefs?.enableAchievementNotifications ?: true
            )
        }
    }

    fun hideEditNotificationPrefsDialog() {
        _uiState.update { it.copy(editNotificationPrefsDialogVisible = false) }
    }

    fun updateEditNotificationPrefs(
        enableSessionReminders: Boolean? = null,
        reminderMinutesBefore: String? = null,
        enableStreakReminders: Boolean? = null,
        enableWeeklyDigest: Boolean? = null,
        enableAchievementNotifications: Boolean? = null
    ) {
        _uiState.update {
            it.copy(
                editEnableSessionReminders = enableSessionReminders ?: it.editEnableSessionReminders,
                editReminderMinutesBefore = reminderMinutesBefore ?: it.editReminderMinutesBefore,
                editEnableStreakReminders = enableStreakReminders ?: it.editEnableStreakReminders,
                editEnableWeeklyDigest = enableWeeklyDigest ?: it.editEnableWeeklyDigest,
                editEnableAchievementNotifications = enableAchievementNotifications ?: it.editEnableAchievementNotifications
            )
        }
    }

    fun saveNotificationPrefs() {
        val reminderMinutes = _uiState.value.editReminderMinutesBefore.toIntOrNull() ?: 15

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateNotificationPreferences(
                enableSessionReminders = _uiState.value.editEnableSessionReminders,
                reminderMinutesBefore = reminderMinutes,
                enableStreakReminders = _uiState.value.editEnableStreakReminders,
                enableWeeklyDigest = _uiState.value.editEnableWeeklyDigest,
                enableAchievementNotifications = _uiState.value.editEnableAchievementNotifications
            )) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isUpdating = false,
                            editNotificationPrefsDialogVisible = false,
                            profile = state.profile?.copy(preferences = result.data!!)
                        )
                    }
                    _events.emit(ProfileEvent.PreferencesUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(ProfileEvent.Error(result.message ?: "Failed to update notification preferences"))
                }
                else -> {}
            }
        }
    }

    // ==================== Privacy Settings Edit ====================

    fun showEditPrivacyDialog() {
        val prefs = _uiState.value.profile?.preferences?.privacy
        _uiState.update {
            it.copy(
                editPrivacyDialogVisible = true,
                editShowProfilePublicly = prefs?.showProfilePublicly ?: false,
                editShowStatsPublicly = prefs?.showStatsPublicly ?: false
            )
        }
    }

    fun hideEditPrivacyDialog() {
        _uiState.update { it.copy(editPrivacyDialogVisible = false) }
    }

    fun updateEditPrivacy(showProfilePublicly: Boolean? = null, showStatsPublicly: Boolean? = null) {
        _uiState.update {
            it.copy(
                editShowProfilePublicly = showProfilePublicly ?: it.editShowProfilePublicly,
                editShowStatsPublicly = showStatsPublicly ?: it.editShowStatsPublicly
            )
        }
    }

    fun savePrivacySettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updatePrivacySettings(
                showProfilePublicly = _uiState.value.editShowProfilePublicly,
                showStatsPublicly = _uiState.value.editShowStatsPublicly
            )) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isUpdating = false,
                            editPrivacyDialogVisible = false,
                            profile = state.profile?.copy(preferences = result.data!!)
                        )
                    }
                    _events.emit(ProfileEvent.PreferencesUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(ProfileEvent.Error(result.message ?: "Failed to update privacy settings"))
                }
                else -> {}
            }
        }
    }
}

