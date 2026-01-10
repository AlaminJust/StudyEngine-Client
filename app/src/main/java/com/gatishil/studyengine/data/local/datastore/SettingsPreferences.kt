package com.gatishil.studyengine.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

/**
 * DataStore preferences for app settings (theme, language, etc.)
 */
@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val LANGUAGE_CODE = stringPreferencesKey("language_code")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val REMINDER_MINUTES_BEFORE = intPreferencesKey("reminder_minutes_before")
        private val STREAK_REMINDERS_ENABLED = booleanPreferencesKey("streak_reminders_enabled")
        private val ACHIEVEMENT_NOTIFICATIONS_ENABLED = booleanPreferencesKey("achievement_notifications_enabled")

        // Theme modes
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        // Language codes
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_BENGALI = "bn"
    }

    // ==================== Theme Operations ====================

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    fun getThemeMode(): Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: THEME_SYSTEM
    }

    // ==================== Language Operations ====================

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE] = languageCode
        }
    }

    fun getLanguage(): Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_CODE] ?: LANGUAGE_ENGLISH
    }

    // ==================== Notification Operations ====================

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    fun isNotificationEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setReminderMinutesBefore(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[REMINDER_MINUTES_BEFORE] = minutes
        }
    }

    fun getReminderMinutesBefore(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[REMINDER_MINUTES_BEFORE] ?: 15
    }

    suspend fun setStreakRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[STREAK_REMINDERS_ENABLED] = enabled
        }
    }

    fun isStreakRemindersEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[STREAK_REMINDERS_ENABLED] ?: true
    }

    suspend fun setAchievementNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ACHIEVEMENT_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    fun isAchievementNotificationsEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ACHIEVEMENT_NOTIFICATIONS_ENABLED] ?: true
    }
}

