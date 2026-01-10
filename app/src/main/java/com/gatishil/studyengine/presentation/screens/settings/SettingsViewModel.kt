package com.gatishil.studyengine.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.local.datastore.SettingsPreferences
import com.gatishil.studyengine.domain.repository.AuthRepository
import com.gatishil.studyengine.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = SettingsPreferences.THEME_SYSTEM,
    val language: String = SettingsPreferences.LANGUAGE_ENGLISH,

    // Notification settings from API
    val notificationsEnabled: Boolean = true,
    val reminderMinutes: Int = 15,
    val streakRemindersEnabled: Boolean = true,
    val weeklyDigestEnabled: Boolean = true,
    val achievementNotificationsEnabled: Boolean = true,

    val isLoading: Boolean = false,
    val isSyncing: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _signOutEvent = MutableSharedFlow<Unit>()
    val signOutEvent = _signOutEvent.asSharedFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    init {
        loadSettings()
        loadNotificationPreferencesFromApi()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsPreferences.getThemeMode(),
                settingsPreferences.getLanguage()
            ) { theme, language ->
                Pair(theme, language)
            }.collect { (theme, language) ->
                _uiState.update {
                    it.copy(themeMode = theme, language = language)
                }
            }
        }
    }

    private fun loadNotificationPreferencesFromApi() {
        viewModelScope.launch {
            profileRepository.getPreferences().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { prefs ->
                            _uiState.update {
                                it.copy(
                                    notificationsEnabled = prefs.notifications.enableSessionReminders,
                                    reminderMinutes = prefs.notifications.reminderMinutesBefore,
                                    streakRemindersEnabled = prefs.notifications.enableStreakReminders,
                                    weeklyDigestEnabled = prefs.notifications.enableWeeklyDigest,
                                    achievementNotificationsEnabled = prefs.notifications.enableAchievementNotifications
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        // Use local defaults on error
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsPreferences.setThemeMode(mode)
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            settingsPreferences.setLanguage(languageCode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled, isSyncing = true) }
        syncNotificationPreferences()
    }

    fun setReminderMinutes(minutes: Int) {
        _uiState.update { it.copy(reminderMinutes = minutes, isSyncing = true) }
        syncNotificationPreferences()
    }

    fun setStreakRemindersEnabled(enabled: Boolean) {
        _uiState.update { it.copy(streakRemindersEnabled = enabled, isSyncing = true) }
        syncNotificationPreferences()
    }

    fun setWeeklyDigestEnabled(enabled: Boolean) {
        _uiState.update { it.copy(weeklyDigestEnabled = enabled, isSyncing = true) }
        syncNotificationPreferences()
    }

    fun setAchievementNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(achievementNotificationsEnabled = enabled, isSyncing = true) }
        syncNotificationPreferences()
    }

    private fun syncNotificationPreferences() {
        viewModelScope.launch {
            val state = _uiState.value
            when (val result = profileRepository.updateNotificationPreferences(
                enableSessionReminders = state.notificationsEnabled,
                reminderMinutesBefore = state.reminderMinutes,
                enableStreakReminders = state.streakRemindersEnabled,
                enableWeeklyDigest = state.weeklyDigestEnabled,
                enableAchievementNotifications = state.achievementNotificationsEnabled
            )) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSyncing = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSyncing = false) }
                    _toastEvent.emit("Failed to sync: ${result.message}")
                }
                else -> {}
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.logout()
            _signOutEvent.emit(Unit)
        }
    }
}

