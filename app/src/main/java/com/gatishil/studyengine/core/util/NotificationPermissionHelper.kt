package com.gatishil.studyengine.core.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit

/**
 * Helper class for managing notification permissions on Android 13+
 */
object NotificationPermissionHelper {

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, check if notifications are enabled
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Check if we should show rationale for notification permission
     */
    fun shouldShowRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            false
        }
    }

    /**
     * Check if notification permission request is needed (Android 13+)
     */
    fun isPermissionRequestNeeded(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * Get the notification permission string for Android 13+
     */
    fun getNotificationPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            ""
        }
    }

    /**
     * Open app notification settings
     */
    fun openNotificationSettings(context: Context) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Check if this is the first time asking for permission
     * Uses SharedPreferences to track
     */
    fun isFirstTimeAskingPermission(context: Context, permission: String): Boolean {
        val prefs = context.getSharedPreferences("notification_perm_prefs", Context.MODE_PRIVATE)
        return !prefs.getBoolean("asked_$permission", false)
    }

    /**
     * Mark that we've asked for permission before
     */
    fun markPermissionAsAsked(context: Context, permission: String) {
        val prefs = context.getSharedPreferences("notification_perm_prefs", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("asked_$permission", true) }
    }

    /**
     * Check if user has permanently denied the permission
     */
    fun isPermanentlyDenied(activity: Activity): Boolean {
        if (!isPermissionRequestNeeded()) return false

        val hasAskedBefore = !isFirstTimeAskingPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        )
        val shouldShowRationale = shouldShowRationale(activity)
        val hasPermission = hasNotificationPermission(activity)

        // If we asked before, don't show rationale, and don't have permission,
        // the user has permanently denied
        return hasAskedBefore && !shouldShowRationale && !hasPermission
    }
}

