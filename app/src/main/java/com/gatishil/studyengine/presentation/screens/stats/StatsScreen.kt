package com.gatishil.studyengine.presentation.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.presentation.common.components.ErrorScreen
import com.gatishil.studyengine.presentation.common.components.LoadingScreen
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.stats == null -> {
                    LoadingScreen()
                }
                uiState.error != null && uiState.stats == null -> {
                    ErrorScreen(
                        message = uiState.error ?: stringResource(R.string.something_went_wrong),
                        onRetry = { viewModel.loadStats() }
                    )
                }
                uiState.stats != null -> {
                    StatsContent(
                        stats = uiState.stats!!,
                        achievements = uiState.achievements,
                        calendarMonth = uiState.calendarMonth,
                        selectedMonth = uiState.selectedMonth,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsContent(
    stats: StudyStats,
    achievements: List<Achievement>,
    calendarMonth: CalendarMonth?,
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Streak Hero Card
        item {
            StreakHeroCard(stats = stats)
        }

        // Quick Stats Grid
        item {
            QuickStatsGrid(stats = stats)
        }

        // Weekly Summary
        stats.weeklyStats?.let { weekly ->
            item {
                WeeklySummaryCard(weeklyStats = weekly)
            }
        }

        // Calendar Section
        item {
            CalendarSection(
                calendarMonth = calendarMonth,
                selectedMonth = selectedMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }

        // Lifetime Stats
        item {
            LifetimeStatsCard(stats = stats)
        }

        // Achievements
        if (achievements.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.achievements),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                AchievementsSection(achievements = achievements)
            }
        }

        // Motivational Message
        stats.streakMessage?.let { message ->
            item {
                MotivationalCard(message = message)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StreakHeroCard(stats: StudyStats) {
    val isActive = stats.isStreakActive

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                StudyEngineTheme.extendedColors.success.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔥",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stats.currentStreak.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = if (isActive) StudyEngineTheme.extendedColors.success else MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(R.string.day_streak),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakInfo(
                    label = stringResource(R.string.longest_streak),
                    value = "${stats.longestStreak} ${stringResource(R.string.days)}"
                )
                StreakInfo(
                    label = stringResource(R.string.perfect_weeks),
                    value = stats.perfectWeeksCount.toString()
                )
            }

            stats.nextStreakMilestone?.let { milestone ->
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { stats.currentStreak.toFloat() / milestone },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isActive) StudyEngineTheme.extendedColors.success else MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stats.daysToNextMilestone ?: 0} ${stringResource(R.string.days_to_milestone)} $milestone",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StreakInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickStatsGrid(stats: StudyStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatItem(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                value = stats.totalPagesRead.toString(),
                label = stringResource(R.string.total_pages)
            )
            QuickStatItem(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.Default.Timer,
                value = String.format(Locale.US, "%.1f", stats.totalHoursStudied),
                label = stringResource(R.string.hours_studied)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatItem(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.Default.EventAvailable,
                value = stats.totalSessionsCompleted.toString(),
                label = stringResource(R.string.sessions_completed_stat)
            )
            QuickStatItem(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.Default.CheckCircle,
                value = stats.totalBooksCompleted.toString(),
                label = stringResource(R.string.books_completed)
            )
        }
    }
}

@Composable
private fun QuickStatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WeeklySummaryCard(weeklyStats: WeeklyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.this_week),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (weeklyStats.isPerfectWeek) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StudyEngineTheme.extendedColors.success.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "⭐ ${stringResource(R.string.perfect_week)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = StudyEngineTheme.extendedColors.success,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeeklyStatItem(
                    value = weeklyStats.studyDays.toString(),
                    label = stringResource(R.string.study_days),
                    maxValue = "7"
                )
                WeeklyStatItem(
                    value = weeklyStats.pagesRead.toString(),
                    label = stringResource(R.string.pages)
                )
                WeeklyStatItem(
                    value = "${weeklyStats.minutesStudied / 60}h ${weeklyStats.minutesStudied % 60}m",
                    label = stringResource(R.string.time)
                )
            }
        }
    }
}

