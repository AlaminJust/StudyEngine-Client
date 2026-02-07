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
