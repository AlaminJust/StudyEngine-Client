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

    /**
     * Bulk update all user availabilities.
     * This replaces all existing availabilities with the provided list.
     */
    @PUT("users/me/availabilities")
    suspend fun bulkUpdateAvailabilities(
        @Body request: BulkUpdateUserAvailabilityRequestDto
    ): Response<List<UserAvailabilityDto>>

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

    // ==================== Stats Endpoints ====================

    @GET("stats")
    suspend fun getStats(): Response<StatsDto>

    @GET("stats/quick")
    suspend fun getQuickStats(): Response<QuickStatsDto>

    @GET("stats/history")
    suspend fun getStudyHistory(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<StudyHistoryDto>

    @GET("stats/calendar/{year}/{month}")
    suspend fun getCalendarMonth(
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Response<CalendarMonthDto>

    @GET("stats/achievements")
    suspend fun getAchievements(): Response<List<AchievementDto>>

    @POST("stats/recalculate")
    suspend fun recalculateStats(): Response<StatsDto>

    // ==================== Sync Endpoints ====================

    @POST("sync")
    suspend fun sync(
        @Body request: SyncRequestDto
    ): Response<SyncResponseDto>

    // ==================== Health Endpoints ====================

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponseDto>

    // ==================== Profile Endpoints ====================

    @GET("profile")
    suspend fun getProfile(): Response<UserProfileDto>

    @PUT("profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequestDto
    ): Response<UserProfileDto>

    @GET("profile/public/{userId}")
    suspend fun getPublicProfile(
        @Path("userId") userId: String
    ): Response<PublicProfileDto>

    @GET("profile/discover")
    suspend fun discoverProfiles(
        @Query("searchTerm") searchTerm: String? = null,
        @Query("role") role: String? = null,
        @Query("academicLevel") academicLevel: String? = null,
        @Query("institutionType") institutionType: String? = null,
        @Query("institutionCountry") institutionCountry: String? = null,
        @Query("major") major: String? = null,
        @Query("department") department: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<PublicProfilesPagedResponseDto>

    @GET("profile/preferences")
    suspend fun getPreferences(): Response<UserPreferencesDto>

    @PUT("profile/preferences/study-goals")
    suspend fun updateStudyGoals(
        @Body request: UpdateStudyGoalsRequestDto
    ): Response<UserPreferencesDto>

    @PUT("profile/preferences/reading-speed")
    suspend fun updateReadingSpeed(
        @Body request: UpdateReadingSpeedRequestDto
    ): Response<UserPreferencesDto>

    @PUT("profile/preferences/session")
    suspend fun updateSessionPreferences(
        @Body request: UpdateSessionPreferencesRequestDto
    ): Response<UserPreferencesDto>

    @PUT("profile/preferences/notifications")
    suspend fun updateNotificationPreferences(
        @Body request: UpdateNotificationPreferencesRequestDto
    ): Response<UserPreferencesDto>

    @PUT("profile/preferences/ui")
    suspend fun updateUIPreferences(
        @Body request: UpdateUIPreferencesRequestDto
    ): Response<UserPreferencesDto>

    @PUT("profile/preferences/privacy")
    suspend fun updatePrivacySettings(
        @Body request: UpdatePrivacySettingsRequestDto
    ): Response<UserPreferencesDto>

    @POST("profile/deactivate")
    suspend fun deactivateAccount(): Response<MessageResponseDto>

    @POST("profile/reactivate")
    suspend fun reactivateAccount(): Response<MessageResponseDto>

    @HTTP(method = "DELETE", path = "profile", hasBody = true)
    suspend fun deleteAccount(
        @Body request: DeleteAccountRequestDto
    ): Response<MessageResponseDto>

    // ==================== Academic Profile Endpoints ====================

    @GET("profile/academic")
    suspend fun getAcademicProfile(): Response<UserAcademicProfileDto>

    @PUT("profile/academic/basic")
    suspend fun updateAcademicBasicInfo(
        @Body request: UpdateAcademicBasicInfoRequestDto
    ): Response<UserAcademicProfileDto>

    @PUT("profile/academic/student")
    suspend fun updateStudentInfo(
        @Body request: UpdateStudentInfoRequestDto
    ): Response<UserAcademicProfileDto>

    @PUT("profile/academic/institution")
    suspend fun updateInstitutionInfo(
        @Body request: UpdateInstitutionInfoRequestDto
    ): Response<UserAcademicProfileDto>

    @PUT("profile/academic/teaching")
    suspend fun updateTeachingInfo(
        @Body request: UpdateTeachingInfoRequestDto
    ): Response<UserAcademicProfileDto>

    @PUT("profile/academic/social")
    suspend fun updateSocialLinks(
        @Body request: UpdateSocialLinksRequestDto
    ): Response<UserAcademicProfileDto>

    // ==================== Notification Endpoints ====================

    @POST("notifications/devices/register")
    suspend fun registerDevice(
        @Body request: RegisterDeviceTokenRequestDto
    ): Response<DeviceRegistrationResponseDto>

    @POST("notifications/devices/unregister")
    suspend fun unregisterDevice(
        @Body request: UnregisterDeviceTokenRequestDto
    ): Response<MessageResponseDto>

    @GET("notifications/devices")
    suspend fun getDevices(): Response<List<DeviceTokenDto>>

    @POST("notifications/devices/deactivate-all")
    suspend fun deactivateAllDevices(): Response<MessageResponseDto>

    @GET("notifications/settings")
    suspend fun getNotificationSettings(): Response<NotificationSettingsDto>

    @GET("notifications/scheduled")
    suspend fun getScheduledNotifications(): Response<ScheduledNotificationsResponseDto>

    @DELETE("notifications/scheduled/{notificationId}")
    suspend fun cancelScheduledNotification(
        @Path("notificationId") notificationId: String
    ): Response<MessageResponseDto>

    @POST("notifications/test")
    suspend fun sendTestNotification(
        @Body request: SendTestNotificationRequestDto
    ): Response<TestNotificationResponseDto>

    // ==================== Custom Reminders Endpoints ====================

    @POST("reminders")
    suspend fun createReminder(
        @Body request: CreateCustomReminderRequestDto
    ): Response<CustomReminderResponseDto>

    @GET("reminders/{id}")
    suspend fun getReminderById(
        @Path("id") id: String
    ): Response<CustomReminderResponseDto>

    @GET("reminders/upcoming")
    suspend fun getUpcomingReminders(
        @Query("limit") limit: Int = 20
    ): Response<UpcomingRemindersResponseDto>

    @GET("reminders")
    suspend fun getAllReminders(
        @Query("limit") limit: Int = 50
    ): Response<UpcomingRemindersResponseDto>

    @PUT("reminders/{id}")
    suspend fun updateReminder(
        @Path("id") id: String,
        @Body request: UpdateCustomReminderRequestDto
    ): Response<CustomReminderResponseDto>

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(
        @Path("id") id: String
    ): Response<Unit>

    // ==================== Category Endpoints ====================

    @GET("categories")
    suspend fun getCategories(
        @Query("includeInactive") includeInactive: Boolean = false
    ): Response<List<CategoryListDto>>

    @GET("categories/with-subjects")
    suspend fun getCategoriesWithSubjects(
        @Query("includeInactive") includeInactive: Boolean = false
    ): Response<List<CategoryWithSubjectsDto>>

    @GET("categories/{id}")
    suspend fun getCategoryById(
        @Path("id") id: String
    ): Response<CategoryDto>

    @GET("categories/{id}/with-subjects")
    suspend fun getCategoryWithSubjects(
        @Path("id") id: String
    ): Response<CategoryWithSubjectsDto>

    // ==================== Subject Endpoints ====================

    @GET("subjects")
    suspend fun getSubjects(
        @Query("includeInactive") includeInactive: Boolean = false
    ): Response<List<SubjectListDto>>

    @GET("subjects/by-category/{categoryId}")
    suspend fun getSubjectsByCategory(
        @Path("categoryId") categoryId: String,
        @Query("includeInactive") includeInactive: Boolean = false
    ): Response<List<SubjectListDto>>

    @GET("subjects/{id}")
    suspend fun getSubjectById(
        @Path("id") id: String
    ): Response<SubjectDto>

    @GET("subjects/{id}/with-chapters")
    suspend fun getSubjectWithChapters(
        @Path("id") id: String
    ): Response<SubjectWithChaptersDto>

    // ==================== Subject Chapter Endpoints ====================

    @GET("subject-chapters/by-subject/{subjectId}")
    suspend fun getSubjectChapters(
        @Path("subjectId") subjectId: String,
        @Query("includeInactive") includeInactive: Boolean = false
    ): Response<List<SubjectChapterListDto>>

    @GET("subject-chapters/{id}")
    suspend fun getSubjectChapterById(
        @Path("id") id: String
    ): Response<SubjectChapterDto>

    // ==================== Question Endpoints ====================

    @GET("questions/count")
    suspend fun getAvailableQuestionCount(
        @Query("subjectId") subjectId: String,
        @Query("difficulty") difficulty: String? = null
    ): Response<AvailableQuestionCountDto>

    // ==================== Exam Endpoints ====================

    @POST("exams/start")
    suspend fun startExam(
        @Body request: StartExamRequestDto
    ): Response<ExamQuestionSetDto>

    @GET("exams/current")
    suspend fun getCurrentExam(): Response<ExamQuestionSetDto?>

    @POST("exams/submit")
    suspend fun submitExam(
        @Body request: SubmitExamRequestDto
    ): Response<ExamResultDto>

    @GET("exams/{examAttemptId}/result")
    suspend fun getExamResult(
        @Path("examAttemptId") examAttemptId: String
    ): Response<ExamResultDto>

    @GET("exams/history")
    suspend fun getExamHistory(
        @Query("subjectId") subjectId: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ExamAttemptPagedResponseDto>

    @POST("exams/{examAttemptId}/cancel")
    suspend fun cancelExam(
        @Path("examAttemptId") examAttemptId: String
    ): Response<Unit>
}

