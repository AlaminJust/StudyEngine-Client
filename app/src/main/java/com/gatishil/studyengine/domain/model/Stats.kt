package com.gatishil.studyengine.domain.model

import java.time.LocalDate

/**
 * Complete study statistics domain model
 */
data class StudyStats(
    val currentStreak: Int,
    val currentStreakStartDate: LocalDate?,
    val lastStudyDate: LocalDate?,
    val isStreakActive: Boolean,
    val daysUntilStreakExpires: Int?,
    val longestStreak: Int,
    val longestStreakStartDate: LocalDate?,
    val longestStreakEndDate: LocalDate?,
    val totalStudyDays: Int,
    val totalPagesRead: Int,
    val totalMinutesStudied: Int,
    val totalHoursStudied: Double,
    val totalSessionsCompleted: Int,
    val totalBooksCompleted: Int,
    val weeklyStats: WeeklyStats?,
    val perfectWeeksCount: Int,
    val streakMilestoneReached: Int?,
    val nextStreakMilestone: Int?,
    val daysToNextMilestone: Int?,
    val averagePagesPerSession: Double,
    val averageMinutesPerSession: Double,
    val averagePagesPerDay: Double,
    val streakMessage: String?,
    val recentAchievements: List<Achievement>
)

/**
 * Weekly stats domain model
 */
data class WeeklyStats(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val studyDays: Int,
    val pagesRead: Int,
    val minutesStudied: Int,
    val isPerfectWeek: Boolean,
    val dailyBreakdown: List<DailyBreakdown>
)

/**
 * Daily breakdown domain model
 */
data class DailyBreakdown(
    val date: LocalDate,
    val pagesRead: Int,
    val minutesStudied: Int,
    val sessionsCompleted: Int
)

/**
 * Quick stats for dashboard domain model
 */
data class QuickStats(
    val currentStreak: Int,
    val isStreakActive: Boolean,
    val todayPages: Int,
    val todayMinutes: Int,
    val weeklyPages: Int,
    val weeklyStudyDays: Int,
    val motivationalMessage: String?
)

/**
 * Study history domain model
 */
data class StudyHistory(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<StudyDay>,
    val totalStudyDays: Int,
    val totalPagesRead: Int,
    val totalMinutesStudied: Int,
    val longestStreakInRange: Int
)

/**
 * Study day domain model
 */
data class StudyDay(
    val date: LocalDate,
    val sessionsCompleted: Int,
    val pagesRead: Int,
    val minutesStudied: Int,
    val booksStudied: Int,
    val isStreakDay: Boolean,
    val dayOfWeek: String
)

/**
 * Achievement domain model
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val achievedDate: LocalDate?,
    val isAchieved: Boolean
)

/**
 * Calendar month domain model
 */
data class CalendarMonth(
    val year: Int,
    val month: Int,
    val days: List<StudyDay>,
    val totalStudyDays: Int,
    val totalPagesRead: Int
)

