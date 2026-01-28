package com.gatishil.studyengine.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gatishil.studyengine.NotificationClickActivity
import com.gatishil.studyengine.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StudyEngineFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID_SESSION_REMINDERS = "session_reminders"
        const val CHANNEL_ID_STREAK_REMINDERS = "streak_reminders"
        const val CHANNEL_ID_ACHIEVEMENTS = "achievements"
        const val CHANNEL_ID_GENERAL = "general"

        private const val NOTIFICATION_ID_BASE = 1000
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            fcmTokenManager.onNewToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // With backend sending DATA-ONLY pushes, always build notification from message.data
        if (message.data.isNotEmpty()) {
            val title = message.data["title"] ?: getString(R.string.app_name)
            val body = message.data["body"] ?: ""
            val type = message.data["type"] ?: "general"
            showNotification(title, body, type, message.data)
            return
        }

        // Fallback (shouldn't happen with our backend, but safe)
        message.notification?.let { notification ->
            val title = notification.title ?: getString(R.string.app_name)
            val body = notification.body ?: ""
            val type = message.data["type"] ?: "general"
            showNotification(title, body, type, message.data)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        type: String,
        payload: Map<String, String>
    ) {
        val channelId = getChannelId(type)

        val requestCode = System.currentTimeMillis().toInt()
        val notificationId = generateNotificationId(type, payload)

        val intent = Intent(this, NotificationClickActivity::class.java).apply {
            action = "com.gatishil.studyengine.NOTIFICATION_CLICK"

            putExtra("notification_type", type)
            payload.forEach { (key, value) -> putExtra(key, value) }

            // Make each PendingIntent unique
            this.data = android.net.Uri.parse("studyengine://notif/$requestCode")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun getChannelId(type: String): String {
        return when (type) {
            "session_reminder", "session_start" -> CHANNEL_ID_SESSION_REMINDERS
            "streak_reminder", "streak_at_risk" -> CHANNEL_ID_STREAK_REMINDERS
            "achievement" -> CHANNEL_ID_ACHIEVEMENTS
            else -> CHANNEL_ID_GENERAL
        }
    }

    private fun generateNotificationId(type: String, payload: Map<String, String>): Int {
        val sessionId = payload["sessionId"]
        return if (sessionId != null) {
            sessionId.hashCode()
        } else {
            NOTIFICATION_ID_BASE + type.hashCode()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Session Reminders Channel
            val sessionChannel = NotificationChannel(
                CHANNEL_ID_SESSION_REMINDERS,
                getString(R.string.channel_session_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_session_reminders_description)
                enableVibration(true)
            }

            // Streak Reminders Channel
            val streakChannel = NotificationChannel(
                CHANNEL_ID_STREAK_REMINDERS,
                getString(R.string.channel_streak_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_streak_reminders_description)
            }

            // Achievements Channel
            val achievementChannel = NotificationChannel(
                CHANNEL_ID_ACHIEVEMENTS,
                getString(R.string.channel_achievements),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_achievements_description)
            }

            // General Channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                getString(R.string.channel_general),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_general_description)
            }

            notificationManager.createNotificationChannels(
                listOf(sessionChannel, streakChannel, achievementChannel, generalChannel)
            )
        }
    }
}
