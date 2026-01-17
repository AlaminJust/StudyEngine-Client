package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to create a custom reminder
 */
@Serializable
data class CreateCustomReminderRequestDto(
    @SerialName("title") val title: String,
    @SerialName("message") val message: String,
    @SerialName("scheduledFor") val scheduledFor: String
)

/**
 * Request to update a custom reminder
 */
@Serializable
data class UpdateCustomReminderRequestDto(
    @SerialName("title") val title: String?,
    @SerialName("message") val message: String?,
    @SerialName("scheduledFor") val scheduledFor: String?
)

/**
 * Response for a custom reminder
 */
@Serializable
data class CustomReminderResponseDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("message") val message: String,
    @SerialName("scheduledFor") val scheduledFor: String,
    @SerialName("status") val status: String,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("sentAt") val sentAt: String?
)

/**
 * Response for upcoming reminders list
 */
@Serializable
data class UpcomingRemindersResponseDto(
    @SerialName("reminders") val reminders: List<CustomReminderResponseDto>,
    @SerialName("totalCount") val totalCount: Int
)

