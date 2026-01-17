package com.gatishil.studyengine.presentation.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    // Auth
    data object Auth : Screen("auth")
    data object Login : Screen("login")

    // Main
    data object Home : Screen("home")
    data object Dashboard : Screen("dashboard")

    // Books
    data object Books : Screen("books")
    data object BookDetail : Screen("books/{bookId}") {
        fun createRoute(bookId: String) = "books/$bookId"
    }
    data object AddBook : Screen("books/add")
    data object EditBook : Screen("books/{bookId}/edit") {
        fun createRoute(bookId: String) = "books/$bookId/edit"
    }
    data object CreateStudyPlan : Screen("books/{bookId}/create-plan") {
        fun createRoute(bookId: String) = "books/$bookId/create-plan"
    }
    data object AddChapter : Screen("books/{bookId}/add-chapter") {
        fun createRoute(bookId: String) = "books/$bookId/add-chapter"
    }

    // Sessions
    data object Sessions : Screen("sessions")
    data object SessionDetail : Screen("sessions/{sessionId}") {
        fun createRoute(sessionId: String) = "sessions/$sessionId"
    }
    data object TodaySessions : Screen("sessions/today")
    data object UpcomingSessions : Screen("sessions/upcoming")
    data object HowSessionsWork : Screen("sessions/how-it-works")

    // Schedule
    data object Schedule : Screen("schedule")
    data object Availability : Screen("schedule/availability")
    data object ScheduleOverrides : Screen("schedule/overrides")
    data object ScheduleContexts : Screen("schedule/contexts")

    // Study Plan
    data object StudyPlanDetail : Screen("study-plans/{studyPlanId}") {
        fun createRoute(studyPlanId: String) = "study-plans/$studyPlanId"
    }

    // Settings
    data object Settings : Screen("settings")
    data object Profile : Screen("settings/profile")
    data object Appearance : Screen("settings/appearance")
    data object Language : Screen("settings/language")
    data object Notifications : Screen("settings/notifications")
    data object About : Screen("settings/about")

    // Stats
    data object Stats : Screen("stats")

    // Academic
    data object Academic : Screen("academic")

    // Discover Profiles
    data object DiscoverProfiles : Screen("profiles/discover")
    data object PublicProfile : Screen("profiles/{userId}") {
        fun createRoute(userId: String) = "profiles/$userId"
    }

    // Reminders
    data object Reminders : Screen("reminders")

    // Exams
    data object Exams : Screen("exams")
    data object StartExam : Screen("exams/start/{subjectId}") {
        fun createRoute(subjectId: String) = "exams/start/$subjectId"
    }
    data object TakeExam : Screen("exams/take")
    data object ExamResult : Screen("exams/result/{examAttemptId}") {
        fun createRoute(examAttemptId: String) = "exams/result/$examAttemptId"
    }
    data object ExamHistory : Screen("exams/history")

    // Legal
    data object PrivacyPolicy : Screen("legal/privacy-policy")
    data object TermsOfService : Screen("legal/terms-of-service")
}

/**
 * Bottom navigation items
 */
enum class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val iconName: String
) {
    HOME("dashboard", com.gatishil.studyengine.R.string.nav_home, "home"),
    BOOKS("books", com.gatishil.studyengine.R.string.nav_books, "book"),
    SESSIONS("sessions/today", com.gatishil.studyengine.R.string.nav_sessions, "calendar_today"),
    EXAMS("exams", com.gatishil.studyengine.R.string.nav_exams, "quiz"),
    SETTINGS("settings", com.gatishil.studyengine.R.string.nav_settings, "settings")
}

