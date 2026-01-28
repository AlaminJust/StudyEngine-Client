package com.gatishil.studyengine

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Trampoline activity used as a stable entry-point for notification clicks.
 *
 * Why:
 * - When the app is in a killed state, notification click intents can be delivered in
 *   different ways depending on whether the push was "notification" vs "data".
 * - This activity is lightweight, exported, and always forwards to MainActivity using
 *   consistent flags.
 * - Prevents crashes on repeated notification clicks by avoiding complex init work here.
 */
class NotificationClickActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val target = Intent(this, MainActivity::class.java).apply {
            // Create/bring task to front reliably
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            // Forward the original extras so MainActivity can route.
            intent?.extras?.let { putExtras(it) }

            // Mark as notification open.
            action = ACTION_NOTIFICATION_OPEN
        }

        startActivity(target)
        finish()
    }

    companion object {
        const val ACTION_NOTIFICATION_OPEN = "com.gatishil.studyengine.ACTION_NOTIFICATION_OPEN"
    }
}

