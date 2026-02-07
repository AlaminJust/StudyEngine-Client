package com.gatishil.studyengine.presentation.screens.exam

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.presentation.common.components.LoadingScreen
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(
    onNavigateToStartExam: (String) -> Unit,
    onNavigateToSelectSubjects: () -> Unit,
    onNavigateToContinueExam: () -> Unit,
    onNavigateToExamHistory: () -> Unit,
    onNavigateToExamResult: (String) -> Unit,
    viewModel: ExamListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showJoinDialog by remember { mutableStateOf<LiveExam?>(null) }

    // Navigate to take exam when live exam is joined
    LaunchedEffect(uiState.joinedExam) {
        if (uiState.joinedExam != null) {
            viewModel.clearJoinedExam()
            onNavigateToContinueExam()
        }
    }

    // Join confirmation dialog
    showJoinDialog?.let { liveExam ->
        AlertDialog(
            onDismissRequest = { showJoinDialog = null },
            title = { Text(stringResource(R.string.live_exam_join_confirm_title)) },
            text = {
                Text(stringResource(R.string.live_exam_join_confirm_message, liveExam.title))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.joinLiveExam(liveExam.id)
                        showJoinDialog = null
                    },
                    enabled = !uiState.isJoiningLiveExam
                ) {
                    if (uiState.isJoiningLiveExam) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.live_exam_join))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Join error snackbar
    uiState.joinError?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearJoinError()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            if (uiState.isLoading && uiState.subjects.isEmpty()) {
                LoadingScreen()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    item {
                        ExamHeader()
                    }

                    // Continue Exam Card (if there's an in-progress exam)
                    uiState.currentExam?.let { exam ->
                        item {
                            ContinueExamCard(
                                exam = exam,
                                onClick = onNavigateToContinueExam,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Live Exams Section
                    if (uiState.liveExams.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.live_exams),
                                icon = Icons.Filled.Sensors
                            )
                        }

                        items(uiState.liveExams) { liveExam ->
                            LiveExamListCard(
                                liveExam = liveExam,
                                isJoining = uiState.isJoiningLiveExam,
                                onJoin = {
                                    if (liveExam.hasAttempted) {
                                        onNavigateToContinueExam()
                                    } else {
                                        showJoinDialog = liveExam
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Subjects Section
                    item {
                        SectionHeader(
                            title = stringResource(R.string.exam_subjects),
                            icon = Icons.Outlined.Category,
                            actionLabel = stringResource(R.string.exam_multi_subject),
                            onAction = onNavigateToSelectSubjects
                        )
                    }

                    if (uiState.subjects.isEmpty() && uiState.categories.isEmpty()) {
                        item {
                            EmptySubjectsCard(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    } else {
                        item {
                            // Multi-subject exam card
                            MultiSubjectExamCard(
                                subjectCount = uiState.subjects.size,
                                totalQuestions = uiState.subjects.sumOf { it.questionCount },
                                onClick = onNavigateToSelectSubjects,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Show categories with their subjects if available
                        if (uiState.categories.isNotEmpty()) {
                            uiState.categories.forEach { category ->
                                if (category.subjects.isNotEmpty()) {
                                    item {
                                        CategoryHeader(
                                            category = category,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }

                                    item {
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(category.subjects) { subject ->
                                                SubjectCard(
                                                    subject = subject,
                                                    onClick = { onNavigateToStartExam(subject.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Fallback: show subjects without categories
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.subjects) { subject ->
                                        SubjectCard(
                                            subject = subject,
                                            onClick = { onNavigateToStartExam(subject.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recent Attempts Section
                    item {
                        SectionHeader(
                            title = stringResource(R.string.exam_recent_attempts),
                            icon = Icons.Outlined.History,
                            actionLabel = stringResource(R.string.view_all),
                            onAction = onNavigateToExamHistory
                        )
                    }

                    if (uiState.recentAttempts.isEmpty()) {
                        item {
                            EmptyHistoryCard(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    } else {
                        items(uiState.recentAttempts) { attempt ->
                            ExamAttemptCard(
                                attempt = attempt,
                                onClick = {
                                    if (attempt.status == ExamAttemptStatus.SUBMITTED) {
                                        onNavigateToExamResult(attempt.id)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Quick Stats Section
                    item {
                        QuickStatsSection(
                            totalAttempts = uiState.recentAttempts.size,
                            subjectsCount = uiState.subjects.size
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Quiz,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.exam_center),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.exam_center_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueExamCard(
    exam: ExamQuestionSet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectNames = exam.subjects.joinToString(", ") { it.name }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.exam_in_progress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = subjectNames,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.exam_questions_count, exam.totalQuestions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun MultiSubjectExamCard(
    subjectCount: Int,
    totalQuestions: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Layers,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.exam_multi_subject_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.exam_multi_subject_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.exam_subject_count, subjectCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.exam_question_count, totalQuestions),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = getSubjectGradient(subject.name)

    Card(
        modifier = modifier
            .width(160.dp)
            .height(140.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
        ) {
            // Subtle overlay for better text contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getSubjectIcon(subject.name),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Quiz,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.exam_question_count, subject.questionCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamAttemptCard(
    attempt: ExamAttemptSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score Circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (attempt.scorePercentage != null) {
                    Text(
                        text = "${attempt.scorePercentage.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
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
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.subjects.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = attempt.examTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status chip
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Date
                    Text(
                        text = attempt.startedAt.format(DateTimeFormatter.ofPattern("MMM dd")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
private fun EmptySubjectsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Category,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.exam_no_subjects),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.exam_no_subjects_desc),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.HistoryEdu,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.exam_no_history),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.exam_no_history_desc),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun QuickStatsSection(
    totalAttempts: Int,
    subjectsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.Outlined.Description,
            value = totalAttempts.toString(),
            label = stringResource(R.string.exam_stat_attempts),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Outlined.Category,
            value = subjectsCount.toString(),
            label = stringResource(R.string.exam_stat_subjects),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CategoryHeader(
    category: CategoryWithSubjects,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.name),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.exam_subject_count, category.subjects.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getCategoryIcon(categoryName: String): ImageVector {
    val name = categoryName.lowercase()
    return when {
        name.contains("bcs") -> Icons.Outlined.WorkspacePremium
        name.contains("hsc") -> Icons.Outlined.School
        name.contains("ssc") -> Icons.AutoMirrored.Outlined.MenuBook
        name.contains("admission") -> Icons.AutoMirrored.Outlined.Assignment
        name.contains("university") -> Icons.Outlined.AccountBalance
        name.contains("job") -> Icons.Outlined.Work
        name.contains("bank") -> Icons.Outlined.AccountBalance
        else -> Icons.Outlined.Category
    }
}

@Composable
private fun QuickStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper functions for subject styling
private fun getSubjectGradient(subjectName: String): List<Color> {
    val hash = subjectName.hashCode()
    // Professional, modern gradient colors that work well in both light and dark modes
    val gradients = listOf(
        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)), // Indigo to Purple
        listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4)), // Sky to Cyan
        listOf(Color(0xFF10B981), Color(0xFF34D399)), // Emerald
        listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)), // Amber
        listOf(Color(0xFFEF4444), Color(0xFFF87171)), // Red
        listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)), // Violet
        listOf(Color(0xFF14B8A6), Color(0xFF2DD4BF)), // Teal
        listOf(Color(0xFFEC4899), Color(0xFFF472B6))  // Pink
    )
    return gradients[kotlin.math.abs(hash) % gradients.size]
}

private fun getSubjectIcon(subjectName: String): ImageVector {
    val name = subjectName.lowercase()
    return when {
        name.contains("math") -> Icons.Outlined.Calculate
        name.contains("science") -> Icons.Outlined.Science
        name.contains("physics") -> Icons.Outlined.Speed
        name.contains("chemistry") -> Icons.Outlined.Biotech
        name.contains("biology") -> Icons.Outlined.Grass
        name.contains("history") -> Icons.Outlined.HistoryEdu
        name.contains("english") -> Icons.Outlined.Translate
        name.contains("geography") -> Icons.Outlined.Public
        name.contains("computer") -> Icons.Outlined.Computer
        name.contains("economics") -> Icons.Outlined.Analytics
        else -> Icons.Outlined.School
    }
}

@Composable
private fun LiveExamListCard(
    liveExam: LiveExam,
    isJoining: Boolean,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = liveExam.status == LiveExamStatus.ACTIVE
    val now = LocalDateTime.now()

    val statusColor = if (isActive) {
        StudyEngineTheme.extendedColors.success
    } else {
        MaterialTheme.colorScheme.primary
    }

    val timeText = if (isActive) {
        val remaining = Duration.between(now, liveExam.scheduledEndTime)
        val minutes = remaining.toMinutes()
        if (minutes > 60) "${minutes / 60}h ${minutes % 60}m remaining" else "${minutes}m remaining"
    } else {
        val until = Duration.between(now, liveExam.scheduledStartTime)
        val minutes = until.toMinutes()
        when {
            minutes < 0 -> ""
            minutes < 60 -> "Starts in ${minutes}m"
            minutes < 1440 -> "Starts in ${minutes / 60}h ${minutes % 60}m"
            else -> "Starts in ${minutes / 1440}d"
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                statusColor.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = if (isActive) {
            androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: status + time remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                        }
                        Text(
                            text = if (isActive) stringResource(R.string.live_exam_active)
                            else stringResource(R.string.live_exam_scheduled),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                if (timeText.isNotEmpty()) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = liveExam.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            liveExam.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.live_exam_questions, liveExam.questionCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                liveExam.timeLimitMinutes?.let { minutes ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.live_exam_time_limit, minutes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = liveExam.scheduledStartTime.format(timeFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Join button for active exams
            if (isActive) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isJoining,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = statusColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isJoining) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = if (liveExam.hasAttempted) Icons.Default.PlayArrow else Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (liveExam.hasAttempted) stringResource(R.string.exam_in_progress)
                        else stringResource(R.string.live_exam_join),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

