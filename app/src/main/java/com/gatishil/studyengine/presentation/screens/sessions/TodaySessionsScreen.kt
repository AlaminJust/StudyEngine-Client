package com.gatishil.studyengine.presentation.screens.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.StudySession
import com.gatishil.studyengine.domain.model.StudySessionStatus
import com.gatishil.studyengine.presentation.common.components.*
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaySessionsScreen(
    onNavigateToSession: (String) -> Unit,
    onNavigateToHowItWorks: () -> Unit = {},
    onNavigateToUpcoming: () -> Unit = {},
    viewModel: TodaySessionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.today_sessions)) },
                windowInsets = WindowInsets(0.dp),
                actions = {
                    IconButton(onClick = onNavigateToUpcoming) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.upcoming_sessions)
                        )
                    }
                    IconButton(onClick = onNavigateToHowItWorks) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = stringResource(R.string.how_sessions_work)
                        )
                    }
                }
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
                uiState.isLoading && uiState.sessions.isEmpty() -> LoadingScreen()
                uiState.error != null && uiState.sessions.isEmpty() -> {
                    ErrorScreen(
                        message = uiState.error ?: stringResource(R.string.something_went_wrong),
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.sessions.isEmpty() -> EmptyTodaySessionsState(onLearnMore = onNavigateToHowItWorks)
                else -> {
                    TodaySessionsContent(
                        sessions = uiState.sessions,
                        today = today,
                        dateFormatter = dateFormatter,
                        onSessionClick = onNavigateToSession,
                        onViewUpcoming = onNavigateToUpcoming
                    )
                }
            }
        }
    }
}

@Composable
private fun TodaySessionsContent(
    sessions: List<StudySession>,
    today: LocalDate,
    dateFormatter: DateTimeFormatter,
    onSessionClick: (String) -> Unit,
    onViewUpcoming: () -> Unit
) {
    val completedCount = sessions.count { it.status == StudySessionStatus.COMPLETED }
    val inProgressCount = sessions.count { it.status == StudySessionStatus.IN_PROGRESS }
    val plannedCount = sessions.count { it.status == StudySessionStatus.PLANNED }
    val totalPages = sessions.sumOf { it.plannedPages }
    val completedPages = sessions.sumOf { it.completedPages }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today's Summary Card
        item {
            TodaySummaryCard(
                date = today.format(dateFormatter),
                totalSessions = sessions.size,
                completedCount = completedCount,
                inProgressCount = inProgressCount,
                plannedCount = plannedCount,
                totalPages = totalPages,
                completedPages = completedPages
            )
        }

        // Quick Stats Row
        item {
            QuickStatsRow(
                completedCount = completedCount,
                inProgressCount = inProgressCount,
                plannedCount = plannedCount
            )
        }

        // Sessions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.sessions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onViewUpcoming) {
                    Text(stringResource(R.string.view_upcoming))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Session Cards
        items(sessions, key = { it.id }) { session ->
            TodaySessionCard(
                session = session,
                onClick = { onSessionClick(session.id) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun TodaySummaryCard(
    date: String,
    totalSessions: Int,
    completedCount: Int,
    inProgressCount: Int,
    plannedCount: Int,
    totalPages: Int,
    completedPages: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
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
                .padding(24.dp)
        ) {
            Column {
                // Date Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.today),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStatItem(
                        value = totalSessions.toString(),
                        label = stringResource(R.string.sessions),
                        icon = Icons.Default.EventNote
                    )
                    SummaryStatItem(
                        value = totalPages.toString(),
                        label = stringResource(R.string.pages_to_read),
                        icon = Icons.AutoMirrored.Filled.MenuBook
                    )
                    SummaryStatItem(
                        value = completedPages.toString(),
                        label = stringResource(R.string.pages_done),
                        icon = Icons.Default.Done
                    )
                }

                // Progress Bar
                if (totalPages > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.progress),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${((completedPages.toFloat() / totalPages) * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (completedPages.toFloat() / totalPages).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun QuickStatsRow(
    completedCount: Int,
    inProgressCount: Int,
    plannedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.Default.CheckCircle,
            value = completedCount.toString(),
            label = stringResource(R.string.completed_label),
            color = StudyEngineTheme.extendedColors.sessionCompleted,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Default.PlayCircle,
            value = inProgressCount.toString(),
            label = stringResource(R.string.status_in_progress),
            color = StudyEngineTheme.extendedColors.sessionInProgress,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Default.Schedule,
            value = plannedCount.toString(),
            label = stringResource(R.string.status_planned),
            color = StudyEngineTheme.extendedColors.sessionPlanned,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TodaySessionCard(
    session: StudySession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (session.status) {
        StudySessionStatus.COMPLETED -> StudyEngineTheme.extendedColors.sessionCompleted
        StudySessionStatus.IN_PROGRESS -> StudyEngineTheme.extendedColors.sessionInProgress
        StudySessionStatus.MISSED -> StudyEngineTheme.extendedColors.sessionMissed
        StudySessionStatus.CANCELLED -> StudyEngineTheme.extendedColors.sessionCancelled
        else -> StudyEngineTheme.extendedColors.sessionPlanned
    }

    val statusIcon = when (session.status) {
        StudySessionStatus.COMPLETED -> Icons.Default.CheckCircle
        StudySessionStatus.IN_PROGRESS -> Icons.Default.PlayCircle
        StudySessionStatus.MISSED -> Icons.Default.ErrorOutline
        StudySessionStatus.CANCELLED -> Icons.Default.Cancel
        else -> Icons.Default.Schedule
    }

    val statusLabel = when (session.status) {
        StudySessionStatus.COMPLETED -> stringResource(R.string.status_completed)
        StudySessionStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
        StudySessionStatus.MISSED -> stringResource(R.string.status_missed)
        StudySessionStatus.CANCELLED -> stringResource(R.string.status_cancelled)
        else -> stringResource(R.string.status_planned)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Time Block
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val timeStr = "${session.startTime}"
                        Text(
                            text = if (timeStr.length >= 5) timeStr.substring(0, 5) else timeStr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            text = "${session.durationMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor.copy(alpha = 0.8f)
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Book Info
            Text(
                text = session.bookTitle ?: "Book",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            session.chapterTitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pages Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${session.plannedPages} pages planned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (session.completedPages > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = StudyEngineTheme.extendedColors.success
                        )
                        Text(
                            text = "${session.completedPages} done",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = StudyEngineTheme.extendedColors.success
                        )
                    }
                }
            }

            // Progress Bar for completed or in-progress sessions
            if (session.isCompleted || session.status == StudySessionStatus.IN_PROGRESS) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = if (session.plannedPages > 0) {
                    (session.completedPages.toFloat() / session.plannedPages).coerceIn(0f, 1f)
                } else 0f

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.progress),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.2f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTodaySessionsState(onLearnMore: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Text(
                text = stringResource(R.string.no_sessions_today),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.no_sessions_today_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onLearnMore) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.learn_how_sessions_work))
            }
        }
    }
}

