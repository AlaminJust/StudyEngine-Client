package com.example.studyengine.presentation.screens.sessions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyengine.R
import com.example.studyengine.domain.model.StudySession
import com.example.studyengine.domain.model.StudySessionStatus
import com.example.studyengine.presentation.common.components.*
import com.example.studyengine.ui.theme.StudyEngineTheme
import kotlinx.coroutines.flow.collectLatest

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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
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

    val statusLabel = when (session.status) {
        StudySessionStatus.COMPLETED -> stringResource(R.string.status_completed)
        StudySessionStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
        StudySessionStatus.MISSED -> stringResource(R.string.status_missed)
        StudySessionStatus.CANCELLED -> stringResource(R.string.status_cancelled)
        else -> stringResource(R.string.status_planned)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Session Info Card
        StudyCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.bookTitle ?: "Book",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    session.chapterTitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(text = statusLabel, color = statusColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = session.sessionDate.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${session.startTime} - ${session.endTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pages info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = "Planned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${session.plannedPages} pages",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (session.isCompleted) {
                    Column {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${session.completedPages} pages",
                            style = MaterialTheme.typography.titleLarge,
                            color = statusColor
                        )
                    }
                }
            }

            // Progress bar for completed/in-progress sessions
            if (session.isCompleted || session.status == StudySessionStatus.IN_PROGRESS) {
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedProgressBar(
                    progress = session.progressPercentage / 100f,
                    color = statusColor,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${session.progressPercentage.toInt()}% completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Actions based on status
        when (session.status) {
            StudySessionStatus.PLANNED -> {
                // Start session button
                Button(
                    onClick = onStartSession,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.start_session))
                    }
                }

                // Other actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onMarkAsMissed,
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    ) {
                        Text(stringResource(R.string.mark_as_missed))
                    }

                    OutlinedButton(
                        onClick = onCancelSession,
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    ) {
                        Text(stringResource(R.string.cancel_session))
                    }
                }
            }

            StudySessionStatus.IN_PROGRESS -> {
                // Complete session form
                StudyCard {
                    Text(
                        text = stringResource(R.string.complete_session),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = completedPages,
                        onValueChange = onCompletedPagesChange,
                        label = { Text(stringResource(R.string.enter_completed_pages)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isProcessing
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        label = { Text(stringResource(R.string.notes)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = !isProcessing
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onCompleteSession,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing && completedPages.isNotBlank()
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.complete_session))
                        }
                    }
                }
            }

            else -> {
                // Session is already completed, missed, or cancelled
                // Show read-only status
            }
        }
    }
}

