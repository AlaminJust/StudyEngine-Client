package com.gatishil.studyengine.data.mapper

import com.gatishil.studyengine.data.remote.dto.*
import com.gatishil.studyengine.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Mapper for profile-related DTOs to domain models
 */
object ProfileMapper {

    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    fun toDomain(dto: UserProfileDto): UserProfile {
        return UserProfile(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            timeZone = dto.timeZone,
            profilePictureUrl = dto.profilePictureUrl,
            authProvider = dto.authProvider,
            isActive = dto.isActive,
            lastLoginAt = dto.lastLoginAt?.let { parseDateTime(it) },
            createdAt = parseDateTime(dto.createdAt),
            daysSinceJoined = dto.daysSinceJoined,
            studySummary = toStudySummaryDomain(dto.studySummary),
            preferences = toPreferencesDomain(dto.preferences),
            librarySummary = toLibrarySummaryDomain(dto.librarySummary)
        )
    }

    fun toStudySummaryDomain(dto: ProfileStudySummaryDto): ProfileStudySummary {
        return ProfileStudySummary(
            currentStreak = dto.currentStreak,
            longestStreak = dto.longestStreak,
            totalStudyDays = dto.totalStudyDays,
            totalPagesRead = dto.totalPagesRead,
            totalHoursStudied = dto.totalHoursStudied,
            totalBooksCompleted = dto.totalBooksCompleted,
            totalSessionsCompleted = dto.totalSessionsCompleted,
            achievementsUnlocked = dto.achievementsUnlocked,
            perfectWeeksCount = dto.perfectWeeksCount
        )
    }

    fun toLibrarySummaryDomain(dto: LibrarySummaryDto): LibrarySummary {
        return LibrarySummary(
            totalBooks = dto.totalBooks,
            activeBooks = dto.activeBooks,
            completedBooks = dto.completedBooks,
            totalChapters = dto.totalChapters,
            totalPages = dto.totalPages
        )
    }

    fun toPreferencesDomain(dto: UserPreferencesDto): UserPreferences {
        return UserPreferences(
            dailyPagesGoal = dto.dailyPagesGoal,
            dailyMinutesGoal = dto.dailyMinutesGoal,
            weeklyStudyDaysGoal = dto.weeklyStudyDaysGoal,
            pagesPerHour = dto.pagesPerHour,
            preferredSessionDurationMinutes = dto.preferredSessionDurationMinutes,
            minSessionDurationMinutes = dto.minSessionDurationMinutes,
            maxSessionDurationMinutes = dto.maxSessionDurationMinutes,
            notifications = toNotificationPreferencesDomain(dto.notifications),
            ui = toUIPreferencesDomain(dto.ui),
            privacy = toPrivacySettingsDomain(dto.privacy)
        )
    }

    fun toNotificationPreferencesDomain(dto: NotificationPreferencesDto): NotificationPreferences {
        return NotificationPreferences(
            enableSessionReminders = dto.enableSessionReminders,
            reminderMinutesBefore = dto.reminderMinutesBefore,
            enableStreakReminders = dto.enableStreakReminders,
            enableWeeklyDigest = dto.enableWeeklyDigest,
            enableAchievementNotifications = dto.enableAchievementNotifications
        )
    }

    fun toUIPreferencesDomain(dto: UIPreferencesDto): UIPreferences {
        return UIPreferences(
            theme = dto.theme,
            language = dto.language,
            showMotivationalQuotes = dto.showMotivationalQuotes
        )
    }

    fun toPrivacySettingsDomain(dto: PrivacySettingsDto): PrivacySettings {
        return PrivacySettings(
            showProfilePublicly = dto.showProfilePublicly,
            showStatsPublicly = dto.showStatsPublicly
        )
    }

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, dateTimeFormatter)
        } catch (e: Exception) {
            // Try parsing with different formats
            try {
                LocalDateTime.parse(dateString.replace("Z", ""))
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
    }
}

