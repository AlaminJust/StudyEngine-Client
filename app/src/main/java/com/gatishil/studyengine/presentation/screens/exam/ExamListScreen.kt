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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(
    onNavigateToStartExam: (String) -> Unit,
    onNavigateToContinueExam: () -> Unit,
    onNavigateToExamHistory: () -> Unit,
    onNavigateToExamResult: (String) -> Unit,
    viewModel: ExamListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

                    // Subjects Section
                    item {
                        SectionHeader(
                            title = stringResource(R.string.exam_subjects),
                            icon = Icons.Outlined.Category
                        )
                    }

                    if (uiState.subjects.isEmpty()) {
                        item {
                            EmptySubjectsCard(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    } else {
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
                    text = exam.subjectName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
        ) {
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
                        .background(Color.White.copy(alpha = 0.2f)),
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
                    Text(
                        text = stringResource(R.string.exam_question_count, subject.questionCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
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
                    text = attempt.subjectName,
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
            icon = Icons.Outlined.Assignment,
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
    val gradients = listOf(
        listOf(Color(0xFF667eea), Color(0xFF764ba2)),
        listOf(Color(0xFF11998e), Color(0xFF38ef7d)),
        listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
        listOf(Color(0xFF4facfe), Color(0xFF00f2fe)),
        listOf(Color(0xFFfa709a), Color(0xFFfee140)),
        listOf(Color(0xFF30cfd0), Color(0xFF330867)),
        listOf(Color(0xFFa8edea), Color(0xFFfed6e3)),
        listOf(Color(0xFF5ee7df), Color(0xFFb490ca))
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
        name.contains("economics") -> Icons.Outlined.TrendingUp
        else -> Icons.Outlined.School
    }
}

