package com.gatishil.studyengine.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Domain model for Study Plan
 */
data class StudyPlan(
    val id: String,
    val bookId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: StudyPlanStatus,
    val recurrenceRule: RecurrenceRule?
)

/**
 * Status of a study plan
 */
enum class StudyPlanStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED;

    companion object {
        fun fromString(value: String): StudyPlanStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
        }
    }
}

/**
 * Domain model for Recurrence Rule
 */
data class RecurrenceRule(
    val id: String,
    val studyPlanId: String,
    val type: RecurrenceType,
    val interval: Int,
    val daysOfWeek: List<DayOfWeek>
)

/**
 * Type of recurrence
 */
enum class RecurrenceType {
    DAILY,
    WEEKLY,
    CUSTOM;

    companion object {
        fun fromString(value: String): RecurrenceType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: DAILY
        }
    }
}

/**
 * Domain model for creating a study plan
 */
data class CreateStudyPlanRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val recurrenceRule: CreateRecurrenceRuleRequest? = null
)

/**
 * Domain model for creating a recurrence rule
 */
data class CreateRecurrenceRuleRequest(
    val type: RecurrenceType,
    val interval: Int = 1,
    val daysOfWeek: List<DayOfWeek>? = null
)

