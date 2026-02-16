package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveExamPublicResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("questionCount")
    val questionCount: Int,
    @SerialName("timeLimitMinutes")
    val timeLimitMinutes: Int? = null,
    @SerialName("difficultyFilter")
    val difficultyFilter: String? = null,
    @SerialName("scheduledStartTime")
    val scheduledStartTime: String,
    @SerialName("scheduledEndTime")
    val scheduledEndTime: String,
    @SerialName("status")
    val status: String,
    @SerialName("hasAttempted")
    val hasAttempted: Boolean
)

@Serializable
data class LiveExamCompletedResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("questionCount")
    val questionCount: Int,
    @SerialName("timeLimitMinutes")
    val timeLimitMinutes: Int? = null,
    @SerialName("difficultyFilter")
    val difficultyFilter: String? = null,
    @SerialName("participantCount")
    val participantCount: Int,
    @SerialName("scheduledStartTime")
    val scheduledStartTime: String,
    @SerialName("scheduledEndTime")
    val scheduledEndTime: String
)

@Serializable
data class LiveExamStandingsResponseDto(
    @SerialName("title")
    val title: String,
    @SerialName("totalParticipants")
    val totalParticipants: Int,
    @SerialName("standings")
    val standings: List<LiveExamStandingEntryDto>
)

@Serializable
data class LiveExamStandingEntryDto(
    @SerialName("rank")
    val rank: Int,
    @SerialName("userName")
    val userName: String,
    @SerialName("earnedPoints")
    val earnedPoints: Int,
    @SerialName("totalPoints")
    val totalPoints: Int,
    @SerialName("scorePercentage")
    val scorePercentage: Double,
    @SerialName("duration")
    val duration: String? = null
)
