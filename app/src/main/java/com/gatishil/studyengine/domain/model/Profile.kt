package com.gatishil.studyengine.domain.model

import java.time.LocalDateTime

/**
 * Complete user profile domain model
 */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val timeZone: String,
    val profilePictureUrl: String?,
    val authProvider: String,
    val isActive: Boolean,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val daysSinceJoined: Int,
    val studySummary: ProfileStudySummary,
    val preferences: UserPreferences,
    val librarySummary: LibrarySummary
)

/**
 * Study summary for profile
 */
data class ProfileStudySummary(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalStudyDays: Int,
    val totalPagesRead: Int,
    val totalHoursStudied: Int,
    val totalBooksCompleted: Int,
    val totalSessionsCompleted: Int,
    val achievementsUnlocked: Int,
    val perfectWeeksCount: Int
)

/**
 * Library summary
 */
data class LibrarySummary(
    val totalBooks: Int,
    val activeBooks: Int,
    val completedBooks: Int,
    val totalChapters: Int,
    val totalPages: Int
)

/**
 * User preferences
 */
data class UserPreferences(
    val dailyPagesGoal: Int,
    val dailyMinutesGoal: Int,
    val weeklyStudyDaysGoal: Int,
    val pagesPerHour: Int,
    val preferredSessionDurationMinutes: Int,
    val minSessionDurationMinutes: Int,
    val maxSessionDurationMinutes: Int,
    val notifications: NotificationPreferences,
    val ui: UIPreferences,
    val privacy: PrivacySettings
)

/**
 * Notification preferences
 */
data class NotificationPreferences(
    val enableSessionReminders: Boolean,
    val reminderMinutesBefore: Int,
    val enableStreakReminders: Boolean,
    val enableWeeklyDigest: Boolean,
    val enableAchievementNotifications: Boolean
)

/**
 * UI preferences
 */
data class UIPreferences(
    val theme: String,
    val language: String,
    val showMotivationalQuotes: Boolean
)

/**
 * Privacy settings
 */
data class PrivacySettings(
    val showProfilePublicly: Boolean,
    val showStatsPublicly: Boolean
)

/**
 * Academic profile domain model
 */
data class UserAcademicProfile(
    val role: String,
    val roleDescription: String?,
    val academicLevel: String?,
    val currentClass: String?,
    val major: String?,
    val department: String?,
    val studentType: String?,
    val studentId: String?,
    val academicYear: Int?,
    val currentSemester: String?,
    val enrollmentDate: String?,
    val expectedGraduationDate: String?,
    val institution: InstitutionInfo?,
    val teachingSubjects: String?,
    val qualifications: String?,
    val yearsOfExperience: Int?,
    val bio: String?,
    val researchInterests: String?,
    val socialLinks: SocialLinks?,
    val isVerified: Boolean,
    val verifiedAt: String?
)

/**
 * Institution information
 */
data class InstitutionInfo(
    val name: String,
    val type: String,
    val country: String,
    val city: String?,
    val state: String?
)

/**
 * Social and professional links
 */
data class SocialLinks(
    val website: String?,
    val linkedIn: String?,
    val gitHub: String?
)

