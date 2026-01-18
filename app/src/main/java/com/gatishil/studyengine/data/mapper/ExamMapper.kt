package com.gatishil.studyengine.data.mapper

import com.gatishil.studyengine.data.remote.dto.*
import com.gatishil.studyengine.domain.model.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ExamMapper {

    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Parse datetime string from backend (which is in UTC) to LocalDateTime in the device's timezone.
     * This ensures proper time comparison for exam timers.
     */
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

        val timePartIndex = tIndex + 1
        val afterTime = dateString.substring(timePartIndex)

        return afterTime.length > 8 && afterTime.substring(8).contains("-")
    }

    // Category mapping
    fun CategoryListDto.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            displayOrder = displayOrder,
            isActive = isActive,
            subjectCount = subjectCount
        )
    }

    fun CategoryDto.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            displayOrder = displayOrder,
            isActive = isActive,
            subjectCount = subjectCount
        )
    }

    fun CategoryWithSubjectsDto.toDomain(): CategoryWithSubjects {
        return CategoryWithSubjects(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            displayOrder = displayOrder,
            isActive = isActive,
            subjects = subjects.map { it.toDomain() }
        )
    }

    // Subject mapping
    fun SubjectListDto.toDomain(): Subject {
        return Subject(
            id = id,
            name = name,
            categoryId = categoryId,
            categoryName = categoryName,
            description = description,
            iconUrl = iconUrl,
            displayOrder = displayOrder,
            questionCount = questionCount,
            chapterCount = chapterCount,
            isActive = isActive
        )
    }

    fun SubjectDto.toDomain(): Subject {
        return Subject(
            id = id,
            name = name,
            categoryId = categoryId,
            categoryName = categoryName,
            description = description,
            iconUrl = iconUrl,
            displayOrder = displayOrder,
            questionCount = questionCount,
            chapterCount = chapterCount,
            isActive = isActive ?: true
        )
    }

    fun SubjectWithChaptersDto.toDomain(): SubjectWithChapters {
        return SubjectWithChapters(
            id = id,
            name = name,
            categoryId = categoryId,
            categoryName = categoryName,
            description = description,
            iconUrl = iconUrl,
            displayOrder = displayOrder,
            isActive = isActive,
            questionCount = questionCount,
            chapters = chapters.map { it.toDomain() }
        )
    }

    // Subject chapter mapping
    fun SubjectChapterListDto.toDomain(): SubjectChapter {
        return SubjectChapter(
            id = id,
            subjectId = subjectId,
            name = name,
            description = description,
            displayOrder = displayOrder,
            isActive = isActive,
            questionCount = questionCount
        )
    }

    fun SubjectChapterDto.toDomain(): SubjectChapter {
        return SubjectChapter(
            id = id,
            subjectId = subjectId,
            subjectName = subjectName,
            name = name,
            description = description,
            displayOrder = displayOrder,
            isActive = isActive,
            questionCount = questionCount
        )
    }

    // Question option mapping
    fun QuestionOptionForExamDto.toDomain(): QuestionOption {
        return QuestionOption(
            id = id,
            optionText = optionText,
            displayOrder = displayOrder,
            isCorrect = false
        )
    }

    // Question mapping
    fun QuestionForExamDto.toDomain(): ExamQuestion {
        return ExamQuestion(
            id = id,
            questionText = questionText,
            difficulty = QuestionDifficulty.fromString(difficulty),
            allowMultipleCorrectAnswers = allowMultipleCorrectAnswers,
            points = points,
            options = options.map { it.toDomain() }.sortedBy { it.displayOrder }
        )
    }

    // Subject info mapping
    fun SubjectInfoDto.toDomain(): SubjectInfo {
        return SubjectInfo(
            id = id,
            name = name
        )
    }

    // Chapter info mapping
    fun ChapterInfoDto.toDomain(): ChapterInfo {
        return ChapterInfo(
            id = id,
            name = name,
            subjectId = subjectId
        )
    }

    // Exam question set mapping
    fun ExamQuestionSetDto.toDomain(): ExamQuestionSet {
        return ExamQuestionSet(
            examAttemptId = examAttemptId,
            examTitle = examTitle,
            subjects = subjects.map { it.toDomain() },
            chapters = chapters.map { it.toDomain() },
            totalQuestions = totalQuestions,
            totalPoints = totalPoints,
            difficultyFilter = difficultyFilter?.let { QuestionDifficulty.fromString(it) },
            timeLimitMinutes = timeLimitMinutes,
            startedAt = parseDateTime(startedAt),
            expiresAt = expiresAt?.let { parseDateTime(it) },
            questions = questions.map { it.toDomain() }
        )
    }

    // Exam subject selection mapping
    fun ExamSubjectSelection.toDto(): ExamSubjectSelectionDto {
        return ExamSubjectSelectionDto(
            subjectId = subjectId,
            chapterIds = chapterIds
        )
    }

    // Start exam request mapping
    fun StartExamRequest.toDto(): StartExamRequestDto {
        return StartExamRequestDto(
            subjects = subjects.map { it.toDto() },
            questionCount = questionCount,
            difficultyFilter = difficultyFilter?.name,
            timeLimitMinutes = timeLimitMinutes
        )
    }

    // Submit answer mapping
    fun SubmitAnswer.toDto(): SubmitAnswerDto {
        return SubmitAnswerDto(
            questionId = questionId,
            selectedOptionIds = selectedOptionIds
        )
    }

    // Submit exam request mapping
    fun SubmitExamRequest.toDto(): SubmitExamRequestDto {
        return SubmitExamRequestDto(
            examAttemptId = examAttemptId,
            answers = answers.map { it.toDto() }
        )
    }

    // Answer option detail mapping
    fun AnswerOptionDetailDto.toDomain(): AnswerOptionDetail {
        return AnswerOptionDetail(
            id = id,
            optionText = optionText,
            isCorrect = isCorrect,
            wasSelected = wasSelected
        )
    }

    // Exam answer result mapping
    fun ExamAnswerResultDto.toDomain(): ExamAnswerResult {
        return ExamAnswerResult(
            questionId = questionId,
            questionText = questionText,
            explanation = explanation,
            selectedOptionIds = selectedOptionIds,
            correctOptionIds = correctOptionIds,
            options = options.map { it.toDomain() },
            isCorrect = isCorrect,
            pointsEarned = pointsEarned,
            maxPoints = maxPoints
        )
    }

    // Exam result mapping
    fun ExamResultDto.toDomain(): ExamResult {
        return ExamResult(
            examAttemptId = examAttemptId,
            examTitle = examTitle,
            subjects = subjects.map { it.toDomain() },
            chapters = chapters.map { it.toDomain() },
            totalQuestions = totalQuestions,
            answeredQuestions = answeredQuestions,
            correctAnswers = correctAnswers,
            totalPoints = totalPoints,
            earnedPoints = earnedPoints,
            scorePercentage = scorePercentage,
            grade = grade,
            duration = duration,
            startedAt = parseDateTime(startedAt),
            submittedAt = parseDateTime(submittedAt),
            answerResults = answerResults.map { it.toDomain() }
        )
    }

    // Exam attempt summary mapping
    fun ExamAttemptSummaryDto.toDomain(): ExamAttemptSummary {
        return ExamAttemptSummary(
            id = id,
            subjects = subjects.map { it.toDomain() },
            examTitle = examTitle,
            totalQuestions = totalQuestions,
            totalPoints = totalPoints,
            earnedPoints = earnedPoints,
            scorePercentage = scorePercentage,
            status = ExamAttemptStatus.fromString(status),
            startedAt = parseDateTime(startedAt),
            submittedAt = submittedAt?.let { parseDateTime(it) }
        )
    }

    // Paged response mapping
    fun ExamAttemptPagedResponseDto.toDomain(): ExamAttemptPagedResponse {
        return ExamAttemptPagedResponse(
            items = items.map { it.toDomain() },
            totalCount = totalCount,
            page = page,
            pageSize = pageSize,
            totalPages = totalPages
        )
    }
}
