package com.example.studyengine.data.mapper

import com.example.studyengine.data.local.entity.*
import com.example.studyengine.data.remote.dto.*
import com.example.studyengine.domain.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Mapper functions for User
 */
object UserMapper {
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    fun UserDto.toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        timeZone = timeZone,
        profilePictureUrl = profilePictureUrl,
        createdAt = parseDateTime(createdAt)
    )

    fun UserDto.toEntity(): UserEntity = UserEntity(
        id = id,
        name = name,
        email = email,
        timeZone = timeZone,
        profilePictureUrl = profilePictureUrl,
        createdAt = createdAt
    )

    fun UserEntity.toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        timeZone = timeZone,
        profilePictureUrl = profilePictureUrl,
        createdAt = parseDateTime(createdAt)
    )

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, dateTimeFormatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}

/**
 * Mapper functions for Auth
 */
object AuthMapper {
    fun AuthResponseDto.toDomain(): AuthResult = AuthResult(
        tokens = AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiration = parseDateTime(accessTokenExpiration),
            refreshTokenExpiration = parseDateTime(refreshTokenExpiration)
        ),
        user = with(UserMapper) { user.toDomain() }
    )

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}

/**
 * Mapper functions for Book
 */
object BookMapper {
    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    fun BookDto.toDomain(): Book = Book(
        id = id,
        userId = userId,
        title = title,
        subject = subject,
        totalPages = totalPages,
        effectiveTotalPages = effectiveTotalPages,
        difficulty = difficulty,
        priority = priority,
        targetEndDate = targetEndDate?.let { parseDate(it) },
        createdAt = parseDateTime(createdAt),
        ignoredChapterCount = ignoredChapterCount,
        studyPlan = studyPlan?.let { with(StudyPlanMapper) { it.toDomain() } },
        chapters = chapters.map { with(ChapterMapper) { it.toDomain() } }
    )

    fun BookDto.toEntity(): BookEntity = BookEntity(
        id = id,
        userId = userId,
        title = title,
        subject = subject,
        totalPages = totalPages,
        effectiveTotalPages = effectiveTotalPages,
        difficulty = difficulty,
        priority = priority,
        targetEndDate = targetEndDate,
        createdAt = createdAt,
        ignoredChapterCount = ignoredChapterCount
    )

    fun BookEntity.toDomain(
        studyPlan: StudyPlan? = null,
        chapters: List<Chapter> = emptyList()
    ): Book = Book(
        id = id,
        userId = userId,
        title = title,
        subject = subject,
        totalPages = totalPages,
        effectiveTotalPages = effectiveTotalPages,
        difficulty = difficulty,
        priority = priority,
        targetEndDate = targetEndDate?.let { parseDate(it) },
        createdAt = parseDateTime(createdAt),
        ignoredChapterCount = ignoredChapterCount,
        studyPlan = studyPlan,
        chapters = chapters
    )

    fun CreateBookRequest.toDto(): CreateBookRequestDto = CreateBookRequestDto(
        title = title,
        subject = subject,
        totalPages = totalPages,
        difficulty = difficulty,
        priority = priority,
        targetEndDate = targetEndDate?.format(dateFormatter)
    )

    fun UpdateBookRequest.toDto(): UpdateBookRequestDto = UpdateBookRequestDto(
        title = title,
        subject = subject,
        totalPages = totalPages,
        difficulty = difficulty,
        priority = priority,
        targetEndDate = targetEndDate?.format(dateFormatter)
    )

    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, dateTimeFormatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}

/**
 * Mapper functions for Chapter
 */
object ChapterMapper {
    fun ChapterDto.toDomain(): Chapter = Chapter(
        id = id,
        bookId = bookId,
        title = title,
        startPage = startPage,
        endPage = endPage,
        orderIndex = orderIndex,
        pageCount = pageCount,
        isIgnored = isIgnored,
        ignoreReason = ignoreReason
    )

    fun ChapterDto.toEntity(): ChapterEntity = ChapterEntity(
        id = id,
        bookId = bookId,
        title = title,
        startPage = startPage,
        endPage = endPage,
        orderIndex = orderIndex,
        pageCount = pageCount,
        isIgnored = isIgnored,
        ignoreReason = ignoreReason
    )

    fun ChapterEntity.toDomain(): Chapter = Chapter(
        id = id,
        bookId = bookId,
        title = title,
        startPage = startPage,
        endPage = endPage,
        orderIndex = orderIndex,
        pageCount = pageCount,
        isIgnored = isIgnored,
        ignoreReason = ignoreReason
    )

