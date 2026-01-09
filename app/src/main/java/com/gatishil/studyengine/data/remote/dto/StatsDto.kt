package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Complete study statistics DTO
 */
@Serializable
data class StatsDto(
    @SerialName("currentStreak")
    val currentStreak: Int,
    @SerialName("currentStreakStartDate")
    val currentStreakStartDate: String?,
    @SerialName("lastStudyDate")
    val lastStudyDate: String?,
    @SerialName("isStreakActive")
    val isStreakActive: Boolean,
    @SerialName("daysUntilStreakExpires")
    val daysUntilStreakExpires: Int?,
    @SerialName("longestStreak")
    val longestStreak: Int,
    @SerialName("longestStreakStartDate")
    val longestStreakStartDate: String?,
    @SerialName("longestStreakEndDate")
    val longestStreakEndDate: String?,
    @SerialName("totalStudyDays")
    val totalStudyDays: Int,
    @SerialName("totalPagesRead")
    val totalPagesRead: Int,
    @SerialName("totalMinutesStudied")
    val totalMinutesStudied: Int,
    @SerialName("totalHoursStudied")
    val totalHoursStudied: Double,
    @SerialName("totalSessionsCompleted")
    val totalSessionsCompleted: Int,
    @SerialName("totalBooksCompleted")
    val totalBooksCompleted: Int,
    @SerialName("weeklyStats")
    val weeklyStats: WeeklyStatsDto?,
    @SerialName("perfectWeeksCount")
    val perfectWeeksCount: Int,
    @SerialName("streakMilestoneReached")
    val streakMilestoneReached: Int?,
    @SerialName("nextStreakMilestone")
    val nextStreakMilestone: Int?,
    @SerialName("daysToNextMilestone")
    val daysToNextMilestone: Int?,
    @SerialName("averagePagesPerSession")
    val averagePagesPerSession: Double,
    @SerialName("averageMinutesPerSession")
    val averageMinutesPerSession: Double,
    @SerialName("averagePagesPerDay")
    val averagePagesPerDay: Double,
    @SerialName("streakMessage")
    val streakMessage: String?,
    @SerialName("recentAchievements")
    val recentAchievements: List<AchievementDto>? = null
)

/**
 * Weekly stats DTO
 */
@Serializable
data class WeeklyStatsDto(
    @SerialName("weekStartDate")
    val weekStartDate: String,
    @SerialName("weekEndDate")
    val weekEndDate: String,
    @SerialName("studyDays")
    val studyDays: Int,
    @SerialName("pagesRead")
    val pagesRead: Int,
    @SerialName("minutesStudied")
    val minutesStudied: Int,
    @SerialName("isPerfectWeek")
    val isPerfectWeek: Boolean,
    @SerialName("dailyBreakdown")
    val dailyBreakdown: List<DailyBreakdownDto>? = null
)

/**
 * Daily breakdown DTO
 */
@Serializable
data class DailyBreakdownDto(
    @SerialName("date")
    val date: String,
    @SerialName("pagesRead")
    val pagesRead: Int,
    @SerialName("minutesStudied")
    val minutesStudied: Int,
    @SerialName("sessionsCompleted")
    val sessionsCompleted: Int
)

/**
 * Quick stats for dashboard DTO
 */
@Serializable
data class QuickStatsDto(
    @SerialName("currentStreak")
    val currentStreak: Int,
    @SerialName("isStreakActive")
    val isStreakActive: Boolean,
    @SerialName("todayPages")
    val todayPages: Int,
    @SerialName("todayMinutes")
    val todayMinutes: Int,
    @SerialName("weeklyPages")
    val weeklyPages: Int,
    @SerialName("weeklyStudyDays")
    val weeklyStudyDays: Int,
    @SerialName("motivationalMessage")
    val motivationalMessage: String?
)

/**
 * Study history response DTO
 */
@Serializable
data class StudyHistoryDto(
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("days")
    val days: List<StudyDayDto>,
    @SerialName("totalStudyDays")
    val totalStudyDays: Int,
    @SerialName("totalPagesRead")
    val totalPagesRead: Int,
    @SerialName("totalMinutesStudied")
    val totalMinutesStudied: Int,
    @SerialName("longestStreakInRange")
    val longestStreakInRange: Int
)

/**
 * Study day DTO
 */
@Serializable
data class StudyDayDto(
    @SerialName("date")
    val date: String,
    @SerialName("sessionsCompleted")
    val sessionsCompleted: Int,
    @SerialName("pagesRead")
    val pagesRead: Int,
    @SerialName("minutesStudied")
    val minutesStudied: Int,
    @SerialName("booksStudied")
    val booksStudied: Int,
    @SerialName("isStreakDay")
    val isStreakDay: Boolean,
    @SerialName("dayOfWeek")
    val dayOfWeek: String
)

/**
 * Achievement DTO
 */
@Serializable
data class AchievementDto(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("icon")
    val icon: String,
    @SerialName("achievedDate")
    val achievedDate: String?,
    @SerialName("isAchieved")
    val isAchieved: Boolean
)

/**
 * Calendar month response DTO
 */
@Serializable
data class CalendarMonthDto(
    @SerialName("year")
    val year: Int,
    @SerialName("month")
    val month: Int,
    @SerialName("days")
    val days: List<StudyDayDto>,
    @SerialName("totalStudyDays")
    val totalStudyDays: Int,
    @SerialName("totalPagesRead")
    val totalPagesRead: Int
)

