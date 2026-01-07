package com.example.studyengine.presentation.screens.sessions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyengine.R
import com.example.studyengine.domain.model.StudySession
import com.example.studyengine.domain.model.StudySessionStatus
import com.example.studyengine.presentation.common.components.*
import com.example.studyengine.ui.theme.StudyEngineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaySessionsScreen(
    onNavigateToSession: (String) -> Unit,
    viewModel: TodaySessionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.today_sessions)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                uiState.isLoading && uiState.sessions.isEmpty() -> {
                    LoadingScreen()
                }
                uiState.error != null && uiState.sessions.isEmpty() -> {
                    ErrorScreen(
                        message = uiState.error ?: stringResource(R.string.something_went_wrong),
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.sessions.isEmpty() -> {
                    EmptySessionsState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.sessions) { session ->
                            SessionListItem(
                                session = session,
                                onClick = { onNavigateToSession(session.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySessionsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EventAvailable,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.no_sessions_today),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionListItem(
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

    val statusLabel = when (session.status) {
        StudySessionStatus.COMPLETED -> stringResource(R.string.status_completed)
        StudySessionStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
        StudySessionStatus.MISSED -> stringResource(R.string.status_missed)
        StudySessionStatus.CANCELLED -> stringResource(R.string.status_cancelled)
        else -> stringResource(R.string.status_planned)
    }

    StudyCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.bookTitle ?: "Book",
                    style = MaterialTheme.typography.titleMedium
                )
                session.chapterTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            StatusChip(text = statusLabel, color = statusColor)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${session.startTime} - ${session.endTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${session.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.planned_pages, session.plannedPages),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.isCompleted) {
                    Text(
                        text = stringResource(R.string.completed_pages, session.completedPages),
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
            }
        }

        if (session.isCompleted || session.status == StudySessionStatus.IN_PROGRESS) {
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedProgressBar(
                progress = session.progressPercentage / 100f,
                color = statusColor
            )
        }
    }
}

