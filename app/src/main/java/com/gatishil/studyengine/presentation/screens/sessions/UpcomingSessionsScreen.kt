package com.gatishil.studyengine.presentation.screens.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingSessionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSession: (String) -> Unit,
    viewModel: UpcomingSessionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.showPastSessions)
                            stringResource(R.string.session_history)
                        else
                            stringResource(R.string.upcoming_sessions)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card
            SessionSummaryCard(
                sessionCount = uiState.sessions.size,
                completedCount = uiState.sessions.count { it.status == StudySessionStatus.COMPLETED },
                missedCount = uiState.sessions.count { it.status == StudySessionStatus.MISSED },
                plannedCount = uiState.sessions.count { it.status == StudySessionStatus.PLANNED },
                isPast = uiState.showPastSessions
            )

            // Filter Section
            FilterSection(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            // Date range indicator
            DateRangeIndicator(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                sessionCount = uiState.sessions.size,
                dateFormatter = dateFormatter,
                isPast = uiState.showPastSessions
            )

            // Content
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.sessions.isEmpty() -> LoadingScreen()
                    uiState.error != null && uiState.sessions.isEmpty() -> {
                        ErrorScreen(
                            message = uiState.error ?: stringResource(R.string.something_went_wrong),
                            onRetry = { viewModel.refresh() }
                        )
                    }
                    uiState.sessions.isEmpty() -> EmptySessionsState(isPast = uiState.showPastSessions)
                    else -> {
                        SessionsByDateList(
                            groupedSessions = uiState.groupedSessions,
                            onSessionClick = onNavigateToSession,
                            dateFormatter = dateFormatter,
                            isPast = uiState.showPastSessions
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionSummaryCard(
    sessionCount: Int,
    completedCount: Int,
    missedCount: Int,
    plannedCount: Int,
    isPast: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isPast) listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary
                        ) else listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    value = sessionCount.toString(),
                    label = stringResource(R.string.total),
                    icon = Icons.Default.EventNote
                )
                if (isPast) {
                    SummaryStatItem(
                        value = completedCount.toString(),
                        label = stringResource(R.string.completed_label),
                        icon = Icons.Default.CheckCircle
                    )
                    SummaryStatItem(
                        value = missedCount.toString(),
                        label = stringResource(R.string.missed),
                        icon = Icons.Default.Cancel
                    )
                } else {
                    SummaryStatItem(
                        value = plannedCount.toString(),
                        label = stringResource(R.string.status_planned),
                        icon = Icons.Default.Schedule
                    )
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
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
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
private fun FilterSection(
    selectedFilter: SessionFilter,
    onFilterSelected: (SessionFilter) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.filter_by),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Upcoming Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipItem(
                    label = stringResource(R.string.filter_next_30_days),
                    icon = Icons.Default.DateRange,
                    selected = selectedFilter == SessionFilter.ALL,
                    onClick = { onFilterSelected(SessionFilter.ALL) }
                )
            }
            item {
                FilterChipItem(
                    label = stringResource(R.string.filter_this_week),
                    icon = Icons.Default.ViewWeek,
                    selected = selectedFilter == SessionFilter.THIS_WEEK,
                    onClick = { onFilterSelected(SessionFilter.THIS_WEEK) }
                )
            }
            item {
                FilterChipItem(
                    label = stringResource(R.string.filter_next_week),
                    icon = Icons.Default.NextWeek,
                    selected = selectedFilter == SessionFilter.NEXT_WEEK,
                    onClick = { onFilterSelected(SessionFilter.NEXT_WEEK) }
                )
            }
            item {
                FilterChipItem(
                    label = stringResource(R.string.filter_this_month),
                    icon = Icons.Default.CalendarMonth,
                    selected = selectedFilter == SessionFilter.THIS_MONTH,
                    onClick = { onFilterSelected(SessionFilter.THIS_MONTH) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Previous Sessions Filters
        Text(
            text = stringResource(R.string.previous_sessions),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipItem(
                    label = stringResource(R.string.filter_last_7_days),
                    icon = Icons.Default.History,
                    selected = selectedFilter == SessionFilter.PREVIOUS_7_DAYS,
                    onClick = { onFilterSelected(SessionFilter.PREVIOUS_7_DAYS) },
                    isPast = true
                )
            }
            item {
                FilterChipItem(
                    label = stringResource(R.string.filter_last_30_days),
                    icon = Icons.Default.History,
                    selected = selectedFilter == SessionFilter.PREVIOUS_30_DAYS,
                    onClick = { onFilterSelected(SessionFilter.PREVIOUS_30_DAYS) },
                    isPast = true
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    isPast: Boolean = false
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = if (selected) Icons.Default.Check else icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = if (isPast)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = if (isPast)
                MaterialTheme.colorScheme.onSecondaryContainer
            else
                MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun DateRangeIndicator(
    startDate: LocalDate,
    endDate: LocalDate,
    sessionCount: Int,
    dateFormatter: DateTimeFormatter,
    isPast: Boolean
) {
    Surface(
        color = if (isPast)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPast) Icons.Default.History else Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (isPast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isPast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "$sessionCount ${if (sessionCount == 1) "session" else "sessions"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SessionsByDateList(
    groupedSessions: Map<LocalDate, List<StudySession>>,
    onSessionClick: (String) -> Unit,
    dateFormatter: DateTimeFormatter,
    isPast: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedSessions.entries.sortedBy { if (isPast) -it.key.toEpochDay() else it.key.toEpochDay() }.forEach { (date, sessions) ->
            item(key = "header_$date") {
                DateHeader(date = date, sessionCount = sessions.size, isPast = isPast)
            }

            items(sessions, key = { it.id }) { session ->
                SessionCard(
                    session = session,
                    onClick = { onSessionClick(session.id) }
                )
            }

            item(key = "spacer_$date") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate, sessionCount: Int, isPast: Boolean) {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val yesterday = today.minusDays(1)

    val dateLabel = when (date) {
        today -> stringResource(R.string.today)
        tomorrow -> stringResource(R.string.tomorrow)
        yesterday -> stringResource(R.string.yesterday)
        else -> {
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val dateStr = date.format(DateTimeFormatter.ofPattern("dd MMM"))
            "$dayName, $dateStr"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isPast)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (isPast)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "$sessionCount",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SessionCard(
    session: StudySession,
    onClick: () -> Unit
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time Block
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.15f),
                modifier = Modifier.size(60.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val timeStr = "${session.startTime}"
                    Text(
                        text = if (timeStr.length >= 5) timeStr.substring(0, 5) else timeStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "${session.durationMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.bookTitle ?: "Book",
                    style = MaterialTheme.typography.titleSmall,
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
                Spacer(modifier = Modifier.height(6.dp))

                // Pages info - stacked vertically to prevent breaking
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${session.plannedPages} pages planned",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    if (session.status == StudySessionStatus.COMPLETED && session.completedPages > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = StudyEngineTheme.extendedColors.success
                            )
                            Text(
                                text = "${session.completedPages} pages read",
                                style = MaterialTheme.typography.labelSmall,
                                color = StudyEngineTheme.extendedColors.success,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
    }
}

@Composable
private fun EmptySessionsState(isPast: Boolean) {
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
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPast) Icons.Default.History else Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = if (isPast)
                    stringResource(R.string.no_past_sessions)
                else
                    stringResource(R.string.no_upcoming_sessions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isPast)
                    stringResource(R.string.no_past_sessions_desc)
                else
                    stringResource(R.string.no_upcoming_sessions_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

