package com.gatishil.studyengine.domain.model

import java.time.LocalDateTime

/**
 * Question difficulty levels - matches backend enum values
 */
enum class QuestionDifficulty(val value: Int) {
    EASY(1),
    MEDIUM(2),
    HARD(3),
    EXPERT(4);

    companion object {
        fun fromValue(value: Int): QuestionDifficulty {
            return entries.find { it.value == value } ?: MEDIUM
        }

        fun fromString(value: String): QuestionDifficulty {
            return when (value.uppercase()) {
                "EASY" -> EASY
                "MEDIUM" -> MEDIUM
                "HARD" -> HARD
                "EXPERT" -> EXPERT
                else -> MEDIUM
            }
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
 * Category for subjects (BCS, HSC, SSC, etc.)
 */
data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val displayOrder: Int,
    val isActive: Boolean,
    val subjectCount: Int
)

/**
 * Category with subjects
 */
data class CategoryWithSubjects(
    val id: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val displayOrder: Int,
    val isActive: Boolean,
    val subjects: List<Subject>
)

/**
 * Subject for MCQ exams
 */
data class Subject(
    val id: String,
    val name: String,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val description: String?,
    val iconUrl: String?,
    val displayOrder: Int = 0,
    val questionCount: Int,
    val chapterCount: Int = 0,
    val isActive: Boolean = true
)

/**
 * Subject with chapters
 */
data class SubjectWithChapters(
    val id: String,
    val name: String,
    val categoryId: String?,
    val categoryName: String?,
    val description: String?,
    val iconUrl: String?,
    val displayOrder: Int,
    val isActive: Boolean,
    val questionCount: Int,
    val chapters: List<SubjectChapter>
)

/**
 * Subject chapter for MCQ exams
 */
data class SubjectChapter(
    val id: String,
    val subjectId: String,
    val subjectName: String? = null,
    val name: String,
    val description: String?,
    val displayOrder: Int,
    val isActive: Boolean,
    val questionCount: Int
)

/**
 * Tag for filtering questions in exams
 */
data class Tag(
    val id: String,
    val categoryId: String? = null,
    val name: String,
    val usageCount: Int = 0,
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
 * Subject info for exam (minimal info)
 */
data class SubjectInfo(
    val id: String,
    val name: String
)

/**
 * Chapter info for exam (minimal info)
 */
data class ChapterInfo(
    val id: String,
    val name: String,
    val subjectId: String
)

/**
 * Active exam with questions
 */
data class ExamQuestionSet(
    val examAttemptId: String,
    val examTitle: String,
    val subjects: List<SubjectInfo>,
    val chapters: List<ChapterInfo> = emptyList(),
    val totalQuestions: Int,
    val totalPoints: Int,
    val difficultyFilter: QuestionDifficulty?,
    val timeLimitMinutes: Int?,
    val startedAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val questions: List<ExamQuestion>
)

/**
 * Option detail for answer review
 */
data class AnswerOptionDetail(
    val id: String,
    val optionText: String,
    val isCorrect: Boolean,
    val wasSelected: Boolean
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
    val options: List<AnswerOptionDetail>,
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
    val subjects: List<SubjectInfo>,
    val chapters: List<ChapterInfo> = emptyList(),
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
    val subjects: List<SubjectInfo>,
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
 * Subject selection for starting an exam (with optional chapter filtering)
 */
data class ExamSubjectSelection(
    val subjectId: String,
    val chapterIds: List<String>? = null
)

/**
 * Request to start an exam
 */
data class StartExamRequest(
    val subjects: List<ExamSubjectSelection>,
    val questionCount: Int = 10,
    val difficultyFilter: QuestionDifficulty? = null,
    val timeLimitMinutes: Int? = null,
    val tagIds: List<String>? = null
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

