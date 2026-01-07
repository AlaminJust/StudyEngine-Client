package com.example.studyengine.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Domain model for User Availability (weekly recurring availability)
 */
data class UserAvailability(
    val id: String,
    val userId: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isActive: Boolean
) {
    val durationMinutes: Int
        get() = ((endTime.toSecondOfDay() - startTime.toSecondOfDay()) / 60)
}

/**
 * Domain model for creating availability
 */
data class CreateUserAvailabilityRequest(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)

/**
 * Domain model for Schedule Override (specific date override)
 */
data class ScheduleOverride(
    val id: String,
    val userId: String,
    val overrideDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val isOff: Boolean
) {
    val isCustomHours: Boolean
        get() = !isOff && startTime != null && endTime != null
}

/**
 * Domain model for creating schedule override
 */
data class CreateScheduleOverrideRequest(
    val overrideDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val isOff: Boolean
)

/**
 * Domain model for Schedule Context (exam period, vacation, etc.)
 */
data class ScheduleContext(
    val id: String,
    val userId: String,
    val contextType: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val loadMultiplier: Float
)

/**
 * Domain model for creating schedule context
 */
data class CreateScheduleContextRequest(
    val contextType: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val loadMultiplier: Float = 1.0f
)

