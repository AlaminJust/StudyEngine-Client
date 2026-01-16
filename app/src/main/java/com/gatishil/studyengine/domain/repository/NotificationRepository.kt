package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.DeviceToken
import com.gatishil.studyengine.domain.model.NotificationSettings
import com.gatishil.studyengine.domain.model.ScheduledNotification

interface NotificationRepository {
    suspend fun registerDevice(): Resource<DeviceToken>
    suspend fun unregisterDevice(): Resource<Unit>
    suspend fun getDevices(): Resource<List<DeviceToken>>
    suspend fun deactivateAllDevices(): Resource<Unit>
    suspend fun getNotificationSettings(): Resource<NotificationSettings>
    suspend fun getScheduledNotifications(): Resource<List<ScheduledNotification>>
    suspend fun cancelScheduledNotification(notificationId: String): Resource<Unit>
    suspend fun sendTestNotification(): Resource<Unit>
}