@Composable
private fun WeeklyStatItem(
    value: String,
    label: String,
    maxValue: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (maxValue != null) {
            Text(
                text = "$value/$maxValue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CalendarSection(
    calendarMonth: CalendarMonth?,
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                }
                Text(
                    text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = onNextMonth,
                    enabled = selectedMonth < YearMonth.now()
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            if (calendarMonth != null) {
                CalendarGrid(calendarMonth = calendarMonth, selectedMonth = selectedMonth)
            }

            // Summary
            calendarMonth?.let {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "${it.totalStudyDays} ${stringResource(R.string.study_days)}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "${it.totalPagesRead} ${stringResource(R.string.pages)}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    calendarMonth: CalendarMonth,
    selectedMonth: YearMonth
) {
    val firstDayOfMonth = selectedMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
    val daysInMonth = selectedMonth.lengthOfMonth()

    val studyDaysMap = calendarMonth.days.associateBy { it.date.dayOfMonth }

    Column {
        var dayCounter = 1
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    if (week == 0 && dayOfWeek < startDayOfWeek) {
                        Box(modifier = Modifier.size(36.dp))
                    } else if (dayCounter <= daysInMonth) {
                        val studyDay = studyDaysMap[dayCounter]
                        CalendarDay(
                            day = dayCounter,
                            isStudyDay = studyDay?.isStreakDay ?: false,
                            pagesRead = studyDay?.pagesRead ?: 0
                        )
                        dayCounter++
                    } else {
                        Box(modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isStudyDay: Boolean,
    pagesRead: Int
) {
    val intensity = when {
        pagesRead >= 50 -> 1f
        pagesRead >= 30 -> 0.7f
        pagesRead >= 10 -> 0.4f
        pagesRead > 0 -> 0.2f
        else -> 0f
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (isStudyDay) {
                    StudyEngineTheme.extendedColors.success.copy(alpha = intensity)
                } else {
                    Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = if (isStudyDay) {
                if (intensity > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun LifetimeStatsCard(stats: StudyStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.lifetime_stats),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LifetimeStat(
                label = stringResource(R.string.total_study_days),
                value = stats.totalStudyDays.toString()
            )
            LifetimeStat(
                label = stringResource(R.string.avg_pages_per_session),
                value = String.format(Locale.US, "%.1f", stats.averagePagesPerSession)
            )
            LifetimeStat(
                label = stringResource(R.string.avg_minutes_per_session),
                value = String.format(Locale.US, "%.0f min", stats.averageMinutesPerSession)
            )
            LifetimeStat(
                label = stringResource(R.string.avg_pages_per_day),
                value = String.format(Locale.US, "%.1f", stats.averagePagesPerDay)
            )
        }
    }
}

@Composable
private fun LifetimeStat(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AchievementsSection(achievements: List<Achievement>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(achievements) { achievement ->
            AchievementCard(achievement = achievement)
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val achievementIcon = when {
        achievement.id.contains("streak") -> Icons.Default.LocalFireDepartment
        achievement.id.contains("book") -> Icons.Default.MenuBook
        achievement.id.contains("page") -> Icons.Default.Description
        achievement.id.contains("session") -> Icons.Default.EventAvailable
        achievement.id.contains("week") -> Icons.Default.DateRange
        achievement.id.contains("hour") -> Icons.Default.Timer
        else -> Icons.Default.EmojiEvents
    }

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(130.dp), // Fixed height for all cards
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isAchieved) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with lock overlay for unachieved
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = achievementIcon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (achievement.isAchieved) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
                if (!achievement.isAchieved) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                color = if (achievement.isAchieved) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
    }
}

@Composable
private fun MotivationalCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💬",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

