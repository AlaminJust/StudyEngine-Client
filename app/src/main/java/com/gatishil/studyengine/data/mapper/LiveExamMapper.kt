package com.gatishil.studyengine.data.mapper

import com.gatishil.studyengine.data.remote.dto.LiveExamPublicResponseDto
import com.gatishil.studyengine.domain.model.LiveExam
import com.gatishil.studyengine.domain.model.LiveExamStatus
import com.gatishil.studyengine.domain.model.QuestionDifficulty
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object LiveExamMapper {

    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            val cleanedString = dateString.trim()

            val instant = if (cleanedString.endsWith("Z")) {
                Instant.parse(cleanedString)
            } else if (cleanedString.contains("+") || hasTimezoneOffset(cleanedString)) {
                ZonedDateTime.parse(cleanedString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
            } else {
                LocalDateTime.parse(cleanedString, dateTimeFormatter).toInstant(ZoneOffset.UTC)
            }

            instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(dateString.removeSuffix("Z"), dateTimeFormatter)
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
    }

    private fun hasTimezoneOffset(dateString: String): Boolean {
        val tIndex = dateString.indexOf('T')
        if (tIndex == -1) return false
        val afterTime = dateString.substring(tIndex + 1)
        return afterTime.length > 8 && afterTime.substring(8).contains("-")
    }

    fun LiveExamPublicResponseDto.toDomain(): LiveExam {
        return LiveExam(
            id = id,
            title = title,
            description = description,
            questionCount = questionCount,
            timeLimitMinutes = timeLimitMinutes,
            difficultyFilter = difficultyFilter?.let { QuestionDifficulty.fromString(it) },
            scheduledStartTime = parseDateTime(scheduledStartTime),
            scheduledEndTime = parseDateTime(scheduledEndTime),
            status = LiveExamStatus.fromString(status),
            hasAttempted = hasAttempted
        )
    }
}
