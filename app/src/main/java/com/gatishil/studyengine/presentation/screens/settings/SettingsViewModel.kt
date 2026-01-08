package com.gatishil.studyengine.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.data.local.datastore.SettingsPreferences
import com.gatishil.studyengine.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = SettingsPreferences.THEME_SYSTEM,
    val language: String = SettingsPreferences.LANGUAGE_ENGLISH,
    val notificationsEnabled: Boolean = true,
    val reminderMinutes: Int = 15,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _signOutEvent = MutableSharedFlow<Unit>()
    val signOutEvent = _signOutEvent.asSharedFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsPreferences.getThemeMode(),
                settingsPreferences.getLanguage(),
                settingsPreferences.isNotificationEnabled(),
                settingsPreferences.getReminderMinutesBefore()
            ) { theme, language, notifications, reminder ->
                SettingsUiState(
                    themeMode = theme,
                    language = language,
                    notificationsEnabled = notifications,
                    reminderMinutes = reminder
                )
            }.collect { state ->
                _uiState.update { state }
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
        viewModelScope.launch {
            settingsPreferences.setNotificationEnabled(enabled)
        }
    }

    fun setReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsPreferences.setReminderMinutesBefore(minutes)
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

