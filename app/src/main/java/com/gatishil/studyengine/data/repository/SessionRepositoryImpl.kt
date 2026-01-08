package com.gatishil.studyengine.data.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.local.dao.StudySessionDao
import com.gatishil.studyengine.data.mapper.StudySessionMapper
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.domain.model.CompleteSessionRequest
import com.gatishil.studyengine.domain.model.StudySession
import com.gatishil.studyengine.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi,
    private val sessionDao: StudySessionDao
) : SessionRepository {

    private val dateFormatter = DateTimeFormatter.ISO_DATE

    override fun getSessions(): Flow<Resource<List<StudySession>>> {
        return sessionDao.getAllSessions()
            .map { sessions ->
                Resource.success(sessions.map { with(StudySessionMapper) { it.toDomain() } })
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override fun getSessionsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Resource<List<StudySession>>> {
        return sessionDao.getSessionsByDateRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        ).map { sessions ->
            Resource.success(sessions.map { with(StudySessionMapper) { it.toDomain() } })
        }.catch { e -> emit(Resource.error(e, e.message)) }
    }

    override fun getTodaySessions(): Flow<Resource<List<StudySession>>> {
        val today = LocalDate.now().format(dateFormatter)
        return sessionDao.getSessionsByDate(today)
            .map { sessions ->
                Resource.success(sessions.map { with(StudySessionMapper) { it.toDomain() } })
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override fun getSessionsByDate(date: LocalDate): Flow<Resource<List<StudySession>>> {
        return sessionDao.getSessionsByDate(date.format(dateFormatter))
            .map { sessions ->
                Resource.success(sessions.map { with(StudySessionMapper) { it.toDomain() } })
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override suspend fun getSessionById(sessionId: String): Resource<StudySession> {
        return try {
            val session = sessionDao.getSessionById(sessionId)
                ?: return Resource.error(Exception("Session not found"))

            Resource.success(with(StudySessionMapper) { session.toDomain() })
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun startSession(sessionId: String): Resource<StudySession> {
        return try {
            val response = api.startSession(sessionId)

            if (response.isSuccessful) {
                response.body()?.let { sessionDto ->
                    sessionDao.insertSession(with(StudySessionMapper) { sessionDto.toEntity() })
                    Resource.success(with(StudySessionMapper) { sessionDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Start session failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun completeSession(
        sessionId: String,
        request: CompleteSessionRequest
    ): Resource<StudySession> {
        return try {
            val response = api.completeSession(
                sessionId,
                with(StudySessionMapper) { request.toDto() }
            )

            if (response.isSuccessful) {
                response.body()?.let { sessionDto ->
                    sessionDao.insertSession(with(StudySessionMapper) { sessionDto.toEntity() })
                    Resource.success(with(StudySessionMapper) { sessionDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Complete session failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun markAsMissed(sessionId: String): Resource<StudySession> {
        return try {
            val response = api.markSessionAsMissed(sessionId)

            if (response.isSuccessful) {
                response.body()?.let { sessionDto ->
                    sessionDao.insertSession(with(StudySessionMapper) { sessionDto.toEntity() })
                    Resource.success(with(StudySessionMapper) { sessionDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Mark as missed failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun cancelSession(sessionId: String): Resource<StudySession> {
        return try {
            val response = api.cancelSession(sessionId)

            if (response.isSuccessful) {
                response.body()?.let { sessionDto ->
                    sessionDao.insertSession(with(StudySessionMapper) { sessionDto.toEntity() })
                    Resource.success(with(StudySessionMapper) { sessionDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Cancel session failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun refreshSessions(): Resource<List<StudySession>> {
        return try {
            val response = api.getSessions()

            if (response.isSuccessful) {
                response.body()?.let { sessionDtos ->
                    sessionDao.deleteAllSessions()
                    sessionDtos.forEach { sessionDto ->
                        sessionDao.insertSession(with(StudySessionMapper) { sessionDto.toEntity() })
                    }
                    Resource.success(sessionDtos.map { with(StudySessionMapper) { it.toDomain() } })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Refresh sessions failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override fun getUpcomingSessionsCount(): Flow<Int> {
        val today = LocalDate.now().format(dateFormatter)
        return sessionDao.getUpcomingSessionsCount(today)
    }

    override fun getTodayCompletedCount(): Flow<Int> {
        val today = LocalDate.now().format(dateFormatter)
        return sessionDao.getCompletedSessionsCountByDate(today)
    }
}

