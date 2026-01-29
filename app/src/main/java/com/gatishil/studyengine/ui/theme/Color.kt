package com.gatishil.studyengine.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// DRACULA THEME COLOR PALETTE (for dark mode)
// ============================================
// Background:    #282A36 - Dark gray
// Current Line:  #44475A - Lighter gray (for cards/surfaces)
// Foreground:    #F8F8F2 - Off-white (primary text)
// Comment:       #6272A4 - Blue-gray (secondary text)
// Cyan:          #8BE9FD - Info, links
// Green:         #50FA7B - Success
// Orange:        #FFB86C - Warnings, highlights
// Pink:          #FF79C6 - Special features
// Purple:        #BD93F9 - Primary accent
// Red:           #FF5555 - Errors
// Yellow:        #F1FA8C - Notifications

// Dracula base colors
val DraculaBackground = Color(0xFF282A36)
val DraculaCurrentLine = Color(0xFF44475A)
val DraculaForeground = Color(0xFFF8F8F2)
val DraculaComment = Color(0xFF6272A4)
val DraculaCyan = Color(0xFF8BE9FD)
val DraculaGreen = Color(0xFF50FA7B)
val DraculaOrange = Color(0xFFFFB86C)
val DraculaPink = Color(0xFFFF79C6)
val DraculaPurple = Color(0xFFBD93F9)
val DraculaRed = Color(0xFFFF5555)
val DraculaYellow = Color(0xFFF1FA8C)

// ============================================
// PRIMARY COLORS
// ============================================
// Light mode: Deep Blue for study focus
val PrimaryLight = Color(0xFF1565C0)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD1E4FF)
val OnPrimaryContainerLight = Color(0xFF001D36)

// Dark mode: Dracula Purple as primary
val PrimaryDark = DraculaPurple
val OnPrimaryDark = Color(0xFF1E1E2E)
val PrimaryContainerDark = Color(0xFF3D3A50)
val OnPrimaryContainerDark = DraculaForeground

// ============================================
// SECONDARY COLORS
// ============================================
// Light mode: Teal for accents
val SecondaryLight = Color(0xFF00897B)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFB2DFDB)
val OnSecondaryContainerLight = Color(0xFF002021)

// Dark mode: Dracula Cyan as secondary
val SecondaryDark = DraculaCyan
val OnSecondaryDark = Color(0xFF1E2A2E)
val SecondaryContainerDark = Color(0xFF2D4A50)
val OnSecondaryContainerDark = DraculaForeground

// ============================================
// TERTIARY COLORS
// ============================================
// Light mode: Amber for highlights
val TertiaryLight = Color(0xFFFFA000)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFE0B2)
val OnTertiaryContainerLight = Color(0xFF2B1700)

// Dark mode: Dracula Orange as tertiary
val TertiaryDark = DraculaOrange
val OnTertiaryDark = Color(0xFF2E2A1E)
val TertiaryContainerDark = Color(0xFF4A3D2D)
val OnTertiaryContainerDark = DraculaForeground

// ============================================
// ERROR COLORS
// ============================================
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// Dark mode: Dracula Red for errors
val ErrorDark = DraculaRed
val OnErrorDark = Color(0xFF1E1E1E)
val ErrorContainerDark = Color(0xFF4A2D2D)
val OnErrorContainerDark = DraculaForeground

// ============================================
// BACKGROUND AND SURFACE COLORS
// ============================================
// Light mode
val BackgroundLight = Color(0xFFFDFBFF)
val OnBackgroundLight = Color(0xFF1A1C1E)
val SurfaceLight = Color(0xFFFDFBFF)
val OnSurfaceLight = Color(0xFF1A1C1E)
val SurfaceVariantLight = Color(0xFFE0E2EC)
val OnSurfaceVariantLight = Color(0xFF43474E)

// Dark mode: Dracula Background colors
val BackgroundDark = DraculaBackground
val OnBackgroundDark = DraculaForeground
val SurfaceDark = DraculaBackground
val OnSurfaceDark = DraculaForeground
val SurfaceVariantDark = DraculaCurrentLine
val OnSurfaceVariantDark = DraculaComment

// ============================================
// SURFACE CONTAINER COLORS (for layering)
// ============================================
// Light mode
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF7F8FA)
val SurfaceContainerLight = Color(0xFFF1F3F5)
val SurfaceContainerHighLight = Color(0xFFEBEDF0)
val SurfaceContainerHighestLight = Color(0xFFE5E7EB)

// Dark mode: Dracula layering
val SurfaceContainerLowestDark = Color(0xFF21222C)  // Slightly lighter than background
val SurfaceContainerLowDark = Color(0xFF282A36)     // Same as background
val SurfaceContainerDark = Color(0xFF2D2F3B)        // Between background and current line
val SurfaceContainerHighDark = DraculaCurrentLine   // Current line color
val SurfaceContainerHighestDark = Color(0xFF4D5066) // Slightly lighter than current line

// ============================================
// OUTLINE COLORS
// ============================================
val OutlineLight = Color(0xFF73777F)
val OutlineVariantLight = Color(0xFFC3C6CF)

val OutlineDark = DraculaComment
val OutlineVariantDark = Color(0xFF3D4050)

// ============================================
// STATUS COLORS (Extended)
// ============================================
val SuccessLight = Color(0xFF2E7D32)
val SuccessDark = DraculaGreen

val WarningLight = Color(0xFFE65100)
val WarningDark = DraculaOrange

val InfoLight = Color(0xFF0288D1)
val InfoDark = DraculaCyan

// ============================================
// SESSION STATUS COLORS
// ============================================
val SessionPlannedLight = Color(0xFF1565C0)
val SessionPlannedDark = DraculaPurple

val SessionInProgressLight = Color(0xFFFFA000)
val SessionInProgressDark = DraculaOrange

val SessionCompletedLight = Color(0xFF2E7D32)
val SessionCompletedDark = DraculaGreen

val SessionMissedLight = Color(0xFFD32F2F)
val SessionMissedDark = DraculaRed

val SessionCancelledLight = Color(0xFF616161)
val SessionCancelledDark = DraculaComment

// ============================================
// PRIORITY COLORS
// ============================================
val PriorityHighLight = Color(0xFFD32F2F)
val PriorityHighDark = DraculaRed

val PriorityMediumLight = Color(0xFFFFA000)
val PriorityMediumDark = DraculaOrange

val PriorityLowLight = Color(0xFF388E3C)
val PriorityLowDark = DraculaGreen

// ============================================
// STATUS BAR COLORS
// ============================================
val StatusBarLight = Color(0xFF1565C0)  // Primary blue
val StatusBarDark = DraculaBackground   // Dracula background

