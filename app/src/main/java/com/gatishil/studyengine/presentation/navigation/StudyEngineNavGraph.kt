package com.gatishil.studyengine.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gatishil.studyengine.presentation.screens.auth.LoginScreen
import com.gatishil.studyengine.presentation.screens.books.AddBookScreen
import com.gatishil.studyengine.presentation.screens.books.AddChapterScreen
import com.gatishil.studyengine.presentation.screens.books.BookDetailScreen
import com.gatishil.studyengine.presentation.screens.books.BooksScreen
import com.gatishil.studyengine.presentation.screens.books.CreateStudyPlanScreen
import com.gatishil.studyengine.presentation.screens.dashboard.DashboardScreen
import com.gatishil.studyengine.presentation.screens.sessions.SessionDetailScreen
import com.gatishil.studyengine.presentation.screens.sessions.TodaySessionsScreen
import com.gatishil.studyengine.presentation.screens.settings.SettingsScreen

private const val ANIMATION_DURATION = 300

@Composable
fun StudyEngineNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(ANIMATION_DURATION)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIMATION_DURATION)) +
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(ANIMATION_DURATION)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(ANIMATION_DURATION)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(ANIMATION_DURATION)) +
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(ANIMATION_DURATION)
            )
        }
    ) {
        // Auth
        composable(route = Screen.Login.route) {
            LoginScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToBooks = { navController.navigate(Screen.Books.route) },
                onNavigateToBook = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onNavigateToSession = { sessionId ->
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                },
                onNavigateToSessions = { navController.navigate(Screen.TodaySessions.route) },
                onNavigateToUpcomingSessions = { navController.navigate(Screen.UpcomingSessions.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToAddBook = { navController.navigate(Screen.AddBook.route) }
            )
        }

        // Stats
        composable(route = Screen.Stats.route) {
            com.gatishil.studyengine.presentation.screens.stats.StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Books
        composable(route = Screen.Books.route) {
            BooksScreen(
                onNavigateToBook = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onNavigateToAddBook = { navController.navigate(Screen.AddBook.route) }
            )
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            BookDetailScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreatePlan = {
                    navController.navigate(Screen.CreateStudyPlan.createRoute(bookId))
                },
                onNavigateToAddChapter = {
                    navController.navigate(Screen.AddChapter.createRoute(bookId))
                }
            )
        }

        composable(route = Screen.AddBook.route) {
            AddBookScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookCreated = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId)) {
                        popUpTo(Screen.AddBook.route) { inclusive = true }
                    }
                }
            )
        }

        // Create Study Plan
        composable(
            route = Screen.CreateStudyPlan.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            CreateStudyPlanScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() },
                onPlanCreated = { navController.popBackStack() }
            )
        }

        // Add Chapter
        composable(
            route = Screen.AddChapter.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            AddChapterScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() },
                onChapterAdded = { navController.popBackStack() }
            )
        }

        // Sessions
        composable(route = Screen.TodaySessions.route) {
            TodaySessionsScreen(
                onNavigateToSession = { sessionId ->
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                },
                onNavigateToHowItWorks = {
                    navController.navigate(Screen.HowSessionsWork.route)
                },
                onNavigateToUpcoming = {
                    navController.navigate(Screen.UpcomingSessions.route)
                }
            )
        }

        composable(route = Screen.UpcomingSessions.route) {
            com.gatishil.studyengine.presentation.screens.sessions.UpcomingSessionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSession = { sessionId ->
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                }
            )
        }

        composable(route = Screen.HowSessionsWork.route) {
            com.gatishil.studyengine.presentation.screens.sessions.HowSessionsWorkScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SessionDetail.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            SessionDetailScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Schedule
        composable(route = Screen.Availability.route) {
            com.gatishil.studyengine.presentation.screens.schedule.AvailabilityScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ScheduleOverrides.route) {
            com.gatishil.studyengine.presentation.screens.schedule.ScheduleOverridesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ScheduleContexts.route) {
            com.gatishil.studyengine.presentation.screens.schedule.ScheduleContextsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) },
                onNavigateToLanguage = { navController.navigate(Screen.Language.route) },
                onNavigateToAvailability = { navController.navigate(Screen.Availability.route) },
                onNavigateToScheduleOverrides = { navController.navigate(Screen.ScheduleOverrides.route) },
                onNavigateToScheduleContexts = { navController.navigate(Screen.ScheduleContexts.route) },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Profile
        composable(route = Screen.Profile.route) {
            com.gatishil.studyengine.presentation.screens.profile.ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

