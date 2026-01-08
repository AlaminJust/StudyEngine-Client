package com.gatishil.studyengine.data.local.dao

import androidx.room.*
import com.gatishil.studyengine.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for User operations
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

/**
 * DAO for Book operations
 */
@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY priority DESC, title ASC")
    fun getBooksByUserId(userId: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY priority DESC, title ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): BookEntity?

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookByIdFlow(bookId: String): Flow<BookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Query("SELECT * FROM books WHERE syncStatus != 0")
    suspend fun getPendingSyncBooks(): List<BookEntity>
}

/**
 * DAO for Chapter operations
 */
@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY orderIndex ASC")
    fun getChaptersByBookId(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBookId(bookId: String)

    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()
}

/**
 * DAO for Study Plan operations
 */
@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans WHERE bookId = :bookId")
    suspend fun getStudyPlanByBookId(bookId: String): StudyPlanEntity?

    @Query("SELECT * FROM study_plans WHERE bookId = :bookId")
    fun getStudyPlanByBookIdFlow(bookId: String): Flow<StudyPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(studyPlan: StudyPlanEntity)

    @Delete
    suspend fun deleteStudyPlan(studyPlan: StudyPlanEntity)

    @Query("DELETE FROM study_plans WHERE bookId = :bookId")
    suspend fun deleteStudyPlanByBookId(bookId: String)

    @Query("DELETE FROM study_plans")
    suspend fun deleteAllStudyPlans()
}

/**
 * DAO for Recurrence Rule operations
 */
@Dao
interface RecurrenceRuleDao {
    @Query("SELECT * FROM recurrence_rules WHERE studyPlanId = :studyPlanId")
    suspend fun getRecurrenceRuleByStudyPlanId(studyPlanId: String): RecurrenceRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurrenceRule(rule: RecurrenceRuleEntity)

    @Delete
    suspend fun deleteRecurrenceRule(rule: RecurrenceRuleEntity)

    @Query("DELETE FROM recurrence_rules WHERE studyPlanId = :studyPlanId")
    suspend fun deleteRecurrenceRuleByStudyPlanId(studyPlanId: String)

    @Query("DELETE FROM recurrence_rules")
    suspend fun deleteAllRecurrenceRules()
}

/**
 * DAO for Study Session operations
 */
@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY sessionDate DESC, startTime DESC")
    fun getSessionsByUserId(userId: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions ORDER BY sessionDate DESC, startTime DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE sessionDate = :date ORDER BY startTime ASC")
    fun getSessionsByDate(date: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE sessionDate BETWEEN :startDate AND :endDate ORDER BY sessionDate ASC, startTime ASC")
    fun getSessionsByDateRange(startDate: String, endDate: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): StudySessionEntity?

    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    fun getSessionByIdFlow(sessionId: String): Flow<StudySessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<StudySessionEntity>)

    @Update
    suspend fun updateSession(session: StudySessionEntity)

    @Delete
    suspend fun deleteSession(session: StudySessionEntity)

    @Query("DELETE FROM study_sessions WHERE bookId = :bookId")
    suspend fun deleteSessionsByBookId(bookId: String)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT * FROM study_sessions WHERE syncStatus != 0")
    suspend fun getPendingSyncSessions(): List<StudySessionEntity>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE sessionDate >= :date AND status = 'Planned'")
    fun getUpcomingSessionsCount(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE sessionDate = :date AND status = 'Completed'")
    fun getCompletedSessionsCountByDate(date: String): Flow<Int>
}

/**
 * DAO for User Availability operations
 */
@Dao
interface UserAvailabilityDao {
    @Query("SELECT * FROM user_availabilities WHERE userId = :userId ORDER BY dayOfWeek, startTime")
    fun getAvailabilitiesByUserId(userId: String): Flow<List<UserAvailabilityEntity>>

    @Query("SELECT * FROM user_availabilities ORDER BY dayOfWeek, startTime")
    fun getAllAvailabilities(): Flow<List<UserAvailabilityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailability(availability: UserAvailabilityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailabilities(availabilities: List<UserAvailabilityEntity>)

    @Delete
    suspend fun deleteAvailability(availability: UserAvailabilityEntity)

    @Query("DELETE FROM user_availabilities WHERE id = :availabilityId")
    suspend fun deleteAvailabilityById(availabilityId: String)

    @Query("DELETE FROM user_availabilities")
    suspend fun deleteAllAvailabilities()
}

/**
 * DAO for Schedule Override operations
 */
@Dao
interface ScheduleOverrideDao {
    @Query("SELECT * FROM schedule_overrides WHERE userId = :userId ORDER BY overrideDate")
    fun getOverridesByUserId(userId: String): Flow<List<ScheduleOverrideEntity>>

    @Query("SELECT * FROM schedule_overrides ORDER BY overrideDate")
    fun getAllOverrides(): Flow<List<ScheduleOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: ScheduleOverrideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverrides(overrides: List<ScheduleOverrideEntity>)

    @Delete
    suspend fun deleteOverride(override: ScheduleOverrideEntity)

    @Query("DELETE FROM schedule_overrides")
    suspend fun deleteAllOverrides()
}

/**
 * DAO for Schedule Context operations
 */
@Dao
interface ScheduleContextDao {
    @Query("SELECT * FROM schedule_contexts WHERE userId = :userId ORDER BY startDate")
    fun getContextsByUserId(userId: String): Flow<List<ScheduleContextEntity>>

    @Query("SELECT * FROM schedule_contexts ORDER BY startDate")
    fun getAllContexts(): Flow<List<ScheduleContextEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: ScheduleContextEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContexts(contexts: List<ScheduleContextEntity>)

    @Delete
    suspend fun deleteContext(context: ScheduleContextEntity)

    @Query("DELETE FROM schedule_contexts")
    suspend fun deleteAllContexts()
}

