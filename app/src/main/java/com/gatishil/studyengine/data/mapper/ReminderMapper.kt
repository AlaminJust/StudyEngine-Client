package com.gatishil.studyengine.data.mapper

import com.gatishil.studyengine.data.remote.dto.CustomReminderResponseDto
import com.gatishil.studyengine.data.remote.dto.UpcomingRemindersResponseDto
import com.gatishil.studyengine.domain.model.CustomReminder
import com.gatishil.studyengine.domain.model.ReminderStatus
import com.gatishil.studyengine.domain.model.RemindersList
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
            scheduledFor = parseUtcToLocal(dto.scheduledFor),
            status = ReminderStatus.fromString(dto.status),
            createdAt = parseUtcToLocal(dto.createdAt),
            sentAt = dto.sentAt?.let { parseUtcToLocal(it) }
        )
    }

    fun toDomain(dto: UpcomingRemindersResponseDto): RemindersList {
        return RemindersList(
            reminders = dto.reminders.map { toDomain(it) },
            totalCount = dto.totalCount
        )
    }

    /**
     * Parse UTC datetime string from backend and convert to local timezone
     */
    private fun parseUtcToLocal(dateString: String): LocalDateTime {
        return try {
            // Try parsing as ISO instant (with Z suffix)
            val instant = Instant.parse(dateString)
            instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        } catch (e: Exception) {
            try {
                // Try parsing as ZonedDateTime
                val zonedDateTime = ZonedDateTime.parse(dateString)
                zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
            } catch (e2: Exception) {
                try {
                    // Fallback: parse as local datetime and assume it's UTC
                    val cleanedString = dateString.replace("Z", "")
                    val utcDateTime = LocalDateTime.parse(cleanedString, dateTimeFormatter)
                    utcDateTime.atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime()
                } catch (e3: Exception) {
                    // Last resort: return current time
                    LocalDateTime.now()
                }
            }
        }
    }
}

