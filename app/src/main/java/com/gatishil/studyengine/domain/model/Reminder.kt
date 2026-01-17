package com.gatishil.studyengine.domain.model

import java.time.LocalDateTime

/**
 * Custom reminder domain model
 */
data class CustomReminder(
    val id: String,
    val title: String,
    val message: String,
    val scheduledFor: LocalDateTime,
    val status: ReminderStatus,
    val createdAt: LocalDateTime,
    val sentAt: LocalDateTime?
)

/**
 * Reminder status
 */
enum class ReminderStatus {
    Pending,
    Processing,
    Sent,
    Failed,
    Cancelled;

    companion object {
        fun fromString(value: String): ReminderStatus {
            return when (value.lowercase()) {
                "pending" -> Pending
                "processing" -> Processing
                "sent" -> Sent
                "failed" -> Failed
                "cancelled" -> Cancelled
                else -> Pending
            }
        }
    }
}

/**
 * List of reminders with count
 */
data class RemindersList(
    val reminders: List<CustomReminder>,
    val totalCount: Int
)

