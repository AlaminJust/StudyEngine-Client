package com.gatishil.studyengine.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

/**
 * Extended colors for session status and priorities
 */
data class ExtendedColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val sessionPlanned: Color,
    val sessionInProgress: Color,
    val sessionCompleted: Color,
    val sessionMissed: Color,
    val sessionCancelled: Color,
    val priorityHigh: Color,
    val priorityMedium: Color,
    val priorityLow: Color
)

val LocalExtendedColors = compositionLocalOf {
    ExtendedColors(
        success = SuccessLight,
        warning = WarningLight,
        info = InfoLight,
        sessionPlanned = SessionPlannedLight,
        sessionInProgress = SessionInProgressLight,
        sessionCompleted = SessionCompletedLight,
        sessionMissed = SessionMissedLight,
        sessionCancelled = SessionCancelledLight,
        priorityHigh = PriorityHighLight,
        priorityMedium = PriorityMediumLight,
        priorityLow = PriorityLowLight
    )
}

private val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    warning = WarningLight,
    info = InfoLight,
    sessionPlanned = SessionPlannedLight,
    sessionInProgress = SessionInProgressLight,
    sessionCompleted = SessionCompletedLight,
    sessionMissed = SessionMissedLight,
    sessionCancelled = SessionCancelledLight,
    priorityHigh = PriorityHighLight,
    priorityMedium = PriorityMediumLight,
    priorityLow = PriorityLowLight
)

private val DarkExtendedColors = ExtendedColors(
    success = SuccessDark,
    warning = WarningDark,
    info = InfoDark,
    sessionPlanned = SessionPlannedDark,
    sessionInProgress = SessionInProgressDark,
    sessionCompleted = SessionCompletedDark,
    sessionMissed = SessionMissedDark,
    sessionCancelled = SessionCancelledDark,
    priorityHigh = PriorityHighDark,
    priorityMedium = PriorityMediumDark,
    priorityLow = PriorityLowDark
)

@Composable
fun StudyEngineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Object to access extended colors from MaterialTheme
 */
object StudyEngineTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
