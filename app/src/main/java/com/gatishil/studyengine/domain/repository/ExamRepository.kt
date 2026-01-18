package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.*

/**
 * Repository interface for exam operations
 */
interface ExamRepository {

    // ==================== Category Operations ====================

    /**
     * Get all available categories
     */
    suspend fun getCategories(includeInactive: Boolean = false): Resource<List<Category>>

    /**
     * Get all categories with their subjects
     */
    suspend fun getCategoriesWithSubjects(includeInactive: Boolean = false): Resource<List<CategoryWithSubjects>>

    /**
     * Get category by ID
     */
    suspend fun getCategoryById(categoryId: String): Resource<Category>

    /**
     * Get category with its subjects
     */
    suspend fun getCategoryWithSubjects(categoryId: String): Resource<CategoryWithSubjects>

    // ==================== Subject Operations ====================

    /**
     * Get all available subjects for exams
     */
    suspend fun getSubjects(includeInactive: Boolean = false): Resource<List<Subject>>

    /**
     * Get subjects by category ID
     */
    suspend fun getSubjectsByCategory(categoryId: String, includeInactive: Boolean = false): Resource<List<Subject>>

    /**
     * Get subject by ID
     */
    suspend fun getSubjectById(subjectId: String): Resource<Subject>

    /**
     * Get subject with its chapters
     */
    suspend fun getSubjectWithChapters(subjectId: String): Resource<SubjectWithChapters>

    // ==================== Subject Chapter Operations ====================

    /**
     * Get chapters for a subject
     */
    suspend fun getSubjectChapters(subjectId: String, includeInactive: Boolean = false): Resource<List<SubjectChapter>>

    /**
     * Get subject chapter by ID
     */
    suspend fun getSubjectChapterById(chapterId: String): Resource<SubjectChapter>

    // ==================== Question Operations ====================

    /**
     * Get available question count for a subject
     */
    suspend fun getAvailableQuestionCount(
        subjectId: String,
        difficulty: QuestionDifficulty? = null
    ): Resource<Int>

    // ==================== Exam Operations ====================

    /**
     * Start a new exam
     */
    suspend fun startExam(request: StartExamRequest): Resource<ExamQuestionSet>

    /**
     * Get current in-progress exam
     */
    suspend fun getCurrentExam(): Resource<ExamQuestionSet?>

    /**
     * Submit exam answers
     */
    suspend fun submitExam(request: SubmitExamRequest): Resource<ExamResult>

    /**
     * Get exam result by attempt ID
     */
    suspend fun getExamResult(examAttemptId: String): Resource<ExamResult>

    /**
     * Get exam history with optional filters
     */
    suspend fun getExamHistory(
        subjectId: String? = null,
        status: ExamAttemptStatus? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Resource<ExamAttemptPagedResponse>

    /**
     * Cancel an in-progress exam
     */
    suspend fun cancelExam(examAttemptId: String): Resource<Unit>
}