    fun CreateChapterRequest.toDto(): CreateChapterRequestDto = CreateChapterRequestDto(
        title = title,
        startPage = startPage,
        endPage = endPage,
        orderIndex = orderIndex
    )

    fun UpdateChapterRequest.toDto(): UpdateChapterRequestDto = UpdateChapterRequestDto(
        title = title,
        startPage = startPage,
        endPage = endPage,
        orderIndex = orderIndex
    )
}

/**
 * Mapper functions for StudyPlan
 */
object StudyPlanMapper {
    private val dateFormatter = DateTimeFormatter.ISO_DATE

    fun StudyPlanDto.toDomain(): StudyPlan = StudyPlan(
        id = id,
        bookId = bookId,
        startDate = parseDate(startDate),
        endDate = parseDate(endDate),
        status = StudyPlanStatus.fromString(status),
        recurrenceRule = recurrenceRule?.let { with(RecurrenceRuleMapper) { it.toDomain() } }
    )

    fun StudyPlanDto.toEntity(): StudyPlanEntity = StudyPlanEntity(
        id = id,
        bookId = bookId,
        startDate = startDate,
        endDate = endDate,
        status = status
    )

    fun StudyPlanEntity.toDomain(recurrenceRule: RecurrenceRule? = null): StudyPlan = StudyPlan(
        id = id,
        bookId = bookId,
        startDate = parseDate(startDate),
        endDate = parseDate(endDate),
        status = StudyPlanStatus.fromString(status),
        recurrenceRule = recurrenceRule
    )

    fun CreateStudyPlanRequest.toDto(): CreateStudyPlanRequestDto = CreateStudyPlanRequestDto(
        startDate = startDate.format(dateFormatter),
        endDate = endDate.format(dateFormatter),
        recurrenceRule = recurrenceRule?.let { with(RecurrenceRuleMapper) { it.toDto() } }
    )

    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
}

/**
 * Mapper functions for RecurrenceRule
 */
object RecurrenceRuleMapper {
    fun RecurrenceRuleDto.toDomain(): RecurrenceRule = RecurrenceRule(
        id = id,
        studyPlanId = studyPlanId,
        type = RecurrenceType.fromString(type),
        interval = interval,
        daysOfWeek = daysOfWeek.map { DayOfWeek.of(if (it == 0) 7 else it) }
    )

    fun RecurrenceRuleDto.toEntity(): RecurrenceRuleEntity = RecurrenceRuleEntity(
        id = id,
        studyPlanId = studyPlanId,
        type = type,
        interval = interval,
        daysOfWeek = daysOfWeek.joinToString(",")
    )

    fun RecurrenceRuleEntity.toDomain(): RecurrenceRule = RecurrenceRule(
        id = id,
        studyPlanId = studyPlanId,
        type = RecurrenceType.fromString(type),
        interval = interval,
        daysOfWeek = daysOfWeek.split(",").filter { it.isNotEmpty() }.map {
            val dayValue = it.toInt()
            DayOfWeek.of(if (dayValue == 0) 7 else dayValue)
        }
    )

    fun CreateRecurrenceRuleRequest.toDto(): CreateRecurrenceRuleRequestDto = CreateRecurrenceRuleRequestDto(
        type = type.name,
        interval = interval,
        daysOfWeek = daysOfWeek?.map { it.value }
    )
}

/**
 * Mapper functions for StudySession
 */
object StudySessionMapper {
    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val timeFormatter = DateTimeFormatter.ISO_TIME

    fun StudySessionDto.toDomain(): StudySession = StudySession(
        id = id,
        userId = userId,
        bookId = bookId,
        chapterId = chapterId,
        sessionDate = parseDate(sessionDate),
        startTime = parseTime(startTime),
        endTime = parseTime(endTime),
        plannedPages = plannedPages,
        completedPages = completedPages,
        status = StudySessionStatus.fromString(status),
        bookTitle = bookTitle,
        chapterTitle = chapterTitle
    )

    fun StudySessionDto.toEntity(): StudySessionEntity = StudySessionEntity(
        id = id,
        userId = userId,
        bookId = bookId,
        chapterId = chapterId,
        sessionDate = sessionDate,
        startTime = startTime,
        endTime = endTime,
        plannedPages = plannedPages,
        completedPages = completedPages,
        status = status,
        bookTitle = bookTitle,
        chapterTitle = chapterTitle
    )

    fun StudySessionEntity.toDomain(): StudySession = StudySession(
        id = id,
        userId = userId,
        bookId = bookId,
        chapterId = chapterId,
        sessionDate = parseDate(sessionDate),
        startTime = parseTime(startTime),
        endTime = parseTime(endTime),
        plannedPages = plannedPages,
        completedPages = completedPages,
        status = StudySessionStatus.fromString(status),
        bookTitle = bookTitle,
        chapterTitle = chapterTitle
    )

