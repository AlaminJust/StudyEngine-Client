package com.gatishil.studyengine.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.data.local.datastore.SettingsPreferences
import kotlinx.coroutines.flow.collectLatest
import com.gatishil.studyengine.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToAvailability: () -> Unit = {},
    onNavigateToScheduleOverrides: () -> Unit = {},
    onNavigateToScheduleContexts: () -> Unit = {},
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.signOutEvent.collectLatest {
            onSignOut()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Profile section
            item {
                SettingsSectionHeader(title = stringResource(R.string.profile))
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.profile),
                    subtitle = "Manage your account",
                    onClick = onNavigateToProfile
                )
            }

            // Appearance section
            item {
                SettingsSectionHeader(title = stringResource(R.string.appearance))
            }

            item {
                val themeLabel = when (uiState.themeMode) {
                    SettingsPreferences.THEME_LIGHT -> stringResource(R.string.theme_light)
                    SettingsPreferences.THEME_DARK -> stringResource(R.string.theme_dark)
                    else -> stringResource(R.string.theme_system)
                }

                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.theme),
                    subtitle = themeLabel,
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                val languageLabel = when (uiState.language) {
                    SettingsPreferences.LANGUAGE_BENGALI -> stringResource(R.string.bengali)
                    else -> stringResource(R.string.english)
                }

                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    subtitle = languageLabel,
                    onClick = { showLanguageDialog = true }
                )
            }

            // Schedule section
            item {
                SettingsSectionHeader(title = stringResource(R.string.schedule_management))
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = stringResource(R.string.study_availability),
                    subtitle = stringResource(R.string.study_availability_desc),
                    onClick = onNavigateToAvailability
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.EventBusy,
                    title = stringResource(R.string.schedule_overrides),
                    subtitle = stringResource(R.string.schedule_overrides_desc),
                    onClick = onNavigateToScheduleOverrides
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.CalendarMonth,
                    title = stringResource(R.string.schedule_contexts),
                    subtitle = stringResource(R.string.schedule_contexts_desc),
                    onClick = onNavigateToScheduleContexts
                )
            }

            // Notifications section
            item {
                SettingsSectionHeader(title = stringResource(R.string.notifications))
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.enable_notifications),
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }

            if (uiState.notificationsEnabled) {
                item {
                    SettingsItem(
                        icon = Icons.Default.AccessTime,
                        title = stringResource(R.string.reminder_before),
                        subtitle = stringResource(R.string.minutes_before, uiState.reminderMinutes),
                        onClick = { /* TODO: Show reminder picker */ }
                    )
                }
            }

            // About section
            item {
                SettingsSectionHeader(title = stringResource(R.string.about))
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.about),
                    subtitle = stringResource(R.string.version, "1.0.0"),
                    onClick = { /* TODO: Show about screen */ }
                )
            }

            // Sign out
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = stringResource(R.string.sign_out),
                    subtitle = null,
                    onClick = { showSignOutDialog = true },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Theme selection dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme)) },
            text = {
                Column {
                    ThemeOption(
                        text = stringResource(R.string.theme_system),
                        selected = uiState.themeMode == SettingsPreferences.THEME_SYSTEM,
                        onClick = {
                            viewModel.setThemeMode(SettingsPreferences.THEME_SYSTEM)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        text = stringResource(R.string.theme_light),
                        selected = uiState.themeMode == SettingsPreferences.THEME_LIGHT,
                        onClick = {
                            viewModel.setThemeMode(SettingsPreferences.THEME_LIGHT)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        text = stringResource(R.string.theme_dark),
                        selected = uiState.themeMode == SettingsPreferences.THEME_DARK,
                        onClick = {
                            viewModel.setThemeMode(SettingsPreferences.THEME_DARK)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Language selection dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    ThemeOption(
                        text = stringResource(R.string.english),
                        selected = uiState.language == SettingsPreferences.LANGUAGE_ENGLISH,
                        onClick = {
                            viewModel.setLanguage(SettingsPreferences.LANGUAGE_ENGLISH)
                            showLanguageDialog = false
                        }
                    )
                    ThemeOption(
                        text = stringResource(R.string.bengali),
                        selected = uiState.language == SettingsPreferences.LANGUAGE_BENGALI,
                        onClick = {
                            viewModel.setLanguage(SettingsPreferences.LANGUAGE_BENGALI)
                            showLanguageDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = tint
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

