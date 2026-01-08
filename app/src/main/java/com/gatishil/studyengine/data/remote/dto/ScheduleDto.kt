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
    val dayOfWeek: Int,
    @SerialName("startTime")
    val startTime: String,
    @SerialName("endTime")
    val endTime: String,
    @SerialName("isActive")
    val isActive: Boolean
)

/**
 * Request DTO for creating availability
 */
@Serializable
data class CreateUserAvailabilityRequestDto(
    @SerialName("dayOfWeek")
    val dayOfWeek: Int,
    @SerialName("startTime")
    val startTime: String,
    @SerialName("endTime")
    val endTime: String
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
    @SerialName("contextType")
    val contextType: String,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("loadMultiplier")
    val loadMultiplier: Float = 1.0f
)

