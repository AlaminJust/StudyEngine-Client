package com.gatishil.studyengine.data.remote.api

import com.gatishil.studyengine.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for StudyEngine backend
 */
interface StudyEngineApi {

    // ==================== Auth Endpoints ====================

    @POST("auth/google")
    suspend fun googleSignIn(
        @Body request: GoogleSignInRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/revoke")
    suspend fun revokeToken(
        @Body request: RefreshTokenRequestDto
    ): Response<MessageResponseDto>

    @POST("auth/revoke-all")
    suspend fun revokeAllTokens(): Response<MessageResponseDto>

    @GET("auth/me")
    suspend fun getAuthUser(): Response<UserDto>

    @GET("auth/validate")
    suspend fun validateToken(): Response<TokenValidationResponseDto>

    // ==================== User Endpoints ====================

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @PUT("users/me")
    suspend fun updateCurrentUser(
        @Body request: UpdateUserRequestDto
    ): Response<UserDto>

    @DELETE("users/me")
    suspend fun deleteCurrentUser(): Response<Unit>

    // ==================== User Availabilities ====================

    @GET("users/me/availabilities")
    suspend fun getAvailabilities(): Response<List<UserAvailabilityDto>>

    @GET("users/me/availabilities/{id}")
    suspend fun getAvailabilityById(
        @Path("id") id: String
    ): Response<UserAvailabilityDto>

    @POST("users/me/availabilities")
    suspend fun createAvailability(
        @Body request: CreateUserAvailabilityRequestDto
    ): Response<UserAvailabilityDto>

    @PUT("users/me/availabilities/{id}")
    suspend fun updateAvailability(
        @Path("id") id: String,
        @Body request: CreateUserAvailabilityRequestDto
    ): Response<UserAvailabilityDto>

    @DELETE("users/me/availabilities/{id}")
    suspend fun deleteAvailability(
        @Path("id") id: String
    ): Response<Unit>

    // ==================== Schedule Overrides ====================

    @GET("users/me/schedule-overrides")
    suspend fun getScheduleOverrides(): Response<List<ScheduleOverrideDto>>

    @GET("users/me/schedule-overrides/range")
    suspend fun getScheduleOverridesByDateRange(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<ScheduleOverrideDto>>

    @GET("users/me/schedule-overrides/{id}")
    suspend fun getScheduleOverrideById(
        @Path("id") id: String
    ): Response<ScheduleOverrideDto>

    @POST("users/me/schedule-overrides")
    suspend fun createScheduleOverride(
        @Body request: CreateScheduleOverrideRequestDto
    ): Response<ScheduleOverrideDto>

    @DELETE("users/me/schedule-overrides/{id}")
    suspend fun deleteScheduleOverride(
        @Path("id") id: String
    ): Response<Unit>

    // ==================== Schedule Contexts ====================

    @GET("users/me/schedule-contexts")
    suspend fun getScheduleContexts(): Response<List<ScheduleContextDto>>

    @GET("users/me/schedule-contexts/active")
    suspend fun getActiveScheduleContext(): Response<ScheduleContextDto>

    @GET("users/me/schedule-contexts/{id}")
    suspend fun getScheduleContextById(
        @Path("id") id: String
    ): Response<ScheduleContextDto>

    @POST("users/me/schedule-contexts")
    suspend fun createScheduleContext(
        @Body request: CreateScheduleContextRequestDto
    ): Response<ScheduleContextDto>

    @PATCH("users/me/schedule-contexts/{id}/load-multiplier")
    suspend fun updateScheduleContextLoadMultiplier(
        @Path("id") id: String,
        @Body request: UpdateLoadMultiplierRequestDto
    ): Response<ScheduleContextDto>

    @DELETE("users/me/schedule-contexts/{id}")
    suspend fun deleteScheduleContext(
        @Path("id") id: String
    ): Response<Unit>

    // ==================== Book Endpoints ====================

    @GET("books")
    suspend fun getBooks(): Response<List<BookDto>>

    @GET("books/{id}")
    suspend fun getBookById(
        @Path("id") bookId: String
    ): Response<BookDto>

    @POST("books")
    suspend fun createBook(
        @Body request: CreateBookRequestDto
    ): Response<BookDto>

    @PUT("books/{id}")
    suspend fun updateBook(
        @Path("id") bookId: String,
        @Body request: UpdateBookRequestDto
    ): Response<BookDto>

    @DELETE("books/{id}")
    suspend fun deleteBook(
        @Path("id") bookId: String
    ): Response<Unit>

    // ==================== Chapter Endpoints (via Books) ====================

    @GET("books/{bookId}/chapters")
    suspend fun getChaptersByBookId(
        @Path("bookId") bookId: String
    ): Response<List<ChapterDto>>

    @GET("books/{bookId}/chapters/{chapterId}")
    suspend fun getChapterByBookId(
        @Path("bookId") bookId: String,
        @Path("chapterId") chapterId: String
    ): Response<ChapterDto>

    @POST("books/{bookId}/chapters")
    suspend fun addChapter(
        @Path("bookId") bookId: String,
        @Body request: CreateChapterRequestDto
    ): Response<ChapterDto>

    @PUT("books/{bookId}/chapters/{chapterId}")
    suspend fun updateChapterByBookId(
        @Path("bookId") bookId: String,
        @Path("chapterId") chapterId: String,
        @Body request: UpdateChapterRequestDto
    ): Response<ChapterDto>

    @DELETE("books/{bookId}/chapters/{chapterId}")
    suspend fun deleteChapterByBookId(
        @Path("bookId") bookId: String,
        @Path("chapterId") chapterId: String
    ): Response<Unit>

    @POST("books/{bookId}/chapters/{chapterId}/ignore")
    suspend fun ignoreChapterByBookId(
        @Path("bookId") bookId: String,
        @Path("chapterId") chapterId: String,
        @Body request: IgnoreChapterRequestDto
    ): Response<ChapterDto>

    @POST("books/{bookId}/chapters/{chapterId}/unignore")
    suspend fun unignoreChapterByBookId(
        @Path("bookId") bookId: String,
        @Path("chapterId") chapterId: String
    ): Response<ChapterDto>

    // ==================== Chapter Endpoints (Standalone) ====================

    @GET("chapters/{id}")
    suspend fun getChapterById(
        @Path("id") chapterId: String
    ): Response<ChapterDto>

    @GET("chapters/book/{bookId}")
    suspend fun getChapters(
        @Path("bookId") bookId: String
    ): Response<List<ChapterDto>>

    @POST("chapters/book/{bookId}")
    suspend fun createChapter(
        @Path("bookId") bookId: String,
        @Body request: CreateChapterRequestDto
    ): Response<ChapterDto>

    @PUT("chapters/{id}")
    suspend fun updateChapter(
        @Path("id") chapterId: String,
        @Body request: UpdateChapterRequestDto
    ): Response<ChapterDto>

    @DELETE("chapters/{id}")
    suspend fun deleteChapter(
        @Path("id") chapterId: String
    ): Response<Unit>

    @POST("chapters/{id}/ignore")
    suspend fun ignoreChapter(
        @Path("id") chapterId: String,
        @Body request: IgnoreChapterRequestDto
    ): Response<ChapterDto>

    @POST("chapters/{id}/unignore")
    suspend fun unignoreChapter(
        @Path("id") chapterId: String
    ): Response<ChapterDto>

    // ==================== Study Plan Endpoints ====================

    @GET("study-plans/{id}")
    suspend fun getStudyPlanById(
        @Path("id") studyPlanId: String
    ): Response<StudyPlanDto>

    @GET("study-plans/book/{bookId}")
    suspend fun getStudyPlanByBookId(
        @Path("bookId") bookId: String
    ): Response<StudyPlanDto>

    @POST("study-plans/book/{bookId}")
    suspend fun createStudyPlan(
        @Path("bookId") bookId: String,
        @Body request: CreateStudyPlanRequestDto
    ): Response<StudyPlanDto>

    @PUT("study-plans/{id}")
    suspend fun updateStudyPlan(
        @Path("id") studyPlanId: String,
        @Body request: UpdateStudyPlanRequestDto
    ): Response<StudyPlanDto>

    @DELETE("study-plans/{id}")
    suspend fun deleteStudyPlan(
        @Path("id") studyPlanId: String
    ): Response<Unit>

    @POST("study-plans/{id}/activate")
    suspend fun activateStudyPlan(
        @Path("id") studyPlanId: String
    ): Response<StudyPlanDto>

    @POST("study-plans/{id}/pause")
    suspend fun pauseStudyPlan(
        @Path("id") studyPlanId: String
    ): Response<StudyPlanDto>

    @POST("study-plans/{id}/complete")
    suspend fun completeStudyPlan(
        @Path("id") studyPlanId: String
    ): Response<StudyPlanDto>

    // ==================== Session Endpoints ====================

    @GET("sessions")
    suspend fun getSessions(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<StudySessionDto>>

    @GET("sessions/{id}")
    suspend fun getSessionById(
        @Path("id") sessionId: String
    ): Response<StudySessionDto>

    @POST("sessions/{id}/start")
    suspend fun startSession(
        @Path("id") sessionId: String
    ): Response<StudySessionDto>

    @POST("sessions/{id}/complete")
    suspend fun completeSession(
        @Path("id") sessionId: String,
        @Body request: CompleteSessionRequestDto
    ): Response<StudySessionDto>

    @POST("sessions/{id}/miss")
    suspend fun markSessionAsMissed(
        @Path("id") sessionId: String
    ): Response<StudySessionDto>

    @POST("sessions/{id}/cancel")
    suspend fun cancelSession(
        @Path("id") sessionId: String
    ): Response<StudySessionDto>

    // ==================== Sync Endpoints ====================

    @POST("sync")
    suspend fun sync(
        @Body request: SyncRequestDto
    ): Response<SyncResponseDto>

    // ==================== Health Endpoints ====================

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponseDto>
}

