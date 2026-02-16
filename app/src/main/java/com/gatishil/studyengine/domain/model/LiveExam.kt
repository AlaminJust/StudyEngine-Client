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

data class CompletedLiveExam(
    val id: String,
    val title: String,
    val description: String?,
    val questionCount: Int,
    val timeLimitMinutes: Int?,
    val difficultyFilter: QuestionDifficulty?,
    val participantCount: Int,
    val scheduledStartTime: LocalDateTime,
    val scheduledEndTime: LocalDateTime
)

data class LiveExamStandings(
    val title: String,
    val totalParticipants: Int,
    val standings: List<LiveExamStandingEntry>
)

data class LiveExamStandingEntry(
    val rank: Int,
    val userName: String,
    val earnedPoints: Int,
    val totalPoints: Int,
    val scorePercentage: Double,
    val duration: String?
)
