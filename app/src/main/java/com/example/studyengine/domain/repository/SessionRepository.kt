package com.example.studyengine.domain.repository

import com.example.studyengine.core.util.Resource
import com.example.studyengine.domain.model.CompleteSessionRequest
import com.example.studyengine.domain.model.StudySession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for study session operations
 */
interface SessionRepository {

    /**
     * Get all sessions for the current user
     */
    fun getSessions(): Flow<Resource<List<StudySession>>>

    /**
     * Get sessions for a specific date range
     */
    fun getSessionsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Resource<List<StudySession>>>

    /**
     * Get sessions for today
     */
    fun getTodaySessions(): Flow<Resource<List<StudySession>>>

    /**
     * Get sessions for a specific date
     */
    fun getSessionsByDate(date: LocalDate): Flow<Resource<List<StudySession>>>

    /**
     * Get a specific session by ID
     */
    suspend fun getSessionById(sessionId: String): Resource<StudySession>

    /**
     * Start a session
     */
    suspend fun startSession(sessionId: String): Resource<StudySession>

    /**
     * Complete a session
     */
    suspend fun completeSession(
        sessionId: String,
        request: CompleteSessionRequest
    ): Resource<StudySession>

    /**
     * Mark a session as missed
     */
    suspend fun markAsMissed(sessionId: String): Resource<StudySession>

    /**
     * Cancel a session
     */
    suspend fun cancelSession(sessionId: String): Resource<StudySession>

    /**
     * Refresh sessions from remote
     */
    suspend fun refreshSessions(): Resource<List<StudySession>>

    /**
     * Get upcoming sessions count
     */
    fun getUpcomingSessionsCount(): Flow<Int>

    /**
     * Get completed sessions count for today
     */
    fun getTodayCompletedCount(): Flow<Int>
}

