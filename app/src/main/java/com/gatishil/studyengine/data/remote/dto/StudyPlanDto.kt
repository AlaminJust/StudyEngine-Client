package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Study Plan DTO
 */
@Serializable
data class StudyPlanDto(
    @SerialName("id")
    val id: String,
    @SerialName("bookId")
    val bookId: String,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("status")
    val status: String,
    @SerialName("recurrenceRule")
    val recurrenceRule: RecurrenceRuleDto?,
    @SerialName("progress")
    val progress: StudyPlanProgressDto? = null
)

/**
 * Progress information for a study plan within its active period.
 */
@Serializable
data class StudyPlanProgressDto(
    @SerialName("totalPages")
    val totalPages: Int,
    @SerialName("completedPages")
    val completedPages: Int,
    @SerialName("remainingPages")
    val remainingPages: Int,
    @SerialName("progressPercentage")
    val progressPercentage: Double,
    @SerialName("totalSessions")
    val totalSessions: Int,
    @SerialName("completedSessions")
    val completedSessions: Int,
    @SerialName("missedSessions")
    val missedSessions: Int,
    @SerialName("plannedSessions")
    val plannedSessions: Int,
    @SerialName("estimatedCompletionDate")
    val estimatedCompletionDate: String? = null
)

/**
 * Request DTO for creating a study plan
 */
@Serializable
data class CreateStudyPlanRequestDto(
    @SerialName("StartDate")
    val startDate: String,
    @SerialName("EndDate")
    val endDate: String,
    @SerialName("RecurrenceRule")
    val recurrenceRule: CreateRecurrenceRuleRequestDto? = null
)

/**
 * Request DTO for updating a study plan
 */
@Serializable
data class UpdateStudyPlanRequestDto(
    @SerialName("StartDate")
    val startDate: String,
    @SerialName("EndDate")
    val endDate: String,
    @SerialName("RecurrenceRule")
    val recurrenceRule: CreateRecurrenceRuleRequestDto? = null
)

/**
 * Recurrence Rule DTO
 */
@Serializable
data class RecurrenceRuleDto(
    @SerialName("id")
    val id: String,
    @SerialName("studyPlanId")
    val studyPlanId: String,
    @SerialName("type")
    val type: String,
    @SerialName("interval")
    val interval: Int,
    @SerialName("daysOfWeek")
    val daysOfWeek: List<String> // Backend returns enum names like "Monday", "Tuesday"
)

/**
 * Request DTO for creating a recurrence rule
 */
@Serializable
data class CreateRecurrenceRuleRequestDto(
    @SerialName("Type")
    val type: String,
    @SerialName("Interval")
    val interval: Int = 1,
    @SerialName("DaysOfWeek")
    val daysOfWeek: List<Int>? = null // We send as integers to backend
)

