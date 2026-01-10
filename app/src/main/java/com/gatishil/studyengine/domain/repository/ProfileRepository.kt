package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for profile operations
 */
interface ProfileRepository {

    /**
     * Get current user's complete profile
     */
    fun getProfile(): Flow<Resource<UserProfile>>

    /**
     * Update basic profile information
     */
    suspend fun updateProfile(name: String, timeZone: String): Resource<UserProfile>

    /**
     * Get user preferences
     */
    fun getPreferences(): Flow<Resource<UserPreferences>>

    /**
     * Update study goals
     */
    suspend fun updateStudyGoals(
        dailyPagesGoal: Int,
        dailyMinutesGoal: Int,
        weeklyStudyDaysGoal: Int
    ): Resource<UserPreferences>

    /**
     * Update reading speed
     */
    suspend fun updateReadingSpeed(pagesPerHour: Int): Resource<UserPreferences>

    /**
     * Update session preferences
     */
    suspend fun updateSessionPreferences(
        preferredDuration: Int,
        minDuration: Int,
        maxDuration: Int
    ): Resource<UserPreferences>

    /**
     * Update notification preferences
     */
    suspend fun updateNotificationPreferences(
        enableSessionReminders: Boolean,
        reminderMinutesBefore: Int,
        enableStreakReminders: Boolean,
        enableWeeklyDigest: Boolean,
        enableAchievementNotifications: Boolean
    ): Resource<UserPreferences>

    /**
     * Update UI preferences
     */
    suspend fun updateUIPreferences(
        theme: String,
        language: String,
        showMotivationalQuotes: Boolean
    ): Resource<UserPreferences>

    /**
     * Update privacy settings
     */
    suspend fun updatePrivacySettings(
        showProfilePublicly: Boolean,
        showStatsPublicly: Boolean
    ): Resource<UserPreferences>

    /**
     * Deactivate account
     */
    suspend fun deactivateAccount(): Resource<Boolean>

    /**
     * Reactivate account
     */
    suspend fun reactivateAccount(): Resource<Boolean>

    /**
     * Delete account permanently
     */
    suspend fun deleteAccount(confirmationPhrase: String): Resource<Boolean>
}

