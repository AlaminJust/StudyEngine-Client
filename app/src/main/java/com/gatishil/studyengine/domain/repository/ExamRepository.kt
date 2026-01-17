package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.*

/**
 * Repository interface for exam operations
 */
interface ExamRepository {

    /**
     * Get all available subjects for exams
     */
    suspend fun getSubjects(includeInactive: Boolean = false): Resource<List<Subject>>

    /**
     * Get subject by ID
     */
    suspend fun getSubjectById(subjectId: String): Resource<Subject>

    /**
     * Get available question count for a subject
     */
    suspend fun getAvailableQuestionCount(
        subjectId: String,
        difficulty: QuestionDifficulty? = null
    ): Resource<Int>

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

