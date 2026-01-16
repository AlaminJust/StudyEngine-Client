package com.gatishil.studyengine

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gatishil.studyengine.core.util.InAppUpdateManager
import com.gatishil.studyengine.data.local.datastore.AuthPreferences
import com.gatishil.studyengine.data.local.datastore.SettingsPreferences
import com.gatishil.studyengine.presentation.navigation.BottomNavItem
import com.gatishil.studyengine.presentation.navigation.Screen
import com.gatishil.studyengine.presentation.navigation.StudyEngineNavGraph
import com.gatishil.studyengine.service.FcmTokenManager
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    @Inject
    lateinit var authPreferences: AuthPreferences

    @Inject
    lateinit var inAppUpdateManager: InAppUpdateManager

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    companion object {
        // Track the last language that was applied to prevent recreation loops
        private var lastAppliedLanguage: String? = null
    }

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        inAppUpdateManager.handleUpdateResult(result.resultCode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Add splash screen exit animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create fade out animation
            val fadeOut = android.animation.ObjectAnimator.ofFloat(
                splashScreenView.view,
                android.view.View.ALPHA,
                1f,
                0f
            )
            fadeOut.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            fadeOut.duration = 300L

            // Create scale animation
            val scaleX = android.animation.ObjectAnimator.ofFloat(
                splashScreenView.iconView,
                android.view.View.SCALE_X,
                1f,
                1.2f,
                0f
            )
            val scaleY = android.animation.ObjectAnimator.ofFloat(
                splashScreenView.iconView,
                android.view.View.SCALE_Y,
                1f,
                1.2f,
                0f
            )
            scaleX.interpolator = android.view.animation.AccelerateInterpolator()
            scaleY.interpolator = android.view.animation.AccelerateInterpolator()
            scaleX.duration = 400L
            scaleY.duration = 400L

            // Play animations together
            val animatorSet = android.animation.AnimatorSet()
            animatorSet.playTogether(fadeOut, scaleX, scaleY)
            animatorSet.doOnEnd { splashScreenView.remove() }
            animatorSet.start()
        }

        // Note: We don't use enableEdgeToEdge() to have better control over status bar
        // Status bar colors are set in Theme.kt

        setContent {
            val themeMode by settingsPreferences.getThemeMode()
                .collectAsStateWithLifecycle(initialValue = SettingsPreferences.THEME_SYSTEM)

            val language by settingsPreferences.getLanguage()
                .collectAsStateWithLifecycle(initialValue = null)

            // Handle language change - only apply when it actually changes
            LaunchedEffect(language) {
                if (language == null) return@LaunchedEffect

                val localeCode = when (language) {
                    SettingsPreferences.LANGUAGE_BENGALI -> "bn"
                    else -> "en"
                }

                // Skip if we already applied this language (prevents recreation loop)
                if (lastAppliedLanguage == localeCode) {
                    return@LaunchedEffect
                }

                val currentLocales = AppCompatDelegate.getApplicationLocales()
                val currentLocale = if (currentLocales.isEmpty) {
                    resources.configuration.locales[0].language
                } else {
                    currentLocales.toLanguageTags().split("-", ",").firstOrNull() ?: "en"
                }

                // Only set locale if it's actually different
                if (currentLocale != localeCode) {
                    lastAppliedLanguage = localeCode
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(localeCode)
                    )
                } else {
                    // Update tracking to match current state
                    lastAppliedLanguage = localeCode
                }
            }

            val darkTheme = when (themeMode) {
                SettingsPreferences.THEME_DARK -> true
                SettingsPreferences.THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            // Check if user is logged in
            val isLoggedIn by authPreferences.isLoggedIn()
                .collectAsStateWithLifecycle(initialValue = null)

            // Observe update state
            val updateState by inAppUpdateManager.updateState.collectAsStateWithLifecycle()

            // Check for updates when app starts
            LaunchedEffect(Unit) {
                inAppUpdateManager.checkForUpdates(forceImmediate = true)
            }

            // Register device for push notifications when user is logged in
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn == true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        fcmTokenManager.registerDevice()
                    }
                }
            }

            // Handle update available
            LaunchedEffect(updateState) {
                when (updateState) {
                    is InAppUpdateManager.UpdateState.UpdateAvailable -> {
                        inAppUpdateManager.startUpdate(this@MainActivity)
                    }
                    is InAppUpdateManager.UpdateState.Downloaded -> {
                        // Show snackbar or prompt to complete update
                    }
                    else -> {}
                }
            }

            StudyEngineTheme(darkTheme = darkTheme) {
                // Show update required screen for immediate updates that user rejected
                val updateError = (updateState as? InAppUpdateManager.UpdateState.Error)
                val isForceUpdateRequired = updateError?.message == "Update is required to continue"

                when {
                    isForceUpdateRequired -> {
                        // Force update required screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.update_required),
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.update_required_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { inAppUpdateManager.checkForUpdates(forceImmediate = true) }
                                ) {
                                    Text(stringResource(R.string.update_now))
                                }
                            }
                        }
                    }
                    isLoggedIn == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {
                        StudyEngineApp(
                            isLoggedIn = isLoggedIn!!,
                            updateState = updateState,
                            onCompleteUpdate = { inAppUpdateManager.completeUpdate() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume any pending immediate updates
        inAppUpdateManager.resumeUpdateIfNeeded(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateManager.cleanup()
    }
}

@Composable
fun StudyEngineApp(
    isLoggedIn: Boolean,
    updateState: InAppUpdateManager.UpdateState = InAppUpdateManager.UpdateState.Idle,
    onCompleteUpdate: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when update is downloaded
    LaunchedEffect(updateState) {
        if (updateState is InAppUpdateManager.UpdateState.Downloaded) {
            val result = snackbarHostState.showSnackbar(
                message = "Update downloaded!",
                actionLabel = "Install",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onCompleteUpdate()
            }
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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