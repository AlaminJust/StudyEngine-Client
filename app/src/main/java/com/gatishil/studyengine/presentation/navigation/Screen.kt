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
    SETTINGS("settings", com.gatishil.studyengine.R.string.nav_settings, "settings")
}

