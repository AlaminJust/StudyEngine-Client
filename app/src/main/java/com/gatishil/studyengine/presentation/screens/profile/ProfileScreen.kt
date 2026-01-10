package com.gatishil.studyengine.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.presentation.common.components.ErrorScreen
import com.gatishil.studyengine.presentation.common.components.LoadingScreen
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.ProfileUpdated -> {
                    snackbarHostState.showSnackbar("Profile updated successfully")
                }
                is ProfileEvent.PreferencesUpdated -> {
                    snackbarHostState.showSnackbar("Preferences updated successfully")
                }
                is ProfileEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.profile == null -> {
                    LoadingScreen()
                }
                uiState.error != null && uiState.profile == null -> {
                    ErrorScreen(
                        message = uiState.error ?: "Something went wrong",
                        onRetry = { viewModel.loadProfile() }
                    )
                }
                uiState.profile != null -> {
                    ProfileContent(
                        profile = uiState.profile!!,
                        onEditName = { viewModel.showEditNameDialog() },
                        onEditStudyGoals = { viewModel.showEditStudyGoalsDialog() },
                        onEditReadingSpeed = { viewModel.showEditReadingSpeedDialog() },
                        onEditSessionPrefs = { viewModel.showEditSessionPrefsDialog() },
                        onEditNotificationPrefs = { viewModel.showEditNotificationPrefsDialog() },
                        onEditPrivacy = { viewModel.showEditPrivacyDialog() }
                    )
                }
            }
        }

        // All Edit Dialogs
        if (uiState.editNameDialogVisible) {
            EditNameDialog(
                name = uiState.editName,
                onNameChange = { viewModel.updateEditName(it) },
                onConfirm = { viewModel.saveProfileName() },
                onDismiss = { viewModel.hideEditNameDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editStudyGoalsDialogVisible) {
            EditStudyGoalsDialog(
                dailyPages = uiState.editDailyPagesGoal,
                dailyMinutes = uiState.editDailyMinutesGoal,
                weeklyDays = uiState.editWeeklyStudyDaysGoal,
                onDailyPagesChange = { viewModel.updateEditStudyGoals(dailyPages = it) },
                onDailyMinutesChange = { viewModel.updateEditStudyGoals(dailyMinutes = it) },
                onWeeklyDaysChange = { viewModel.updateEditStudyGoals(weeklyDays = it) },
                onConfirm = { viewModel.saveStudyGoals() },
                onDismiss = { viewModel.hideEditStudyGoalsDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editReadingSpeedDialogVisible) {
            EditReadingSpeedDialog(
                pagesPerHour = uiState.editPagesPerHour,
                onPagesPerHourChange = { viewModel.updateEditReadingSpeed(it) },
                onConfirm = { viewModel.saveReadingSpeed() },
                onDismiss = { viewModel.hideEditReadingSpeedDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editSessionPrefsDialogVisible) {
            EditSessionPrefsDialog(
                preferredDuration = uiState.editPreferredSessionDuration,
                minDuration = uiState.editMinSessionDuration,
                maxDuration = uiState.editMaxSessionDuration,
                onPreferredChange = { viewModel.updateEditSessionPrefs(preferred = it) },
                onMinChange = { viewModel.updateEditSessionPrefs(min = it) },
                onMaxChange = { viewModel.updateEditSessionPrefs(max = it) },
                onConfirm = { viewModel.saveSessionPrefs() },
                onDismiss = { viewModel.hideEditSessionPrefsDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editNotificationPrefsDialogVisible) {
            EditNotificationPrefsDialog(
                enableSessionReminders = uiState.editEnableSessionReminders,
                reminderMinutesBefore = uiState.editReminderMinutesBefore,
                enableStreakReminders = uiState.editEnableStreakReminders,
                enableWeeklyDigest = uiState.editEnableWeeklyDigest,
                enableAchievementNotifications = uiState.editEnableAchievementNotifications,
                onSessionRemindersChange = { viewModel.updateEditNotificationPrefs(enableSessionReminders = it) },
                onReminderMinutesChange = { viewModel.updateEditNotificationPrefs(reminderMinutesBefore = it) },
                onStreakRemindersChange = { viewModel.updateEditNotificationPrefs(enableStreakReminders = it) },
                onWeeklyDigestChange = { viewModel.updateEditNotificationPrefs(enableWeeklyDigest = it) },
                onAchievementNotificationsChange = { viewModel.updateEditNotificationPrefs(enableAchievementNotifications = it) },
                onConfirm = { viewModel.saveNotificationPrefs() },
                onDismiss = { viewModel.hideEditNotificationPrefsDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editPrivacyDialogVisible) {
            EditPrivacyDialog(
                showProfilePublicly = uiState.editShowProfilePublicly,
                showStatsPublicly = uiState.editShowStatsPublicly,
                onShowProfileChange = { viewModel.updateEditPrivacy(showProfilePublicly = it) },
                onShowStatsChange = { viewModel.updateEditPrivacy(showStatsPublicly = it) },
                onConfirm = { viewModel.savePrivacySettings() },
                onDismiss = { viewModel.hideEditPrivacyDialog() },
                isLoading = uiState.isUpdating
            )
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    onEditName: () -> Unit,
    onEditStudyGoals: () -> Unit,
    onEditReadingSpeed: () -> Unit,
    onEditSessionPrefs: () -> Unit,
    onEditNotificationPrefs: () -> Unit,
    onEditPrivacy: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(profile = profile, onEditName = onEditName)
        }

        // Study Stats Section
        item {
            SectionTitle(title = stringResource(R.string.study_summary))
        }
        item {
            StudyStatsGrid(studySummary = profile.studySummary)
        }

        // Library Section
        item {
            SectionTitle(title = stringResource(R.string.my_books))
        }
        item {
            LibraryStatsCard(librarySummary = profile.librarySummary)
        }

        // Study Goals Section (Editable)
        item {
            EditableSectionTitle(
                title = stringResource(R.string.study_goals),
                onEdit = onEditStudyGoals
            )
        }
        item {
            StudyGoalsCard(preferences = profile.preferences, onEdit = onEditStudyGoals)
        }

        // Session Preferences Section (Editable)
        item {
            EditableSectionTitle(
                title = stringResource(R.string.session_preferences),
                onEdit = onEditSessionPrefs
            )
        }
        item {
            SessionPreferencesCard(preferences = profile.preferences, onEdit = onEditSessionPrefs)
        }

        // Notification Preferences Section (Editable)
        item {
            EditableSectionTitle(
                title = stringResource(R.string.notification_preferences),
                onEdit = onEditNotificationPrefs
            )
        }
        item {
            NotificationPreferencesCard(preferences = profile.preferences.notifications, onEdit = onEditNotificationPrefs)
        }

        // Privacy Settings Section (Editable)
        item {
            EditableSectionTitle(
                title = stringResource(R.string.privacy_settings),
                onEdit = onEditPrivacy
            )
        }
        item {
            PrivacySettingsCard(privacy = profile.preferences.privacy, onEdit = onEditPrivacy)
        }

        // Account Info Section
        item {
            SectionTitle(title = stringResource(R.string.account_info))
        }
        item {
            AccountInfoCard(profile = profile)
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    onEditName: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Profile Picture
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                if (profile.profilePictureUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profile.profilePictureUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = profile.name.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name with edit button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onEditName, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit name",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Member since badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${profile.daysSinceJoined} ${stringResource(R.string.days)} ${stringResource(R.string.member_since)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun EditableSectionTitle(title: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.edit))
        }
    }
}

@Composable
private fun StudyStatsGrid(studySummary: ProfileStudySummary) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem(
                icon = Icons.Default.Bolt,
                value = studySummary.currentStreak.toString(),
                label = stringResource(R.string.current_streak),
                color = StudyEngineTheme.extendedColors.success,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                icon = Icons.Default.EmojiEvents,
                value = studySummary.longestStreak.toString(),
                label = stringResource(R.string.longest_streak),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                value = studySummary.totalPagesRead.toString(),
                label = stringResource(R.string.pages_read),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                icon = Icons.Default.Schedule,
                value = "${studySummary.totalHoursStudied}h",
                label = stringResource(R.string.hours_studied),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem(
                icon = Icons.Default.CheckCircle,
                value = studySummary.totalSessionsCompleted.toString(),
                label = stringResource(R.string.sessions_completed_label),
                color = StudyEngineTheme.extendedColors.sessionCompleted,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                icon = Icons.Default.Star,
                value = studySummary.achievementsUnlocked.toString(),
                label = stringResource(R.string.achievements),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Column {
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LibraryStatsCard(librarySummary: LibrarySummary) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LibraryStat(value = librarySummary.totalBooks.toString(), label = stringResource(R.string.total_books))
            VerticalDivider()
            LibraryStat(value = librarySummary.activeBooks.toString(), label = stringResource(R.string.active))
            VerticalDivider()
            LibraryStat(value = librarySummary.completedBooks.toString(), label = stringResource(R.string.completed_label))
            VerticalDivider()
            LibraryStat(value = librarySummary.totalPages.toString(), label = stringResource(R.string.pages))
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

@Composable
private fun LibraryStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun StudyGoalsCard(preferences: UserPreferences, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GoalRow(icon = Icons.AutoMirrored.Filled.MenuBook, label = stringResource(R.string.daily_pages_goal), value = "${preferences.dailyPagesGoal} pages")
            HorizontalDivider()
            GoalRow(icon = Icons.Default.Schedule, label = stringResource(R.string.daily_minutes_goal), value = "${preferences.dailyMinutesGoal} min")
            HorizontalDivider()
            GoalRow(icon = Icons.Default.CalendarMonth, label = stringResource(R.string.weekly_study_days), value = "${preferences.weeklyStudyDaysGoal} days")
            HorizontalDivider()
            GoalRow(icon = Icons.Default.Speed, label = stringResource(R.string.reading_speed), value = "${preferences.pagesPerHour} pages/hour")
        }
    }
}

@Composable
private fun SessionPreferencesCard(preferences: UserPreferences, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GoalRow(icon = Icons.Default.Timer, label = stringResource(R.string.preferred_session_duration), value = "${preferences.preferredSessionDurationMinutes} min")
            HorizontalDivider()
            GoalRow(icon = Icons.Default.TimerOff, label = stringResource(R.string.min_session_duration), value = "${preferences.minSessionDurationMinutes} min")
            HorizontalDivider()
            GoalRow(icon = Icons.Default.Timelapse, label = stringResource(R.string.max_session_duration), value = "${preferences.maxSessionDurationMinutes} min")
        }
    }
}

@Composable
private fun NotificationPreferencesCard(preferences: NotificationPreferences, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ToggleRow(icon = Icons.Default.Notifications, label = stringResource(R.string.session_reminders), isEnabled = preferences.enableSessionReminders)
            if (preferences.enableSessionReminders) {
                Text(
                    text = "${preferences.reminderMinutesBefore} min before",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 52.dp)
                )
            }
            HorizontalDivider()
            ToggleRow(icon = Icons.Default.Bolt, label = stringResource(R.string.streak_reminders), isEnabled = preferences.enableStreakReminders)
            HorizontalDivider()
            ToggleRow(icon = Icons.Default.Email, label = stringResource(R.string.weekly_digest), isEnabled = preferences.enableWeeklyDigest)
            HorizontalDivider()
            ToggleRow(icon = Icons.Default.EmojiEvents, label = stringResource(R.string.achievement_notifications), isEnabled = preferences.enableAchievementNotifications)
        }
    }
}

@Composable
private fun PrivacySettingsCard(privacy: PrivacySettings, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ToggleRow(icon = Icons.Default.Person, label = stringResource(R.string.show_profile_publicly), isEnabled = privacy.showProfilePublicly)
            HorizontalDivider()
            ToggleRow(icon = Icons.Default.BarChart, label = stringResource(R.string.show_stats_publicly), isEnabled = privacy.showStatsPublicly)
        }
    }
}

@Composable
private fun GoalRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, label: String, isEnabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(
            imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isEnabled) StudyEngineTheme.extendedColors.success else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AccountInfoCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AccountInfoRow(label = stringResource(R.string.email), value = profile.email)
            HorizontalDivider()
            AccountInfoRow(label = stringResource(R.string.auth_provider), value = profile.authProvider.replaceFirstChar { it.uppercase() })
            HorizontalDivider()
            AccountInfoRow(label = stringResource(R.string.timezone), value = profile.timeZone)
            HorizontalDivider()
            AccountInfoRow(
                label = stringResource(R.string.account_status),
                value = if (profile.isActive) stringResource(R.string.active) else stringResource(R.string.inactive),
                valueColor = if (profile.isActive) StudyEngineTheme.extendedColors.success else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AccountInfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

// ==================== Edit Dialogs ====================

@Composable
private fun EditNameDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_name)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank() && !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun EditStudyGoalsDialog(
    dailyPages: String,
    dailyMinutes: String,
    weeklyDays: String,
    onDailyPagesChange: (String) -> Unit,
    onDailyMinutesChange: (String) -> Unit,
    onWeeklyDaysChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.study_goals)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dailyPages,
                    onValueChange = onDailyPagesChange,
                    label = { Text(stringResource(R.string.daily_pages_goal)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dailyMinutes,
                    onValueChange = onDailyMinutesChange,
                    label = { Text(stringResource(R.string.daily_minutes_goal)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weeklyDays,
                    onValueChange = onWeeklyDaysChange,
                    label = { Text(stringResource(R.string.weekly_study_days)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun EditReadingSpeedDialog(
    pagesPerHour: String,
    onPagesPerHourChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reading_speed)) },
        text = {
            OutlinedTextField(
                value = pagesPerHour,
                onValueChange = onPagesPerHourChange,
                label = { Text(stringResource(R.string.pages_per_hour)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = pagesPerHour.isNotBlank() && !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun EditSessionPrefsDialog(
    preferredDuration: String,
    minDuration: String,
    maxDuration: String,
    onPreferredChange: (String) -> Unit,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.session_preferences)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = preferredDuration,
                    onValueChange = onPreferredChange,
                    label = { Text(stringResource(R.string.preferred_session_duration)) },
                    suffix = { Text("min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = minDuration,
                    onValueChange = onMinChange,
                    label = { Text(stringResource(R.string.min_session_duration)) },
                    suffix = { Text("min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxDuration,
                    onValueChange = onMaxChange,
                    label = { Text(stringResource(R.string.max_session_duration)) },
                    suffix = { Text("min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun EditNotificationPrefsDialog(
    enableSessionReminders: Boolean,
    reminderMinutesBefore: String,
    enableStreakReminders: Boolean,
    enableWeeklyDigest: Boolean,
    enableAchievementNotifications: Boolean,
    onSessionRemindersChange: (Boolean) -> Unit,
    onReminderMinutesChange: (String) -> Unit,
    onStreakRemindersChange: (Boolean) -> Unit,
    onWeeklyDigestChange: (Boolean) -> Unit,
    onAchievementNotificationsChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notification_preferences)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SwitchRow(label = stringResource(R.string.session_reminders), checked = enableSessionReminders, onCheckedChange = onSessionRemindersChange, enabled = !isLoading)
                if (enableSessionReminders) {
                    OutlinedTextField(
                        value = reminderMinutesBefore,
                        onValueChange = onReminderMinutesChange,
                        label = { Text(stringResource(R.string.reminder_minutes_before)) },
                        suffix = { Text("min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                SwitchRow(label = stringResource(R.string.streak_reminders), checked = enableStreakReminders, onCheckedChange = onStreakRemindersChange, enabled = !isLoading)
                SwitchRow(label = stringResource(R.string.weekly_digest), checked = enableWeeklyDigest, onCheckedChange = onWeeklyDigestChange, enabled = !isLoading)
                SwitchRow(label = stringResource(R.string.achievement_notifications), checked = enableAchievementNotifications, onCheckedChange = onAchievementNotificationsChange, enabled = !isLoading)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun EditPrivacyDialog(
    showProfilePublicly: Boolean,
    showStatsPublicly: Boolean,
    onShowProfileChange: (Boolean) -> Unit,
    onShowStatsChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.privacy_settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SwitchRow(label = stringResource(R.string.show_profile_publicly), checked = showProfilePublicly, onCheckedChange = onShowProfileChange, enabled = !isLoading)
                SwitchRow(label = stringResource(R.string.show_stats_publicly), checked = showStatsPublicly, onCheckedChange = onShowStatsChange, enabled = !isLoading)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

