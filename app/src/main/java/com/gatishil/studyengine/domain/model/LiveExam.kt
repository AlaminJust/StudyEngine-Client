package com.gatishil.studyengine.domain.model

import java.time.LocalDateTime

/**
 * Live exam status - matches backend LiveExamStatus enum
 */
enum class LiveExamStatus {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    companion object {
        fun fromString(value: String): LiveExamStatus {
            return when (value.uppercase()) {
                "SCHEDULED" -> SCHEDULED
                "ACTIVE" -> ACTIVE
                "COMPLETED" -> COMPLETED
                "CANCELLED" -> CANCELLED
                else -> SCHEDULED
            }
        }
    }
}

/**
 * Public live exam data visible to all users
 */
data class LiveExam(
    val id: String,
    val title: String,
    val description: String?,
    val questionCount: Int,
    val timeLimitMinutes: Int?,
    val difficultyFilter: QuestionDifficulty?,
    val scheduledStartTime: LocalDateTime,
    val scheduledEndTime: LocalDateTime,
    val status: LiveExamStatus,
    val hasAttempted: Boolean
)