    fun CompleteSessionRequest.toDto(): CompleteSessionRequestDto = CompleteSessionRequestDto(
        completedPages = completedPages,
        notes = notes
    )

    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    private fun parseTime(timeString: String): LocalTime {
        return try {
            LocalTime.parse(timeString, timeFormatter)
        } catch (e: Exception) {
            try {
                LocalTime.parse(timeString)
            } catch (e2: Exception) {
                LocalTime.NOON
            }
        }
    }
}

/**
 * Mapper functions for Schedule models
 */
object ScheduleMapper {
    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val timeFormatter = DateTimeFormatter.ISO_TIME

    // UserAvailability
    fun UserAvailabilityDto.toDomain(): UserAvailability = UserAvailability(
        id = id,
        userId = userId,
        dayOfWeek = DayOfWeek.of(if (dayOfWeek == 0) 7 else dayOfWeek),
        startTime = parseTime(startTime),
        endTime = parseTime(endTime),
        isActive = isActive
    )

    fun UserAvailabilityDto.toEntity(): UserAvailabilityEntity = UserAvailabilityEntity(
        id = id,
        userId = userId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        isActive = isActive
    )

    fun UserAvailabilityEntity.toDomain(): UserAvailability = UserAvailability(
        id = id,
        userId = userId,
        dayOfWeek = DayOfWeek.of(if (dayOfWeek == 0) 7 else dayOfWeek),
        startTime = parseTime(startTime),
        endTime = parseTime(endTime),
        isActive = isActive
    )

    fun CreateUserAvailabilityRequest.toDto(): CreateUserAvailabilityRequestDto = CreateUserAvailabilityRequestDto(
        dayOfWeek = dayOfWeek.value,
        startTime = startTime.format(timeFormatter),
        endTime = endTime.format(timeFormatter)
    )

    // ScheduleOverride
    fun ScheduleOverrideDto.toDomain(): ScheduleOverride = ScheduleOverride(
        id = id,
        userId = userId,
        overrideDate = parseDate(overrideDate),
        startTime = startTime?.let { parseTime(it) },
        endTime = endTime?.let { parseTime(it) },
        isOff = isOff
    )

    fun ScheduleOverrideDto.toEntity(): ScheduleOverrideEntity = ScheduleOverrideEntity(
        id = id,
        userId = userId,
        overrideDate = overrideDate,
        startTime = startTime,
        endTime = endTime,
        isOff = isOff
    )

    fun ScheduleOverrideEntity.toDomain(): ScheduleOverride = ScheduleOverride(
        id = id,
        userId = userId,
        overrideDate = parseDate(overrideDate),
        startTime = startTime?.let { parseTime(it) },
        endTime = endTime?.let { parseTime(it) },
        isOff = isOff
    )

    fun CreateScheduleOverrideRequest.toDto(): CreateScheduleOverrideRequestDto = CreateScheduleOverrideRequestDto(
        overrideDate = overrideDate.format(dateFormatter),
        startTime = startTime?.format(timeFormatter),
        endTime = endTime?.format(timeFormatter),
        isOff = isOff
    )

    // ScheduleContext
    fun ScheduleContextDto.toDomain(): ScheduleContext = ScheduleContext(
        id = id,
        userId = userId,
        contextType = contextType,
        startDate = parseDate(startDate),
        endDate = parseDate(endDate),
        loadMultiplier = loadMultiplier
    )

    fun ScheduleContextDto.toEntity(): ScheduleContextEntity = ScheduleContextEntity(
        id = id,
        userId = userId,
        contextType = contextType,
        startDate = startDate,
        endDate = endDate,
        loadMultiplier = loadMultiplier
    )

    fun ScheduleContextEntity.toDomain(): ScheduleContext = ScheduleContext(
        id = id,
        userId = userId,
        contextType = contextType,
        startDate = parseDate(startDate),
        endDate = parseDate(endDate),
        loadMultiplier = loadMultiplier
    )

    fun CreateScheduleContextRequest.toDto(): CreateScheduleContextRequestDto = CreateScheduleContextRequestDto(
        contextType = contextType,
        startDate = startDate.format(dateFormatter),
        endDate = endDate.format(dateFormatter),
        loadMultiplier = loadMultiplier
    )

    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    private fun parseTime(timeString: String): LocalTime {
        return try {
            LocalTime.parse(timeString, timeFormatter)
        } catch (e: Exception) {
            try {
                LocalTime.parse(timeString)
            } catch (e2: Exception) {
                LocalTime.NOON
            }
        }
    }
}

