package com.gatishil.studyengine.presentation.screens.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.ExamAttemptStatus
import com.gatishil.studyengine.domain.model.ExamAttemptSummary
import com.gatishil.studyengine.presentation.common.components.LoadingScreen
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    viewModel: ExamHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Load more when reaching end
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != null &&
            lastVisibleItem.index >= uiState.attempts.size - 3 &&
            uiState.hasMore &&
            !uiState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exam_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters
            FilterSection(
                subjects = uiState.subjects,
                selectedSubjectId = uiState.selectedSubjectId,
                selectedStatus = uiState.selectedStatus,
                onSubjectChange = { viewModel.setSubjectFilter(it) },
                onStatusChange = { viewModel.setStatusFilter(it) }
            )

            if (uiState.isLoading) {
                LoadingScreen()
            } else if (uiState.attempts.isEmpty()) {
                EmptyHistory()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.attempts) { attempt ->
                        HistoryAttemptCard(
                            attempt = attempt,
                            onClick = {
                                if (attempt.status == ExamAttemptStatus.SUBMITTED) {
                                    onNavigateToResult(attempt.id)
                                }
                            }
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    subjects: List<com.gatishil.studyengine.domain.model.Subject>,
    selectedSubjectId: String?,
    selectedStatus: ExamAttemptStatus?,
    onSubjectChange: (String?) -> Unit,
    onStatusChange: (ExamAttemptStatus?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        // Subject filter
        Text(
            text = stringResource(R.string.exam_filter_subject),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedSubjectId == null,
                    onClick = { onSubjectChange(null) },
                    label = { Text(stringResource(R.string.exam_filter_all)) }
                )
            }
            items(subjects) { subject ->
                FilterChip(
                    selected = selectedSubjectId == subject.id,
                    onClick = { onSubjectChange(subject.id) },
                    label = { Text(subject.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status filter
        Text(
            text = stringResource(R.string.exam_filter_status),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { onStatusChange(null) },
                    label = { Text(stringResource(R.string.exam_filter_all)) }
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == ExamAttemptStatus.SUBMITTED,
                    onClick = { onStatusChange(ExamAttemptStatus.SUBMITTED) },
                    label = { Text(stringResource(R.string.exam_status_submitted)) }
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == ExamAttemptStatus.IN_PROGRESS,
                    onClick = { onStatusChange(ExamAttemptStatus.IN_PROGRESS) },
                    label = { Text(stringResource(R.string.exam_status_in_progress)) }
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == ExamAttemptStatus.TIMED_OUT,
                    onClick = { onStatusChange(ExamAttemptStatus.TIMED_OUT) },
                    label = { Text(stringResource(R.string.exam_status_timed_out)) }
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == ExamAttemptStatus.CANCELLED,
                    onClick = { onStatusChange(ExamAttemptStatus.CANCELLED) },
                    label = { Text(stringResource(R.string.exam_status_cancelled)) }
                )
            }
        }
    }
}

@Composable
private fun HistoryAttemptCard(
    attempt: ExamAttemptSummary,
    onClick: () -> Unit
) {
    val statusColor = when (attempt.status) {
        ExamAttemptStatus.SUBMITTED -> MaterialTheme.colorScheme.primary
        ExamAttemptStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
        ExamAttemptStatus.TIMED_OUT -> MaterialTheme.colorScheme.error
        ExamAttemptStatus.CANCELLED -> MaterialTheme.colorScheme.outline
    }

    val statusText = when (attempt.status) {
        ExamAttemptStatus.SUBMITTED -> stringResource(R.string.exam_status_submitted)
        ExamAttemptStatus.IN_PROGRESS -> stringResource(R.string.exam_status_in_progress)
        ExamAttemptStatus.TIMED_OUT -> stringResource(R.string.exam_status_timed_out)
        ExamAttemptStatus.CANCELLED -> stringResource(R.string.exam_status_cancelled)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (attempt.scorePercentage != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${attempt.scorePercentage.toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                } else {
                    Icon(
                        imageVector = when (attempt.status) {
                            ExamAttemptStatus.IN_PROGRESS -> Icons.Filled.Pending
                            ExamAttemptStatus.TIMED_OUT -> Icons.Filled.TimerOff
                            ExamAttemptStatus.CANCELLED -> Icons.Filled.Cancel
                            else -> Icons.Filled.Check
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = attempt.examTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Questions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${attempt.totalQuestions}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Points
                    if (attempt.earnedPoints != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Stars,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFB300)
                            )
                            Text(
                                text = "${attempt.earnedPoints}/${attempt.totalPoints}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = attempt.startedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (attempt.status == ExamAttemptStatus.SUBMITTED) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.HistoryEdu,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.exam_no_history),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.exam_no_history_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

