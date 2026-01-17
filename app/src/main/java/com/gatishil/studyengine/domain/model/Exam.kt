package com.gatishil.studyengine.domain.model

import java.time.LocalDateTime

/**
 * Question difficulty levels
 */
enum class QuestionDifficulty(val value: Int) {
    EASY(0),
    MEDIUM(1),
    HARD(2);

    companion object {
        fun fromValue(value: Int): QuestionDifficulty {
            return entries.find { it.value == value } ?: MEDIUM
        }
    }
}

/**
 * Exam attempt status
 */
enum class ExamAttemptStatus {
    IN_PROGRESS,
    SUBMITTED,
    TIMED_OUT,
    CANCELLED;

    companion object {
        fun fromString(value: String): ExamAttemptStatus {
            return when (value.uppercase()) {
                "INPROGRESS", "IN_PROGRESS" -> IN_PROGRESS
                "SUBMITTED" -> SUBMITTED
                "TIMEDOUT", "TIMED_OUT" -> TIMED_OUT
                "CANCELLED" -> CANCELLED
                else -> IN_PROGRESS
            }
        }
    }
}

/**
 * Subject for MCQ exams
 */
data class Subject(
    val id: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val questionCount: Int,
    val isActive: Boolean = true
)

/**
 * Question option for exam
 */
data class QuestionOption(
    val id: String,
    val optionText: String,
    val displayOrder: Int,
    val isCorrect: Boolean = false
)

/**
 * Question for exam (without correct answer revealed)
 */
data class ExamQuestion(
    val id: String,
    val questionText: String,
    val difficulty: QuestionDifficulty,
    val allowMultipleCorrectAnswers: Boolean,
    val points: Int,
    val options: List<QuestionOption>
)

/**
 * Active exam with questions
 */
data class ExamQuestionSet(
    val examAttemptId: String,
    val examTitle: String,
    val subjectId: String,
    val subjectName: String,
    val totalQuestions: Int,
    val totalPoints: Int,
    val difficultyFilter: QuestionDifficulty?,
    val timeLimitMinutes: Int?,
    val startedAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val questions: List<ExamQuestion>
)

/**
 * Answer result for a single question
 */
data class ExamAnswerResult(
    val questionId: String,
    val questionText: String,
    val explanation: String?,
    val selectedOptionIds: List<String>,
    val correctOptionIds: List<String>,
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val maxPoints: Int
)

/**
 * Exam result after submission
 */
data class ExamResult(
    val examAttemptId: String,
    val examTitle: String,
    val subjectId: String,
    val subjectName: String,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctAnswers: Int,
    val totalPoints: Int,
    val earnedPoints: Int,
    val scorePercentage: Double,
    val grade: String,
    val duration: String,
    val startedAt: LocalDateTime,
    val submittedAt: LocalDateTime,
    val answerResults: List<ExamAnswerResult>
)

/**
 * Exam attempt summary for history
 */
data class ExamAttemptSummary(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val examTitle: String,
    val totalQuestions: Int,
    val totalPoints: Int,
    val earnedPoints: Int?,
    val scorePercentage: Double?,
    val status: ExamAttemptStatus,
    val startedAt: LocalDateTime,
    val submittedAt: LocalDateTime?
)

/**
 * Paged response for exam attempts
 */
data class ExamAttemptPagedResponse(
    val items: List<ExamAttemptSummary>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

/**
 * Request to start an exam
 */
data class StartExamRequest(
    val subjectId: String,
    val questionCount: Int = 10,
    val difficultyFilter: QuestionDifficulty? = null,
    val timeLimitMinutes: Int? = null
)

/**
 * Answer for a question
 */
data class SubmitAnswer(
    val questionId: String,
    val selectedOptionIds: List<String>
)

/**
 * Request to submit an exam
 */
data class SubmitExamRequest(
    val examAttemptId: String,
    val answers: List<SubmitAnswer>
)

