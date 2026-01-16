package com.gatishil.studyengine.service

import android.content.Context
import android.os.Build
import android.util.Log
import com.gatishil.studyengine.BuildConfig
import com.gatishil.studyengine.core.session.UserSessionManager
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.RegisterDeviceTokenRequestDto
import com.gatishil.studyengine.data.remote.dto.UnregisterDeviceTokenRequestDto
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: StudyEngineApi,
    private val userSessionManager: UserSessionManager
) {
    companion object {
        private const val TAG = "FcmTokenManager"
        private const val PLATFORM = "android"
    }

    /**
     * Get the current FCM token
     */
    suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }

    /**
     * Register the device with the backend
     * Should be called after user login and when token is refreshed
     */
    suspend fun registerDevice(): Result<Unit> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Failed to get FCM token"))

            // Check if user is logged in
            val isLoggedIn = userSessionManager.isLoggedIn.first()
            if (!isLoggedIn) {
                Log.d(TAG, "User not logged in, skipping device registration")
                return Result.success(Unit)
            }

            val request = RegisterDeviceTokenRequestDto(
                token = token,
                platform = PLATFORM,
                deviceName = getDeviceName(),
                appVersion = BuildConfig.VERSION_NAME
            )

            val response = api.registerDevice(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "Device registered successfully")
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to register device: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering device", e)
            Result.failure(e)
        }
    }

    /**
     * Unregister the device from the backend
     * Should be called when user logs out
     */
    suspend fun unregisterDevice(): Result<Unit> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Failed to get FCM token"))

            val request = UnregisterDeviceTokenRequestDto(token = token)
            val response = api.unregisterDevice(request)

            if (response.isSuccessful) {
                Log.d(TAG, "Device unregistered successfully")
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to unregister device: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering device", e)
            Result.failure(e)
        }
    }

    /**
     * Called when FCM token is refreshed
     */
    suspend fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed")

        // Check if user is logged in before registering
        val isLoggedIn = userSessionManager.isLoggedIn.first()
        if (isLoggedIn) {
            registerDevice()
        }
    }

    /**
     * Subscribe to a topic
     */
    suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "Subscribed to topic: $topic")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe to topic: $topic", e)
            Result.failure(e)
        }
    }

    /**
     * Unsubscribe from a topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Unsubscribed from topic: $topic")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unsubscribe from topic: $topic", e)
            Result.failure(e)
        }
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }
}

