package com.gatishil.studyengine.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.BuildConfig
import com.gatishil.studyengine.R
import com.gatishil.studyengine.data.local.datastore.SettingsPreferences
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToAvailability: () -> Unit = {},
    onNavigateToScheduleOverrides: () -> Unit = {},
    onNavigateToScheduleContexts: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.signOutEvent.collectLatest {
            onSignOut()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                ProfileSettingsCard(onNavigateToProfile = onNavigateToProfile)
            }

            // Appearance Section
            item {
                SettingsCard(
                    title = stringResource(R.string.appearance),
                    icon = Icons.Default.Palette,
                    iconBackgroundColor = MaterialTheme.colorScheme.primary
                ) {
                    val themeLabel = when (uiState.themeMode) {
                        SettingsPreferences.THEME_LIGHT -> stringResource(R.string.theme_light)
                        SettingsPreferences.THEME_DARK -> stringResource(R.string.theme_dark)
                        else -> stringResource(R.string.theme_system)
                    }
                    SettingsCardItem(
                        icon = Icons.Default.DarkMode,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.theme),
                        value = themeLabel,
                        onClick = { showThemeDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))

                    val languageLabel = when (uiState.language) {
                        SettingsPreferences.LANGUAGE_BENGALI -> stringResource(R.string.bengali)
                        else -> stringResource(R.string.english)
                    }
                    SettingsCardItem(
                        icon = Icons.Default.Language,
                        iconColor = StudyEngineTheme.extendedColors.success,
                        title = stringResource(R.string.language),
                        value = languageLabel,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            // Schedule Section
            item {
                SettingsCard(
                    title = stringResource(R.string.schedule_management),
                    icon = Icons.Default.CalendarMonth,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondary
                ) {
                    SettingsCardItem(
                        icon = Icons.Default.Schedule,
                        iconColor = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.study_availability),
                        onClick = onNavigateToAvailability
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    SettingsCardItem(
                        icon = Icons.Default.EventBusy,
                        iconColor = MaterialTheme.colorScheme.error,
                        title = stringResource(R.string.schedule_overrides),
                        onClick = onNavigateToScheduleOverrides
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    SettingsCardItem(
                        icon = Icons.Default.Tune,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.schedule_contexts),
                        onClick = onNavigateToScheduleContexts
                    )
                }
            }

            // Notifications Section
            item {
                SettingsCard(
                    title = stringResource(R.string.notifications),
                    icon = Icons.Default.Notifications,
                    iconBackgroundColor = StudyEngineTheme.extendedColors.sessionInProgress,
                    isSyncing = uiState.isSyncing
                ) {
                    SettingsCardSwitchItem(
                        icon = Icons.Default.NotificationsActive,
                        iconColor = StudyEngineTheme.extendedColors.success,
                        title = stringResource(R.string.session_reminders),
                        subtitle = stringResource(R.string.get_reminded_before_session),
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )

                    if (uiState.notificationsEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                        SettingsCardItem(
                            icon = Icons.Default.Timer,
                            iconColor = MaterialTheme.colorScheme.primary,
                            title = stringResource(R.string.reminder_time),
                            value = "${uiState.reminderMinutes} min before",
                            onClick = { showReminderDialog = true }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    SettingsCardSwitchItem(
                        icon = Icons.Default.Bolt,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.streak_reminders),
                        subtitle = stringResource(R.string.dont_break_streak),
                        checked = uiState.streakRemindersEnabled,
                        onCheckedChange = viewModel::setStreakRemindersEnabled
                    )

                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    SettingsCardSwitchItem(
                        icon = Icons.Default.Email,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        title = stringResource(R.string.weekly_digest),
                        subtitle = stringResource(R.string.weekly_summary_email),
                        checked = uiState.weeklyDigestEnabled,
                        onCheckedChange = viewModel::setWeeklyDigestEnabled
                    )

                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    SettingsCardSwitchItem(
                        icon = Icons.Default.EmojiEvents,
                        iconColor = StudyEngineTheme.extendedColors.sessionCompleted,
                        title = stringResource(R.string.achievement_notifications),
                        subtitle = stringResource(R.string.celebrate_achievements),
                        checked = uiState.achievementNotificationsEnabled,
                        onCheckedChange = viewModel::setAchievementNotificationsEnabled
                    )
                }
            }

            // About Section
            item {
                SettingsCard(
                    title = stringResource(R.string.about),
                    icon = Icons.Default.Info,
                    iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val versionName = BuildConfig.VERSION_NAME
                    val versionCode = BuildConfig.VERSION_CODE
                    SettingsCardItem(
                        icon = Icons.Default.Code,
                        iconColor = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.version),
                        value = "$versionName ($versionCode)",
                        onClick = { }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    SettingsCardItem(
                        icon = Icons.Default.Policy,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        title = stringResource(R.string.privacy_policy),
                        onClick = onNavigateToPrivacyPolicy
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    SettingsCardItem(
                        icon = Icons.Default.Description,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.terms_of_service),
                        onClick = onNavigateToTermsOfService
                    )
                }
            }

            // Danger Zone - Delete Account
            item {
                SettingsCard(
                    title = stringResource(R.string.danger_zone),
                    icon = Icons.Default.Warning,
                    iconBackgroundColor = MaterialTheme.colorScheme.error
                ) {
                    SettingsCardItem(
                        icon = Icons.Default.DeleteForever,
                        iconColor = MaterialTheme.colorScheme.error,
                        title = stringResource(R.string.delete_account),
                        onClick = { showDeleteAccountDialog = true }
                    )
                }
            }

            // Sign Out
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSignOutDialog = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.sign_out),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme)) },
            text = {
                Column {
                    ThemeOption(stringResource(R.string.theme_system), uiState.themeMode == SettingsPreferences.THEME_SYSTEM) {
                        viewModel.setThemeMode(SettingsPreferences.THEME_SYSTEM); showThemeDialog = false
                    }
                    ThemeOption(stringResource(R.string.theme_light), uiState.themeMode == SettingsPreferences.THEME_LIGHT) {
                        viewModel.setThemeMode(SettingsPreferences.THEME_LIGHT); showThemeDialog = false
                    }
                    ThemeOption(stringResource(R.string.theme_dark), uiState.themeMode == SettingsPreferences.THEME_DARK) {
                        viewModel.setThemeMode(SettingsPreferences.THEME_DARK); showThemeDialog = false
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    ThemeOption(stringResource(R.string.english), uiState.language == SettingsPreferences.LANGUAGE_ENGLISH) {
                        viewModel.setLanguage(SettingsPreferences.LANGUAGE_ENGLISH); showLanguageDialog = false
                    }
                    ThemeOption(stringResource(R.string.bengali), uiState.language == SettingsPreferences.LANGUAGE_BENGALI) {
                        viewModel.setLanguage(SettingsPreferences.LANGUAGE_BENGALI); showLanguageDialog = false
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    // Reminder Dialog
    if (showReminderDialog) {
        val options = listOf(5, 10, 15, 30, 60)
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text(stringResource(R.string.reminder_time)) },
            text = {
                Column {
                    options.forEach { minutes ->
                        ThemeOption("$minutes minutes before", uiState.reminderMinutes == minutes) {
                            viewModel.setReminderMinutes(minutes); showReminderDialog = false
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showReminderDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    // Sign Out Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text(stringResource(R.string.sign_out_confirm)) },
            confirmButton = {
                TextButton(onClick = { viewModel.signOut(); showSignOutDialog = false }) {
                    Text(stringResource(R.string.sign_out), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showSignOutDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAccountDialog = false
                deleteConfirmationText = ""
            },
            title = {
                Text(
                    stringResource(R.string.delete_account),
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.delete_account_warning))
                    Text(
                        stringResource(R.string.delete_account_type_confirm),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = deleteConfirmationText,
                        onValueChange = { deleteConfirmationText = it },
                        label = { Text(stringResource(R.string.type_delete_confirm)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(deleteConfirmationText)
                        showDeleteAccountDialog = false
                        deleteConfirmationText = ""
                    },
                enabled = deleteConfirmationText.equals("DELETE MY ACCOUNT", ignoreCase = true)
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteAccountDialog = false
                    deleteConfirmationText = ""
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Show loading overlay when deleting
    if (uiState.isDeletingAccount) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ProfileSettingsCard(onNavigateToProfile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .clickable { onNavigateToProfile() }
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.profile_and_goals),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.profile_settings_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Navigation indicator
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Feature tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileFeatureTag(text = stringResource(R.string.study_goals))
                    ProfileFeatureTag(text = stringResource(R.string.reading_speed))
                    ProfileFeatureTag(text = stringResource(R.string.notifications))
                }
            }
        }
    }
}

@Composable
private fun ProfileFeatureTag(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    isSyncing: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = iconBackgroundColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconBackgroundColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun SettingsCardItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsCardSwitchItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = selected, onClick = onClick)
    }
}
