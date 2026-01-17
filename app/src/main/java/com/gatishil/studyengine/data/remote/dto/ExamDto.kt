package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== Subject DTOs ====================

@Serializable
data class SubjectDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String?,
    @SerialName("iconUrl")
    val iconUrl: String?,
    @SerialName("isActive")
    val isActive: Boolean? = true,
    @SerialName("questionCount")
    val questionCount: Int,
    @SerialName("createdAt")
    val createdAt: String? = null,
    @SerialName("updatedAt")
    val updatedAt: String? = null
)

@Serializable
data class SubjectListDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String?,
    @SerialName("iconUrl")
    val iconUrl: String?,
    @SerialName("isActive")
    val isActive: Boolean = true,
    @SerialName("questionCount")
    val questionCount: Int
)

// ==================== Question DTOs ====================

@Serializable
data class QuestionOptionForExamDto(
    @SerialName("id")
    val id: String,
    @SerialName("optionText")
    val optionText: String,
    @SerialName("displayOrder")
    val displayOrder: Int
)

@Serializable
data class QuestionForExamDto(
    @SerialName("id")
    val id: String,
    @SerialName("questionText")
    val questionText: String,
    @SerialName("difficulty")
    val difficulty: String,
    @SerialName("allowMultipleCorrectAnswers")
    val allowMultipleCorrectAnswers: Boolean,
    @SerialName("points")
    val points: Int,
    @SerialName("options")
    val options: List<QuestionOptionForExamDto>
)

@Serializable
data class AvailableQuestionCountDto(
    @SerialName("subjectId")
    val subjectId: String,
    @SerialName("difficulty")
    val difficulty: String?,
    @SerialName("availableCount")
    val availableCount: Int
)

// ==================== Exam DTOs ====================

@Serializable
data class StartExamRequestDto(
    @SerialName("SubjectId")
    val subjectId: String,
    @SerialName("QuestionCount")
    val questionCount: Int = 10,
    @SerialName("DifficultyFilter")
    val difficultyFilter: String? = null,
    @SerialName("TimeLimitMinutes")
    val timeLimitMinutes: Int? = null
)

@Serializable
data class ExamQuestionSetDto(
    @SerialName("examAttemptId")
    val examAttemptId: String,
    @SerialName("examTitle")
    val examTitle: String,
    @SerialName("subjectId")
    val subjectId: String,
    @SerialName("subjectName")
    val subjectName: String,
    @SerialName("totalQuestions")
    val totalQuestions: Int,
    @SerialName("totalPoints")
    val totalPoints: Int,
    @SerialName("difficultyFilter")
    val difficultyFilter: String?,
    @SerialName("timeLimitMinutes")
    val timeLimitMinutes: Int?,
    @SerialName("startedAt")
    val startedAt: String,
    @SerialName("expiresAt")
    val expiresAt: String?,
    @SerialName("questions")
    val questions: List<QuestionForExamDto>
)

@Serializable
data class SubmitAnswerDto(
    @SerialName("QuestionId")
    val questionId: String,
    @SerialName("SelectedOptionIds")
    val selectedOptionIds: List<String>
)

@Serializable
data class SubmitExamRequestDto(
    @SerialName("ExamAttemptId")
    val examAttemptId: String,
    @SerialName("Answers")
    val answers: List<SubmitAnswerDto>
)

@Serializable
data class AnswerOptionDetailDto(
    @SerialName("id")
    val id: String,
    @SerialName("optionText")
    val optionText: String,
    @SerialName("isCorrect")
    val isCorrect: Boolean,
    @SerialName("wasSelected")
    val wasSelected: Boolean
)

@Serializable
data class ExamAnswerResultDto(
    @SerialName("questionId")
    val questionId: String,
    @SerialName("questionText")
    val questionText: String,
    @SerialName("explanation")
    val explanation: String?,
    @SerialName("selectedOptionIds")
    val selectedOptionIds: List<String>,
    @SerialName("correctOptionIds")
    val correctOptionIds: List<String>,
    @SerialName("options")
    val options: List<AnswerOptionDetailDto> = emptyList(),
    @SerialName("isCorrect")
    val isCorrect: Boolean,
    @SerialName("pointsEarned")
    val pointsEarned: Int,
    @SerialName("maxPoints")
    val maxPoints: Int
)

@Serializable
data class ExamResultDto(
    @SerialName("examAttemptId")
    val examAttemptId: String,
    @SerialName("examTitle")
    val examTitle: String,
    @SerialName("subjectId")
    val subjectId: String,
    @SerialName("subjectName")
    val subjectName: String,
    @SerialName("totalQuestions")
    val totalQuestions: Int,
    @SerialName("answeredQuestions")
    val answeredQuestions: Int,
    @SerialName("correctAnswers")
    val correctAnswers: Int,
    @SerialName("totalPoints")
    val totalPoints: Int,
    @SerialName("earnedPoints")
    val earnedPoints: Int,
    @SerialName("scorePercentage")
    val scorePercentage: Double,
    @SerialName("grade")
    val grade: String,
    @SerialName("duration")
    val duration: String,
    @SerialName("startedAt")
    val startedAt: String,
    @SerialName("submittedAt")
    val submittedAt: String,
    @SerialName("answerResults")
    val answerResults: List<ExamAnswerResultDto>
)

@Serializable
data class ExamAttemptSummaryDto(
    @SerialName("id")
    val id: String,
    @SerialName("subjectId")
    val subjectId: String,
    @SerialName("subjectName")
    val subjectName: String,
    @SerialName("examTitle")
    val examTitle: String,
    @SerialName("totalQuestions")
    val totalQuestions: Int,
    @SerialName("totalPoints")
    val totalPoints: Int,
    @SerialName("earnedPoints")
    val earnedPoints: Int?,
    @SerialName("scorePercentage")
    val scorePercentage: Double?,
    @SerialName("status")
    val status: String,
    @SerialName("startedAt")
    val startedAt: String,
    @SerialName("submittedAt")
    val submittedAt: String?
)

@Serializable
data class ExamAttemptPagedResponseDto(
    @SerialName("items")
    val items: List<ExamAttemptSummaryDto>,
    @SerialName("totalCount")
    val totalCount: Int,
    @SerialName("page")
    val page: Int,
    @SerialName("pageSize")
    val pageSize: Int,
    @SerialName("totalPages")
    val totalPages: Int
)

