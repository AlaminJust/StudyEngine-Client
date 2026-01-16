package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== Request DTOs ====================

@Serializable
data class RegisterDeviceTokenRequestDto(
    @SerialName("token") val token: String,
    @SerialName("platform") val platform: String,
    @SerialName("deviceName") val deviceName: String? = null,
    @SerialName("appVersion") val appVersion: String? = null
)

@Serializable
data class UnregisterDeviceTokenRequestDto(
    @SerialName("token") val token: String
)

@Serializable
data class SendTestNotificationRequestDto(
    @SerialName("title") val title: String = "Test Notification",
    @SerialName("body") val body: String = "This is a test notification from StudyEngine!"
)

// ==================== Response DTOs ====================

@Serializable
data class DeviceTokenDto(
    @SerialName("id") val id: String,
    @SerialName("platform") val platform: String,
    @SerialName("deviceName") val deviceName: String? = null,
    @SerialName("appVersion") val appVersion: String? = null,
    @SerialName("isActive") val isActive: Boolean,
    @SerialName("lastSuccessfulPush") val lastSuccessfulPush: String? = null,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String
)

@Serializable
data class DeviceRegistrationResponseDto(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("deviceToken") val deviceToken: DeviceTokenDto? = null
)

@Serializable
data class NotificationSettingsDto(
    @SerialName("enableSessionReminders") val enableSessionReminders: Boolean,
    @SerialName("reminderMinutesBefore") val reminderMinutesBefore: Int,
    @SerialName("enableStreakReminders") val enableStreakReminders: Boolean,
    @SerialName("enableWeeklyDigest") val enableWeeklyDigest: Boolean,
    @SerialName("enableAchievementNotifications") val enableAchievementNotifications: Boolean,
    @SerialName("registeredDevicesCount") val registeredDevicesCount: Int
)

@Serializable
data class ScheduledNotificationDto(
    @SerialName("id") val id: String,
    @SerialName("notificationType") val notificationType: String,
    @SerialName("title") val title: String,
    @SerialName("body") val body: String,
    @SerialName("scheduledFor") val scheduledFor: String,
    @SerialName("status") val status: String,
    @SerialName("sessionId") val sessionId: String? = null
)

@Serializable
data class ScheduledNotificationsResponseDto(
    @SerialName("notifications") val notifications: List<ScheduledNotificationDto>,
    @SerialName("totalCount") val totalCount: Int
)

@Serializable
data class TestNotificationResponseDto(
    @SerialName("message") val message: String,
    @SerialName("successCount") val successCount: Int,
    @SerialName("failureCount") val failureCount: Int,
    @SerialName("invalidTokens") val invalidTokens: Int
)

