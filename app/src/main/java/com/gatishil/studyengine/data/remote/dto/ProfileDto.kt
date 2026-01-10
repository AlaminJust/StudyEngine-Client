package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Complete user profile with all related information.
 */
@Serializable
data class UserProfileDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("timeZone") val timeZone: String,
    @SerialName("profilePictureUrl") val profilePictureUrl: String?,
    @SerialName("authProvider") val authProvider: String,
    @SerialName("isActive") val isActive: Boolean,
    @SerialName("lastLoginAt") val lastLoginAt: String?,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("daysSinceJoined") val daysSinceJoined: Int,
    @SerialName("studySummary") val studySummary: ProfileStudySummaryDto,
    @SerialName("preferences") val preferences: UserPreferencesDto,
    @SerialName("librarySummary") val librarySummary: LibrarySummaryDto
)

/**
 * Study summary for profile display.
 */
@Serializable
data class ProfileStudySummaryDto(
    @SerialName("currentStreak") val currentStreak: Int,
    @SerialName("longestStreak") val longestStreak: Int,
    @SerialName("totalStudyDays") val totalStudyDays: Int,
    @SerialName("totalPagesRead") val totalPagesRead: Int,
    @SerialName("totalHoursStudied") val totalHoursStudied: Int,
    @SerialName("totalBooksCompleted") val totalBooksCompleted: Int,
    @SerialName("totalSessionsCompleted") val totalSessionsCompleted: Int,
    @SerialName("achievementsUnlocked") val achievementsUnlocked: Int,
    @SerialName("perfectWeeksCount") val perfectWeeksCount: Int
)

/**
 * Library summary for profile.
 */
@Serializable
data class LibrarySummaryDto(
    @SerialName("totalBooks") val totalBooks: Int,
    @SerialName("activeBooks") val activeBooks: Int,
    @SerialName("completedBooks") val completedBooks: Int,
    @SerialName("totalChapters") val totalChapters: Int,
    @SerialName("totalPages") val totalPages: Int
)

/**
 * User preferences DTO.
 */
@Serializable
data class UserPreferencesDto(
    @SerialName("dailyPagesGoal") val dailyPagesGoal: Int,
    @SerialName("dailyMinutesGoal") val dailyMinutesGoal: Int,
    @SerialName("weeklyStudyDaysGoal") val weeklyStudyDaysGoal: Int,
    @SerialName("pagesPerHour") val pagesPerHour: Int,
    @SerialName("preferredSessionDurationMinutes") val preferredSessionDurationMinutes: Int,
    @SerialName("minSessionDurationMinutes") val minSessionDurationMinutes: Int,
    @SerialName("maxSessionDurationMinutes") val maxSessionDurationMinutes: Int,
    @SerialName("notifications") val notifications: NotificationPreferencesDto,
    @SerialName("ui") val ui: UIPreferencesDto,
    @SerialName("privacy") val privacy: PrivacySettingsDto
)

/**
 * Notification preferences.
 */
@Serializable
data class NotificationPreferencesDto(
    @SerialName("enableSessionReminders") val enableSessionReminders: Boolean,
    @SerialName("reminderMinutesBefore") val reminderMinutesBefore: Int,
    @SerialName("enableStreakReminders") val enableStreakReminders: Boolean,
    @SerialName("enableWeeklyDigest") val enableWeeklyDigest: Boolean,
    @SerialName("enableAchievementNotifications") val enableAchievementNotifications: Boolean
)

/**
 * UI preferences.
 */
@Serializable
data class UIPreferencesDto(
    @SerialName("theme") val theme: String,
    @SerialName("language") val language: String,
    @SerialName("showMotivationalQuotes") val showMotivationalQuotes: Boolean
)

/**
 * Privacy settings.
 */
@Serializable
data class PrivacySettingsDto(
    @SerialName("showProfilePublicly") val showProfilePublicly: Boolean,
    @SerialName("showStatsPublicly") val showStatsPublicly: Boolean
)

// ==================== Request DTOs ====================

/**
 * Update basic profile information.
 */
@Serializable
data class UpdateProfileRequestDto(
    @SerialName("name") val name: String,
    @SerialName("timeZone") val timeZone: String
)

/**
 * Update study goals.
 */
@Serializable
data class UpdateStudyGoalsRequestDto(
    @SerialName("dailyPagesGoal") val dailyPagesGoal: Int,
    @SerialName("dailyMinutesGoal") val dailyMinutesGoal: Int,
    @SerialName("weeklyStudyDaysGoal") val weeklyStudyDaysGoal: Int
)

/**
 * Update reading speed preference.
 */
@Serializable
data class UpdateReadingSpeedRequestDto(
    @SerialName("pagesPerHour") val pagesPerHour: Int
)

/**
 * Update session preferences.
 */
@Serializable
data class UpdateSessionPreferencesRequestDto(
    @SerialName("preferredSessionDurationMinutes") val preferredSessionDurationMinutes: Int,
    @SerialName("minSessionDurationMinutes") val minSessionDurationMinutes: Int,
    @SerialName("maxSessionDurationMinutes") val maxSessionDurationMinutes: Int
)

/**
 * Update notification preferences.
 */
@Serializable
data class UpdateNotificationPreferencesRequestDto(
    @SerialName("enableSessionReminders") val enableSessionReminders: Boolean,
    @SerialName("reminderMinutesBefore") val reminderMinutesBefore: Int,
    @SerialName("enableStreakReminders") val enableStreakReminders: Boolean,
    @SerialName("enableWeeklyDigest") val enableWeeklyDigest: Boolean,
    @SerialName("enableAchievementNotifications") val enableAchievementNotifications: Boolean
)

/**
 * Update UI preferences.
 */
@Serializable
data class UpdateUIPreferencesRequestDto(
    @SerialName("theme") val theme: String,
    @SerialName("language") val language: String,
    @SerialName("showMotivationalQuotes") val showMotivationalQuotes: Boolean
)

/**
 * Update privacy settings.
 */
@Serializable
data class UpdatePrivacySettingsRequestDto(
    @SerialName("showProfilePublicly") val showProfilePublicly: Boolean,
    @SerialName("showStatsPublicly") val showStatsPublicly: Boolean
)

/**
 * Account deletion request with confirmation.
 */
@Serializable
data class DeleteAccountRequestDto(
    @SerialName("confirmationPhrase") val confirmationPhrase: String
)

/**
 * Public profile view for sharing.
 */
@Serializable
data class PublicProfileDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("profilePictureUrl") val profilePictureUrl: String?,
    @SerialName("daysSinceJoined") val daysSinceJoined: Int,
    @SerialName("studySummary") val studySummary: PublicStudySummaryDto?
)

/**
 * Public study summary.
 */
@Serializable
data class PublicStudySummaryDto(
    @SerialName("currentStreak") val currentStreak: Int,
    @SerialName("longestStreak") val longestStreak: Int,
    @SerialName("totalStudyDays") val totalStudyDays: Int,
    @SerialName("totalBooksCompleted") val totalBooksCompleted: Int,
    @SerialName("achievementsUnlocked") val achievementsUnlocked: Int
)

