package com.gatishil.studyengine.presentation.screens.exam

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.QuestionDifficulty
import com.gatishil.studyengine.presentation.common.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartExamScreen(
    onNavigateBack: () -> Unit,
    onExamStarted: () -> Unit,
    viewModel: StartExamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate when exam starts
    LaunchedEffect(uiState.examStarted) {
        if (uiState.examStarted != null) {
            onExamStarted()
        }
    }

    // Show error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exam_start_title)) },
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
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Subjects Header
                if (uiState.subjects.isNotEmpty()) {
                    SubjectsHeader(
                        subjects = uiState.subjects,
                        totalQuestionCount = uiState.totalAvailableQuestionCount
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Configuration Section
                Text(
                    text = stringResource(R.string.exam_configure),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Question Count
                QuestionCountSelector(
                    count = uiState.questionCount,
                    maxCount = uiState.totalAvailableQuestionCount,
                    onCountChange = { viewModel.setQuestionCount(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty Selection
                DifficultySelector(
                    selectedDifficulty = uiState.selectedDifficulty,
                    onDifficultyChange = { viewModel.setDifficulty(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time Limit
                TimeLimitSelector(
                    timeLimit = uiState.timeLimitMinutes,
                    onTimeLimitChange = { viewModel.setTimeLimit(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Exam Info Summary
                ExamSummaryCard(
                    questionCount = uiState.questionCount,
                    difficulty = uiState.selectedDifficulty,
                    timeLimit = uiState.timeLimitMinutes,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Start Button
                Button(
                    onClick = { viewModel.startExam() },
                    enabled = !uiState.isStarting && uiState.questionCount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isStarting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.exam_start_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SubjectsHeader(
    subjects: List<com.gatishil.studyengine.domain.model.Subject>,
    totalQuestionCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Quiz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (subjects.size == 1) {
                    subjects.first().name
                } else {
                    stringResource(R.string.exam_multiple_subjects, subjects.size)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (subjects.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subjects.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            } else if (subjects.size == 1) {
                subjects.first().description?.let { desc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.exam_available_questions, totalQuestionCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun QuestionCountSelector(
    count: Int,
    maxCount: Int,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.QuestionMark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.exam_question_count_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onCountChange(count - 5) },
                    enabled = count > 5
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = { onCountChange(count + 5) },
                    enabled = count < maxCount
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            Slider(
                value = count.toFloat().coerceIn(5f, maxOf(5f, maxCount.toFloat())),
                onValueChange = { onCountChange(it.toInt()) },
                valueRange = 5f..maxOf(5f, maxCount.toFloat()),
                steps = maxOf(0, (maxCount / 5) - 1),
                modifier = Modifier.fillMaxWidth(),
                enabled = maxCount > 5
            )

            Text(
                text = stringResource(R.string.exam_max_questions, maxCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun DifficultySelector(
    selectedDifficulty: QuestionDifficulty?,
    onDifficultyChange: (QuestionDifficulty?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.exam_difficulty_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DifficultyChip(
                    text = stringResource(R.string.exam_difficulty_all),
                    color = MaterialTheme.colorScheme.outline,
                    selected = selectedDifficulty == null,
                    onClick = { onDifficultyChange(null) },
                    modifier = Modifier.weight(1f)
                )
                DifficultyChip(
                    text = stringResource(R.string.exam_difficulty_easy),
                    color = Color(0xFF4CAF50),
                    selected = selectedDifficulty == QuestionDifficulty.EASY,
                    onClick = { onDifficultyChange(QuestionDifficulty.EASY) },
                    modifier = Modifier.weight(1f)
                )
                DifficultyChip(
                    text = stringResource(R.string.exam_difficulty_medium),
                    color = Color(0xFFFF9800),
                    selected = selectedDifficulty == QuestionDifficulty.MEDIUM,
                    onClick = { onDifficultyChange(QuestionDifficulty.MEDIUM) },
                    modifier = Modifier.weight(1f)
                )
                DifficultyChip(
                    text = stringResource(R.string.exam_difficulty_hard),
                    color = Color(0xFFF44336),
                    selected = selectedDifficulty == QuestionDifficulty.HARD,
                    onClick = { onDifficultyChange(QuestionDifficulty.HARD) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DifficultyChip(
    text: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(2.dp, color, RoundedCornerShape(8.dp))
                else Modifier
            ),
        color = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        )
    }
}

@Composable
private fun TimeLimitSelector(
    timeLimit: Int?,
    onTimeLimitChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeOptions = listOf(null, 10, 15, 20, 30, 45, 60)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.exam_time_limit_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeOptions.take(4).forEach { time ->
                    TimeChip(
                        time = time,
                        selected = timeLimit == time,
                        onClick = { onTimeLimitChange(time) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeOptions.drop(4).forEach { time ->
                    TimeChip(
                        time = time,
                        selected = timeLimit == time,
                        onClick = { onTimeLimitChange(time) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TimeChip(
    time: Int?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                )
                else Modifier
            ),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = time?.let { stringResource(R.string.exam_time_minutes, it) }
                  ?: stringResource(R.string.exam_time_no_limit),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        )
    }
}

@Composable
private fun ExamSummaryCard(
    questionCount: Int,
    difficulty: QuestionDifficulty?,
    timeLimit: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.exam_summary),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Outlined.QuestionMark,
                    value = questionCount.toString(),
                    label = stringResource(R.string.exam_summary_questions)
                )

                SummaryItem(
                    icon = Icons.Outlined.Speed,
                    value = difficulty?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
                           ?: stringResource(R.string.exam_difficulty_all),
                    label = stringResource(R.string.exam_summary_difficulty)
                )

                SummaryItem(
                    icon = Icons.Outlined.Timer,
                    value = timeLimit?.let { "${it}m" } ?: "∞",
                    label = stringResource(R.string.exam_summary_time)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

