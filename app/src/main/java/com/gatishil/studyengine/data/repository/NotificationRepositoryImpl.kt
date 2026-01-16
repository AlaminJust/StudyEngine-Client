package com.gatishil.studyengine.data.repository

import android.util.Log
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.SendTestNotificationRequestDto
import com.gatishil.studyengine.domain.model.DeviceToken
import com.gatishil.studyengine.domain.model.NotificationSettings
import com.gatishil.studyengine.domain.model.ScheduledNotification
import com.gatishil.studyengine.domain.repository.NotificationRepository
import com.gatishil.studyengine.service.FcmTokenManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi,
    private val fcmTokenManager: FcmTokenManager
) : NotificationRepository {

    companion object {
        private const val TAG = "NotificationRepository"
    }

    override suspend fun registerDevice(): Resource<DeviceToken> {
        return try {
            val result = fcmTokenManager.registerDevice()
            if (result.isSuccess) {
                // Fetch the registered device info
                val response = api.getDevices()
                if (response.isSuccessful) {
                    val devices = response.body() ?: emptyList()
                    val latestDevice = devices.maxByOrNull { it.createdAt }
                    if (latestDevice != null) {
                        Resource.success(latestDevice.toDomain())
                    } else {
                        Resource.error(Exception("No device found after registration"))
                    }
                } else {
                    Resource.error(Exception("Failed to get device info"))
                }
            } else {
                Resource.error(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering device", e)
            Resource.error(e)
        }
    }

    override suspend fun unregisterDevice(): Resource<Unit> {
        return try {
            val result = fcmTokenManager.unregisterDevice()
            if (result.isSuccess) {
                Resource.success(Unit)
            } else {
                Resource.error(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering device", e)
            Resource.error(e)
        }
    }

    override suspend fun getDevices(): Resource<List<DeviceToken>> {
        return try {
            val response = api.getDevices()
            if (response.isSuccessful) {
                val devices = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(devices)
            } else {
                Resource.error(Exception("Failed to get devices: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting devices", e)
            Resource.error(e)
        }
    }

    override suspend fun deactivateAllDevices(): Resource<Unit> {
        return try {
            val response = api.deactivateAllDevices()
            if (response.isSuccessful) {
                Resource.success(Unit)
            } else {
                Resource.error(Exception("Failed to deactivate devices: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating all devices", e)
            Resource.error(e)
        }
    }

    override suspend fun getNotificationSettings(): Resource<NotificationSettings> {
        return try {
            val response = api.getNotificationSettings()
            if (response.isSuccessful) {
                response.body()?.let { settings ->
                    Resource.success(
                        NotificationSettings(
                            enableSessionReminders = settings.enableSessionReminders,
                            reminderMinutesBefore = settings.reminderMinutesBefore,
                            enableStreakReminders = settings.enableStreakReminders,
                            enableWeeklyDigest = settings.enableWeeklyDigest,
                            enableAchievementNotifications = settings.enableAchievementNotifications,
                            registeredDevicesCount = settings.registeredDevicesCount
                        )
                    )
                } ?: Resource.error(Exception("Empty response"))
            } else {
                Resource.error(Exception("Failed to get notification settings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notification settings", e)
            Resource.error(e)
        }
    }

    override suspend fun getScheduledNotifications(): Resource<List<ScheduledNotification>> {
        return try {
            val response = api.getScheduledNotifications()
            if (response.isSuccessful) {
                val notifications = response.body()?.notifications?.map { it.toDomain() } ?: emptyList()
                Resource.success(notifications)
            } else {
                Resource.error(Exception("Failed to get scheduled notifications: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting scheduled notifications", e)
            Resource.error(e)
        }
    }

    override suspend fun cancelScheduledNotification(notificationId: String): Resource<Unit> {
        return try {
            val response = api.cancelScheduledNotification(notificationId)
            if (response.isSuccessful) {
                Resource.success(Unit)
            } else {
                Resource.error(Exception("Failed to cancel notification: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification", e)
            Resource.error(e)
        }
    }

    override suspend fun sendTestNotification(): Resource<Unit> {
        return try {
            val response = api.sendTestNotification(SendTestNotificationRequestDto())
            if (response.isSuccessful) {
                Resource.success(Unit)
            } else {
                Resource.error(Exception("Failed to send test notification: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending test notification", e)
            Resource.error(e)
        }
    }

    private fun com.gatishil.studyengine.data.remote.dto.DeviceTokenDto.toDomain(): DeviceToken {
        return DeviceToken(
            id = id,
            platform = platform,
            deviceName = deviceName,
            appVersion = appVersion,
            isActive = isActive,
            lastSuccessfulPush = lastSuccessfulPush?.let { parseDateTime(it) },
            createdAt = parseDateTime(createdAt),
            updatedAt = parseDateTime(updatedAt)
        )
    }

    private fun com.gatishil.studyengine.data.remote.dto.ScheduledNotificationDto.toDomain(): ScheduledNotification {
        return ScheduledNotification(
            id = id,
            notificationType = notificationType,
            title = title,
            body = body,
            scheduledFor = parseDateTime(scheduledFor),
            status = status,
            sessionId = sessionId
        )
    }

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}

