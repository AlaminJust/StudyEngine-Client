package com.gatishil.studyengine.data.mapper

import com.gatishil.studyengine.data.remote.dto.CustomReminderResponseDto
import com.gatishil.studyengine.data.remote.dto.UpcomingRemindersResponseDto
import com.gatishil.studyengine.domain.model.CustomReminder
import com.gatishil.studyengine.domain.model.ReminderStatus
import com.gatishil.studyengine.domain.model.RemindersList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Mapper for reminder-related DTOs to domain models
 */
object ReminderMapper {

    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    fun toDomain(dto: CustomReminderResponseDto): CustomReminder {
        return CustomReminder(
            id = dto.id,
            title = dto.title,
            message = dto.message,
            scheduledFor = parseDateTime(dto.scheduledFor),
            status = ReminderStatus.fromString(dto.status),
            createdAt = parseDateTime(dto.createdAt),
            sentAt = dto.sentAt?.let { parseDateTime(it) }
        )
    }

    fun toDomain(dto: UpcomingRemindersResponseDto): RemindersList {
        return RemindersList(
            reminders = dto.reminders.map { toDomain(it) },
            totalCount = dto.totalCount
        )
    }

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, dateTimeFormatter)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(dateString.replace("Z", ""))
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
    }
}

