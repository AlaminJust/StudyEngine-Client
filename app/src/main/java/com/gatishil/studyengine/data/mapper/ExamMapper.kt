package com.gatishil.studyengine.data.mapper

import com.gatishil.studyengine.data.remote.dto.*
import com.gatishil.studyengine.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object ExamMapper {

    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, dateTimeFormatter)
        } catch (e: DateTimeParseException) {
            // Try with just date
            try {
                LocalDateTime.parse(dateString.removeSuffix("Z"), dateTimeFormatter)
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
    }

    // Subject mapping
    fun SubjectListDto.toDomain(): Subject {
        return Subject(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            questionCount = questionCount,
            isActive = isActive
        )
    }

    fun SubjectDto.toDomain(): Subject {
        return Subject(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            questionCount = questionCount,
            isActive = isActive ?: true
        )
    }

    // Question option mapping
    fun QuestionOptionForExamDto.toDomain(): QuestionOption {
        return QuestionOption(
            id = id,
            optionText = optionText,
            displayOrder = displayOrder,
            isCorrect = false // Not revealed in exam
        )
    }

    // Question mapping
    fun QuestionForExamDto.toDomain(): ExamQuestion {
        return ExamQuestion(
            id = id,
            questionText = questionText,
            difficulty = QuestionDifficulty.fromValue(difficulty),
            allowMultipleCorrectAnswers = allowMultipleCorrectAnswers,
            points = points,
            options = options.map { it.toDomain() }.sortedBy { it.displayOrder }
        )
    }

    // Exam question set mapping
    fun ExamQuestionSetDto.toDomain(): ExamQuestionSet {
        return ExamQuestionSet(
            examAttemptId = examAttemptId,
            examTitle = examTitle,
            subjectId = subjectId,
            subjectName = subjectName,
            totalQuestions = totalQuestions,
            totalPoints = totalPoints,
            difficultyFilter = difficultyFilter?.let { QuestionDifficulty.fromValue(it) },
            timeLimitMinutes = timeLimitMinutes,
            startedAt = parseDateTime(startedAt),
            expiresAt = expiresAt?.let { parseDateTime(it) },
            questions = questions.map { it.toDomain() }
        )
    }

    // Start exam request mapping
    fun StartExamRequest.toDto(): StartExamRequestDto {
        return StartExamRequestDto(
            subjectId = subjectId,
            questionCount = questionCount,
            difficultyFilter = difficultyFilter?.value,
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

    // Exam answer result mapping
    fun ExamAnswerResultDto.toDomain(): ExamAnswerResult {
        return ExamAnswerResult(
            questionId = questionId,
            questionText = questionText,
            explanation = explanation,
            selectedOptionIds = selectedOptionIds,
            correctOptionIds = correctOptionIds,
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
            subjectId = subjectId,
            subjectName = subjectName,
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
            subjectId = subjectId,
            subjectName = subjectName,
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

