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
// Light mode: Modern blue for study focus
val PrimaryLight = Color(0xFF3B82F6)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFDBEAFE)
val OnPrimaryContainerLight = Color(0xFF1E3A5F)

// Dark mode: Dracula Purple as primary
val PrimaryDark = DraculaPurple
val OnPrimaryDark = Color(0xFF1E1E2E)
val PrimaryContainerDark = Color(0xFF3D3A50)
val OnPrimaryContainerDark = DraculaForeground

// ============================================
// SECONDARY COLORS
// ============================================
// Light mode: Violet for harmonious accents
val SecondaryLight = Color(0xFF8B5CF6)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFEDE9FE)
val OnSecondaryContainerLight = Color(0xFF2E1065)

// Dark mode: Dracula Cyan as secondary
val SecondaryDark = DraculaCyan
val OnSecondaryDark = Color(0xFF1E2A2E)
val SecondaryContainerDark = Color(0xFF2D4A50)
val OnSecondaryContainerDark = DraculaForeground

// ============================================
// TERTIARY COLORS
// ============================================
// Light mode: Cyan for cool accent (creates smooth blue→cyan gradients)
val TertiaryLight = Color(0xFF0891B2)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFCFFAFE)
val OnTertiaryContainerLight = Color(0xFF164E63)

// Dark mode: Dracula Orange as tertiary
val TertiaryDark = DraculaOrange
val OnTertiaryDark = Color(0xFF2E2A1E)
val TertiaryContainerDark = Color(0xFF4A3D2D)
val OnTertiaryContainerDark = DraculaForeground

// ============================================
// ERROR COLORS
// ============================================
val ErrorLight = Color(0xFFDC2626)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF450A0A)

// Dark mode: Dracula Red for errors
val ErrorDark = DraculaRed
val OnErrorDark = Color(0xFF1E1E1E)
val ErrorContainerDark = Color(0xFF4A2D2D)
val OnErrorContainerDark = DraculaForeground

// ============================================
// BACKGROUND AND SURFACE COLORS
// ============================================
// Light mode - subtle cool gray tint
val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF475569)

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
// Light mode - smooth gradient of slate grays
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF8FAFC)
val SurfaceContainerLight = Color(0xFFF1F5F9)
val SurfaceContainerHighLight = Color(0xFFE2E8F0)
val SurfaceContainerHighestLight = Color(0xFFCBD5E1)

// Dark mode: Dracula layering
val SurfaceContainerLowestDark = Color(0xFF21222C)  // Slightly lighter than background
val SurfaceContainerLowDark = Color(0xFF282A36)     // Same as background
val SurfaceContainerDark = Color(0xFF2D2F3B)        // Between background and current line
val SurfaceContainerHighDark = DraculaCurrentLine   // Current line color
val SurfaceContainerHighestDark = Color(0xFF4D5066) // Slightly lighter than current line

// ============================================
// OUTLINE COLORS
// ============================================
val OutlineLight = Color(0xFF94A3B8)
val OutlineVariantLight = Color(0xFFE2E8F0)

val OutlineDark = DraculaComment
val OutlineVariantDark = Color(0xFF3D4050)

// ============================================
// STATUS COLORS (Extended)
// ============================================
val SuccessLight = Color(0xFF16A34A)
val SuccessDark = DraculaGreen

val WarningLight = Color(0xFFEA580C)
val WarningDark = DraculaOrange

val InfoLight = Color(0xFF0284C7)
val InfoDark = DraculaCyan

// ============================================
// SESSION STATUS COLORS
// ============================================
val SessionPlannedLight = Color(0xFF3B82F6)
val SessionPlannedDark = DraculaPurple

val SessionInProgressLight = Color(0xFFF59E0B)
val SessionInProgressDark = DraculaOrange

val SessionCompletedLight = Color(0xFF16A34A)
val SessionCompletedDark = DraculaGreen

val SessionMissedLight = Color(0xFFDC2626)
val SessionMissedDark = DraculaRed

val SessionCancelledLight = Color(0xFF64748B)
val SessionCancelledDark = DraculaComment

// ============================================
// PRIORITY COLORS
// ============================================
val PriorityHighLight = Color(0xFFDC2626)
val PriorityHighDark = DraculaRed

val PriorityMediumLight = Color(0xFFF59E0B)
val PriorityMediumDark = DraculaOrange

val PriorityLowLight = Color(0xFF16A34A)
val PriorityLowDark = DraculaGreen

// ============================================
// STATUS BAR COLORS
// ============================================
val StatusBarLight = Color(0xFFF8FAFC)  // Matches background
val StatusBarDark = DraculaBackground   // Dracula background
