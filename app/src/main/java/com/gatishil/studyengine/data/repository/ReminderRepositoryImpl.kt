package com.gatishil.studyengine.data.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.mapper.ReminderMapper
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.CreateCustomReminderRequestDto
import com.gatishil.studyengine.data.remote.dto.UpdateCustomReminderRequestDto
import com.gatishil.studyengine.domain.model.CustomReminder
import com.gatishil.studyengine.domain.model.RemindersList
import com.gatishil.studyengine.domain.repository.ReminderRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi
) : ReminderRepository {

    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    override suspend fun createReminder(
        title: String,
        message: String,
        scheduledFor: LocalDateTime
    ): Resource<CustomReminder> {
        return try {
            val response = api.createReminder(
                CreateCustomReminderRequestDto(
                    title = title,
                    message = message,
                    scheduledFor = scheduledFor.format(dateTimeFormatter)
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ReminderMapper.toDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to create reminder: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun getReminderById(id: String): Resource<CustomReminder> {
        return try {
            val response = api.getReminderById(id)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ReminderMapper.toDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else if (response.code() == 404) {
                Resource.error(
                    Exception("Reminder not found"),
                    "Reminder not found"
                )
            } else {
                Resource.error(
                    Exception("Failed to get reminder: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun getUpcomingReminders(limit: Int): Resource<RemindersList> {
        return try {
            val response = api.getUpcomingReminders(limit)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ReminderMapper.toDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to get upcoming reminders: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun getAllReminders(limit: Int): Resource<RemindersList> {
        return try {
            val response = api.getAllReminders(limit)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ReminderMapper.toDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to get reminders: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateReminder(
        id: String,
        title: String?,
        message: String?,
        scheduledFor: LocalDateTime?
    ): Resource<CustomReminder> {
        return try {
            val response = api.updateReminder(
                id = id,
                request = UpdateCustomReminderRequestDto(
                    title = title,
                    message = message,
                    scheduledFor = scheduledFor?.format(dateTimeFormatter)
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ReminderMapper.toDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else if (response.code() == 404) {
                Resource.error(
                    Exception("Reminder not found or cannot be updated"),
                    "Reminder not found or cannot be updated"
                )
            } else {
                Resource.error(
                    Exception("Failed to update reminder: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun deleteReminder(id: String): Resource<Boolean> {
        return try {
            val response = api.deleteReminder(id)
            if (response.isSuccessful) {
                Resource.success(true)
            } else if (response.code() == 404) {
                Resource.error(
                    Exception("Reminder not found"),
                    "Reminder not found"
                )
            } else {
                Resource.error(
                    Exception("Failed to delete reminder: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }
}

