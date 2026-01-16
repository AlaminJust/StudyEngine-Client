package com.gatishil.studyengine.presentation.common.components

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gatishil.studyengine.R
import com.gatishil.studyengine.core.util.NotificationPermissionHelper

/**
 * A composable that handles notification permission with explanation dialog
 *
 * @param showDialog Whether to show the dialog
 * @param onDismiss Called when dialog is dismissed
 * @param onPermissionResult Called with the result of the permission request
 * @param context The context for creating the study plan (e.g., "study plan" or "book")
 */
@Composable
fun NotificationPermissionHandler(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    context: String = "study sessions"
) {
    val activityContext = LocalContext.current
    val activity = activityContext as? Activity

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationPermissionHelper.markPermissionAsAsked(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        onPermissionResult(isGranted)
    }

    // Check if permission is permanently denied
    val isPermanentlyDenied = activity?.let {
        NotificationPermissionHelper.isPermanentlyDenied(it)
    } ?: false

    if (showDialog) {
        NotificationPermissionDialog(
            contextName = context,
            isPermanentlyDenied = isPermanentlyDenied,
            onDismiss = onDismiss,
            onRequestPermission = {
                if (isPermanentlyDenied) {
                    // Open settings
                    NotificationPermissionHelper.openNotificationSettings(activityContext)
                    onDismiss()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    onDismiss()
                } else {
                    onPermissionResult(true)
                    onDismiss()
                }
            },
            onSkip = {
                onPermissionResult(false)
                onDismiss()
            }
        )
    }
}

@Composable
private fun NotificationPermissionDialog(
    contextName: String,
    isPermanentlyDenied: Boolean,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
    onSkip: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = stringResource(R.string.notification_permission_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = stringResource(R.string.notification_permission_message, contextName),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Benefits list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NotificationBenefitItem(
                        text = stringResource(R.string.notification_benefit_reminders)
                    )
                    NotificationBenefitItem(
                        text = stringResource(R.string.notification_benefit_streak)
                    )
                    NotificationBenefitItem(
                        text = stringResource(R.string.notification_benefit_achievements)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Warning when skipping
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.notification_skip_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isPermanentlyDenied) {
                                stringResource(R.string.open_settings)
                            } else {
                                stringResource(R.string.enable_notifications)
                            }
                        )
                    }

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.skip_for_now),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationBenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(8.dp)
        ) {}
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * A simplified banner that shows when notifications are disabled
 */
@Composable
fun NotificationDisabledBanner(
    modifier: Modifier = Modifier,
    onEnableClick: () -> Unit
) {
    val context = LocalContext.current
    val hasPermission = NotificationPermissionHelper.hasNotificationPermission(context)

    if (!hasPermission) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.notifications_disabled_banner),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                TextButton(
                    onClick = onEnableClick,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.enable),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

