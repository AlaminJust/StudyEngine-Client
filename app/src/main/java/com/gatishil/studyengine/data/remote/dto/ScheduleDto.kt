package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User Availability DTO
 */
@Serializable
data class UserAvailabilityDto(
    @SerialName("id")
    val id: String,
    @SerialName("userId")
    val userId: String,
    @SerialName("dayOfWeek")
    val dayOfWeek: String,  // Backend sends enum as string: "Sunday", "Monday", etc.
    @SerialName("startTime")
    val startTime: String,
    @SerialName("endTime")
    val endTime: String,
    @SerialName("isActive")
    val isActive: Boolean
) {
    /**
     * Convert dayOfWeek string to integer (Sunday=0, Monday=1, etc.)
     */
    fun getDayOfWeekInt(): Int {
        return when (dayOfWeek.lowercase()) {
            "sunday" -> 0
            "monday" -> 1
            "tuesday" -> 2
            "wednesday" -> 3
            "thursday" -> 4
            "friday" -> 5
            "saturday" -> 6
            else -> try {
                dayOfWeek.toInt()
            } catch (e: Exception) {
                0
            }
        }
    }
}

/**
 * Request DTO for creating availability
 */
@Serializable
data class CreateUserAvailabilityRequestDto(
    @SerialName("DayOfWeek")
    val dayOfWeek: Int,
    @SerialName("StartTime")
    val startTime: String,
    @SerialName("EndTime")
    val endTime: String
)

/**
 * Request DTO for bulk updating all user availabilities.
 * This replaces all existing availabilities with the provided list.
 */
@Serializable
data class BulkUpdateUserAvailabilityRequestDto(
    @SerialName("Availabilities")
    val availabilities: List<CreateUserAvailabilityRequestDto>
)

/**
 * Schedule Override DTO
 */
@Serializable
data class ScheduleOverrideDto(
    @SerialName("id")
    val id: String,
    @SerialName("userId")
    val userId: String,
    @SerialName("overrideDate")
    val overrideDate: String,
    @SerialName("startTime")
    val startTime: String?,
    @SerialName("endTime")
    val endTime: String?,
    @SerialName("isOff")
    val isOff: Boolean
)

/**
 * Request DTO for creating schedule override
 */
@Serializable
data class CreateScheduleOverrideRequestDto(
    @SerialName("OverrideDate")
    val overrideDate: String,
    @SerialName("StartTime")
    val startTime: String?,
    @SerialName("EndTime")
    val endTime: String?,
    @SerialName("IsOff")
    val isOff: Boolean
)

/**
 * Schedule Context DTO
 */
@Serializable
data class ScheduleContextDto(
    @SerialName("id")
    val id: String,
    @SerialName("userId")
    val userId: String,
    @SerialName("contextType")
    val contextType: String,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("loadMultiplier")
    val loadMultiplier: Float
)

/**
 * Request DTO for creating schedule context
 */
@Serializable
data class CreateScheduleContextRequestDto(
    @SerialName("ContextType")
    val contextType: String,
    @SerialName("StartDate")
    val startDate: String,
    @SerialName("EndDate")
    val endDate: String,
    @SerialName("LoadMultiplier")
    val loadMultiplier: Float = 1.0f
)

/**
 * Request DTO for updating load multiplier
 */
@Serializable
data class UpdateLoadMultiplierRequestDto(
    @SerialName("LoadMultiplier")
    val loadMultiplier: Float
)

