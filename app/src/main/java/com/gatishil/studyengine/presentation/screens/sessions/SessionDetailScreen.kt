package com.gatishil.studyengine.presentation.screens.sessions

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.StudySession
import com.gatishil.studyengine.domain.model.StudySessionStatus
import com.gatishil.studyengine.presentation.common.components.*
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SessionEvent.SessionCompleted -> {
                    snackbarHostState.showSnackbar("Session completed!")
                }
                is SessionEvent.SessionUpdated -> {
                    snackbarHostState.showSnackbar("Session updated")
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.session_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.session == null -> {
                LoadingScreen(modifier = Modifier.padding(paddingValues))
            }
            uiState.session != null -> {
                SessionDetailContent(
                    session = uiState.session!!,
                    completedPages = uiState.completedPages,
                    notes = uiState.notes,
                    isProcessing = uiState.isCompleting,
                    onCompletedPagesChange = viewModel::updateCompletedPages,
                    onNotesChange = viewModel::updateNotes,
                    onStartSession = viewModel::startSession,
                    onCompleteSession = viewModel::completeSession,
                    onMarkAsMissed = viewModel::markAsMissed,
                    onCancelSession = viewModel::cancelSession,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SessionDetailContent(
    session: StudySession,
    completedPages: String,
    notes: String,
    isProcessing: Boolean,
    onCompletedPagesChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onStartSession: () -> Unit,
    onCompleteSession: () -> Unit,
    onMarkAsMissed: () -> Unit,
    onCancelSession: () -> Unit,
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

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        SessionHeroCard(
            session = session,
            statusColor = statusColor,
            statusIcon = statusIcon,
            statusLabel = statusLabel,
            dateFormatter = dateFormatter
        )

        // Quick Stats
        QuickStatsSection(session = session, statusColor = statusColor)

        // Progress Section (for completed or in-progress)
        if (session.isCompleted || session.status == StudySessionStatus.IN_PROGRESS) {
            ProgressSection(session = session, statusColor = statusColor)
        }

        // Actions based on status
        ActionsSection(
            session = session,
            completedPages = completedPages,
            notes = notes,
            isProcessing = isProcessing,
            onCompletedPagesChange = onCompletedPagesChange,
            onNotesChange = onNotesChange,
            onStartSession = onStartSession,
            onCompleteSession = onCompleteSession,
            onMarkAsMissed = onMarkAsMissed,
            onCancelSession = onCancelSession
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SessionHeroCard(
    session: StudySession,
    statusColor: Color,
    statusIcon: ImageVector,
    statusLabel: String,
    dateFormatter: DateTimeFormatter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.9f),
                            statusColor.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Book Title
                Text(
                    text = session.bookTitle ?: "Book",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                session.chapterTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Date and Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = session.sessionDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }

                    // Time
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.time),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            val startTime = "${session.startTime}".let { if (it.length >= 5) it.substring(0, 5) else it }
                            val endTime = "${session.endTime}".let { if (it.length >= 5) it.substring(0, 5) else it }
                            Text(
                                text = "$startTime - $endTime",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsSection(session: StudySession, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Default.Timer,
            value = "${session.durationMinutes}",
            label = stringResource(R.string.minutes),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            value = "${session.plannedPages}",
            label = stringResource(R.string.planned),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        if (session.completedPages > 0 || session.isCompleted) {
            StatCard(
                icon = Icons.Default.Done,
                value = "${session.completedPages}",
                label = stringResource(R.string.read),
                color = statusColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ProgressSection(session: StudySession, statusColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.progress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${session.progressPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { session.progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${session.completedPages} / ${session.plannedPages} pages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudyEngineTheme.extendedColors.success.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(R.string.status_completed),
                            style = MaterialTheme.typography.labelSmall,
                            color = StudyEngineTheme.extendedColors.success,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsSection(
    session: StudySession,
    completedPages: String,
    notes: String,
    isProcessing: Boolean,
    onCompletedPagesChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onStartSession: () -> Unit,
    onCompleteSession: () -> Unit,
    onMarkAsMissed: () -> Unit,
    onCancelSession: () -> Unit
) {
    when (session.status) {
        StudySessionStatus.PLANNED -> {
            PlannedSessionActions(
                isProcessing = isProcessing,
                onStartSession = onStartSession,
                onMarkAsMissed = onMarkAsMissed,
                onCancelSession = onCancelSession
            )
        }
        StudySessionStatus.IN_PROGRESS -> {
            InProgressSessionActions(
                completedPages = completedPages,
                notes = notes,
                isProcessing = isProcessing,
                plannedPages = session.plannedPages,
                onCompletedPagesChange = onCompletedPagesChange,
                onNotesChange = onNotesChange,
                onCompleteSession = onCompleteSession
            )
        }
        else -> {
            // Completed, Missed, or Cancelled - show read-only status
            CompletedSessionInfo(session = session)
        }
    }
}

@Composable
private fun PlannedSessionActions(
    isProcessing: Boolean,
    onStartSession: () -> Unit,
    onMarkAsMissed: () -> Unit,
    onCancelSession: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Start Session Button
        Button(
            onClick = onStartSession,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isProcessing,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.start_session),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Secondary Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onMarkAsMissed,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = StudyEngineTheme.extendedColors.sessionMissed
                )
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.mark_as_missed))
            }

            OutlinedButton(
                onClick = onCancelSession,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.cancel_session))
            }
        }
    }
}

@Composable
private fun InProgressSessionActions(
    completedPages: String,
    notes: String,
    isProcessing: Boolean,
    plannedPages: Int,
    onCompletedPagesChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCompleteSession: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = StudyEngineTheme.extendedColors.sessionInProgress.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = StudyEngineTheme.extendedColors.sessionInProgress,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.complete_session),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = completedPages,
                onValueChange = onCompletedPagesChange,
                label = { Text(stringResource(R.string.enter_completed_pages)) },
                placeholder = { Text("e.g., $plannedPages") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                }
            )

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text(stringResource(R.string.notes)) },
                placeholder = { Text(stringResource(R.string.add_notes_optional)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Notes, contentDescription = null)
                }
            )

            Button(
                onClick = onCompleteSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isProcessing && completedPages.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudyEngineTheme.extendedColors.success
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.complete_session),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedSessionInfo(session: StudySession) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (icon, message, color) = when (session.status) {
                StudySessionStatus.COMPLETED -> Triple(
                    Icons.Default.CheckCircle,
                    stringResource(R.string.session_completed_message),
                    StudyEngineTheme.extendedColors.success
                )
                StudySessionStatus.MISSED -> Triple(
                    Icons.Default.ErrorOutline,
                    stringResource(R.string.session_missed_message),
                    StudyEngineTheme.extendedColors.sessionMissed
                )
                else -> Triple(
                    Icons.Default.Cancel,
                    stringResource(R.string.session_cancelled_message),
                    StudyEngineTheme.extendedColors.sessionCancelled
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

