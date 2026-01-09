package com.gatishil.studyengine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gatishil.studyengine.data.local.datastore.AuthPreferences
import com.gatishil.studyengine.data.local.datastore.SettingsPreferences
import com.gatishil.studyengine.presentation.navigation.BottomNavItem
import com.gatishil.studyengine.presentation.navigation.Screen
import com.gatishil.studyengine.presentation.navigation.StudyEngineNavGraph
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    @Inject
    lateinit var authPreferences: AuthPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by settingsPreferences.getThemeMode()
                .collectAsStateWithLifecycle(initialValue = SettingsPreferences.THEME_SYSTEM)

            val language by settingsPreferences.getLanguage()
                .collectAsStateWithLifecycle(initialValue = SettingsPreferences.LANGUAGE_ENGLISH)

            // Apply language change using AppCompatDelegate
            LaunchedEffect(language) {
                val localeCode = when (language) {
                    SettingsPreferences.LANGUAGE_BENGALI -> "bn"
                    else -> "en"
                }
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(localeCode)
                )
            }

            val darkTheme = when (themeMode) {
                SettingsPreferences.THEME_DARK -> true
                SettingsPreferences.THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            // Check if user is logged in
            val isLoggedIn by authPreferences.isLoggedIn()
                .collectAsStateWithLifecycle(initialValue = null)

            StudyEngineTheme(darkTheme = darkTheme) {
                // Show loading until we know login state
                if (isLoggedIn == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    StudyEngineApp(isLoggedIn = isLoggedIn!!)
                }
            }
        }
    }
}

@Composable
fun StudyEngineApp(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if we should show bottom navigation
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Dashboard.route,
        Screen.Books.route,
        Screen.TodaySessions.route,
        Screen.Settings.route
    )

    // Start destination based on login state
    val startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItem.entries.forEach { navItem ->
                        // Simple route matching - check if current route matches nav item route
                        val selected = currentDestination?.route == navItem.route

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = when (navItem) {
                                        BottomNavItem.HOME -> Icons.Default.Home
                                        BottomNavItem.BOOKS -> Icons.AutoMirrored.Filled.MenuBook
                                        BottomNavItem.SESSIONS -> Icons.Default.CalendarToday
                                        BottomNavItem.SETTINGS -> Icons.Default.Settings
                                    },
                                    contentDescription = stringResource(navItem.titleResId)
                                )
                            },
                            label = { Text(stringResource(navItem.titleResId)) },
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(navItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        StudyEngineNavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}