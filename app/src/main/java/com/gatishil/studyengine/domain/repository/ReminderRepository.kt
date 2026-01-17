package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.CustomReminder
import com.gatishil.studyengine.domain.model.RemindersList
import java.time.LocalDateTime

/**
 * Repository interface for reminder operations
 */
interface ReminderRepository {

    /**
     * Create a new custom reminder
     */
    suspend fun createReminder(
        title: String,
        message: String,
        scheduledFor: LocalDateTime
    ): Resource<CustomReminder>

    /**
     * Get a reminder by ID
     */
    suspend fun getReminderById(id: String): Resource<CustomReminder>

    /**
     * Get upcoming reminders
     */
    suspend fun getUpcomingReminders(limit: Int = 20): Resource<RemindersList>

    /**
     * Get all reminders
     */
    suspend fun getAllReminders(limit: Int = 50): Resource<RemindersList>

    /**
     * Update a reminder
     */
    suspend fun updateReminder(
        id: String,
        title: String?,
        message: String?,
        scheduledFor: LocalDateTime?
    ): Resource<CustomReminder>

    /**
     * Delete a reminder
     */
    suspend fun deleteReminder(id: String): Resource<Boolean>
}

