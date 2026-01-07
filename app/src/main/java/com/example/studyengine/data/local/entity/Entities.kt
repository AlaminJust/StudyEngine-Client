package com.example.studyengine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for User
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val timeZone: String,
    val profilePictureUrl: String?,
    val createdAt: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for Book
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val subject: String,
    val totalPages: Int,
    val effectiveTotalPages: Int,
    val difficulty: Int,
    val priority: Int,
    val targetEndDate: String?,
    val createdAt: String,
    val ignoredChapterCount: Int,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: Int = SyncStatus.SYNCED
)

/**
 * Room entity for Chapter
 */
@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val title: String,
    val startPage: Int,
    val endPage: Int,
    val orderIndex: Int,
    val pageCount: Int,
    val isIgnored: Boolean,
    val ignoreReason: String?,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: Int = SyncStatus.SYNCED
)

/**
 * Room entity for Study Plan
 */
@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: Int = SyncStatus.SYNCED
)

/**
 * Room entity for Recurrence Rule
 */
@Entity(tableName = "recurrence_rules")
data class RecurrenceRuleEntity(
    @PrimaryKey
    val id: String,
    val studyPlanId: String,
    val type: String,
    val interval: Int,
    val daysOfWeek: String // Comma-separated day indices
)

/**
 * Room entity for Study Session
 */
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val bookId: String,
    val chapterId: String?,
    val sessionDate: String,
    val startTime: String,
    val endTime: String,
    val plannedPages: Int,
    val completedPages: Int,
    val status: String,
    val bookTitle: String?,
    val chapterTitle: String?,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: Int = SyncStatus.SYNCED
)

/**
 * Room entity for User Availability
 */
@Entity(tableName = "user_availabilities")
data class UserAvailabilityEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val isActive: Boolean,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: Int = SyncStatus.SYNCED
)

/**
 * Room entity for Schedule Override
 */
@Entity(tableName = "schedule_overrides")
data class ScheduleOverrideEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val overrideDate: String,
    val startTime: String?,
    val endTime: String?,
    val isOff: Boolean,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: Int = SyncStatus.SYNCED
)

/**
 * Room entity for Schedule Context
 */
@Entity(tableName = "schedule_contexts")
data class ScheduleContextEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val contextType: String,
    val startDate: String,
    val endDate: String,
    val loadMultiplier: Float,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: Int = SyncStatus.SYNCED
)

/**
 * Sync status constants
 */
object SyncStatus {
    const val SYNCED = 0
    const val PENDING_CREATE = 1
    const val PENDING_UPDATE = 2
    const val PENDING_DELETE = 3
}

