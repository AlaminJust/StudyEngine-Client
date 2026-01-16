package com.gatishil.studyengine.domain.model

import java.time.LocalDateTime

data class DeviceToken(
    val id: String,
    val platform: String,
    val deviceName: String?,
    val appVersion: String?,
    val isActive: Boolean,
    val lastSuccessfulPush: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class NotificationSettings(
    val enableSessionReminders: Boolean,
    val reminderMinutesBefore: Int,
    val enableStreakReminders: Boolean,
    val enableWeeklyDigest: Boolean,
    val enableAchievementNotifications: Boolean,
    val registeredDevicesCount: Int
)

data class ScheduledNotification(
    val id: String,
    val notificationType: String,
    val title: String,
    val body: String,
    val scheduledFor: LocalDateTime,
    val status: String,
    val sessionId: String?
)

