package com.example.studyengine.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Domain model for Study Session
 */
data class StudySession(
    val id: String,
    val userId: String,
    val bookId: String,
    val chapterId: String?,
    val sessionDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val plannedPages: Int,
    val completedPages: Int,
    val status: StudySessionStatus,
    val bookTitle: String?,
    val chapterTitle: String?
) {
    val durationMinutes: Int
        get() = ((endTime.toSecondOfDay() - startTime.toSecondOfDay()) / 60)

    val isCompleted: Boolean
        get() = status == StudySessionStatus.COMPLETED

    val progressPercentage: Float
        get() = if (plannedPages > 0) {
            (completedPages.toFloat() / plannedPages) * 100
        } else 0f
}

/**
 * Status of a study session
 */
enum class StudySessionStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    MISSED,
    CANCELLED;

    companion object {
        fun fromString(value: String): StudySessionStatus {
            return when (value.uppercase().replace(" ", "_")) {
                "PLANNED" -> PLANNED
                "INPROGRESS", "IN_PROGRESS" -> IN_PROGRESS
                "COMPLETED" -> COMPLETED
                "MISSED" -> MISSED
                "CANCELLED" -> CANCELLED
                else -> PLANNED
            }
        }
    }
}

/**
 * Domain model for completing a session
 */
data class CompleteSessionRequest(
    val completedPages: Int,
    val notes: String? = null
)

/**
 * Domain model for session log
 */
data class SessionLog(
    val id: String,
    val studySessionId: String,
    val actualStartTime: java.time.LocalDateTime,
    val actualEndTime: java.time.LocalDateTime?,
    val notes: String
)

