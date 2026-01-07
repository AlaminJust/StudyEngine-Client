package com.example.studyengine.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Extension functions for date and time formatting with localization support
 */
object DateTimeUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private val time24Formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun LocalDate.toFormattedString(): String = format(dateFormatter)

    fun LocalDate.toShortString(): String = format(shortDateFormatter)

    fun LocalTime.toFormattedString(): String = format(timeFormatter)

    fun LocalTime.to24HourString(): String = format(time24Formatter)

    fun DayOfWeek.toLocalizedName(locale: Locale = Locale.getDefault()): String {
        return getDisplayName(java.time.format.TextStyle.FULL, locale)
    }

    fun DayOfWeek.toShortName(locale: Locale = Locale.getDefault()): String {
        return getDisplayName(java.time.format.TextStyle.SHORT, locale)
    }
}

/**
 * Extension function to convert minutes to formatted duration string
 */
fun Int.toFormattedDuration(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

