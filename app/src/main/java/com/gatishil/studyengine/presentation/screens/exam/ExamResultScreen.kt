package com.gatishil.studyengine.presentation.screens.exam

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.ExamAnswerResult
import com.gatishil.studyengine.domain.model.ExamResult
import com.gatishil.studyengine.presentation.common.components.LoadingScreen
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    onNavigateBack: () -> Unit,
    onRetakeExam: (String) -> Unit,
    viewModel: ExamResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exam_result_title)) },
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
        } else if (uiState.result != null) {
            val result = uiState.result!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Score Header
                item {
                    ScoreHeader(result = result)
                }

                // Stats Cards
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    StatsSection(result = result)
                }

                // Actions
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ActionsSection(
                        onRetake = { onRetakeExam(result.subjectId) },
                        onViewAnswers = { viewModel.toggleShowAllAnswers() },
                        showAnswers = uiState.showAllAnswers
                    )
                }

                // Answer Details
                if (uiState.showAllAnswers) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.exam_answer_review),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    itemsIndexed(result.answerResults) { index, answerResult ->
                        AnswerResultCard(
                            answerResult = answerResult,
                            questionNumber = index + 1,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        } else {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error ?: stringResource(R.string.error_loading),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreHeader(result: ExamResult) {
    val grade = result.grade
    val (gradeColor, gradeBg) = when {
        result.scorePercentage >= 90 -> Color(0xFF4CAF50) to Color(0xFFE8F5E9)
        result.scorePercentage >= 80 -> Color(0xFF8BC34A) to Color(0xFFF1F8E9)
        result.scorePercentage >= 70 -> Color(0xFFFFB300) to Color(0xFFFFF8E1)
        result.scorePercentage >= 60 -> Color(0xFFFF9800) to Color(0xFFFFF3E0)
        result.scorePercentage >= 50 -> Color(0xFFFF5722) to Color(0xFFFBE9E7)
        else -> Color(0xFFF44336) to Color(0xFFFFEBEE)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        gradeBg,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Grade Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(8.dp, gradeColor, CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor
                    )
                    Text(
                        text = "${result.scorePercentage.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = gradeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    result.scorePercentage >= 90 -> stringResource(R.string.exam_result_excellent)
                    result.scorePercentage >= 80 -> stringResource(R.string.exam_result_great)
                    result.scorePercentage >= 70 -> stringResource(R.string.exam_result_good)
                    result.scorePercentage >= 60 -> stringResource(R.string.exam_result_pass)
                    result.scorePercentage >= 50 -> stringResource(R.string.exam_result_needs_improvement)
                    else -> stringResource(R.string.exam_result_try_again)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = gradeColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.subjectName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.startedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsSection(result: ExamResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Outlined.CheckCircle,
            value = "${result.correctAnswers}/${result.totalQuestions}",
            label = stringResource(R.string.exam_stat_correct),
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Outlined.Stars,
            value = "${result.earnedPoints}/${result.totalPoints}",
            label = stringResource(R.string.exam_stat_points),
            color = Color(0xFFFFB300),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Outlined.Timer,
            value = result.duration,
            label = stringResource(R.string.exam_stat_time),
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Outlined.Quiz,
            value = result.answeredQuestions.toString(),
            label = stringResource(R.string.exam_stat_answered),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Outlined.Cancel,
            value = (result.totalQuestions - result.correctAnswers).toString(),
            label = stringResource(R.string.exam_stat_wrong),
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Outlined.HelpOutline,
            value = (result.totalQuestions - result.answeredQuestions).toString(),
            label = stringResource(R.string.exam_stat_skipped),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ActionsSection(
    onRetake: () -> Unit,
    onViewAnswers: () -> Unit,
    showAnswers: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onViewAnswers,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (showAnswers) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (showAnswers) stringResource(R.string.exam_hide_answers)
                       else stringResource(R.string.exam_view_answers)
            )
        }

        Button(
            onClick = onRetake,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.exam_retake))
        }
    }
}

@Composable
private fun AnswerResultCard(
    answerResult: ExamAnswerResult,
    questionNumber: Int,
    modifier: Modifier = Modifier
) {
    val isCorrect = answerResult.isCorrect
    val borderColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
    val bgColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = bgColor.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(borderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCorrect) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.exam_question_label, questionNumber),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${answerResult.pointsEarned}/${answerResult.maxPoints}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question text
            Text(
                text = answerResult.questionText,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Your answer
            if (answerResult.selectedOptionIds.isNotEmpty()) {
                Row {
                    Text(
                        text = stringResource(R.string.exam_your_answer),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Correct answer (if wrong)
            if (!isCorrect) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.exam_correct_answer),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Explanation
            answerResult.explanation?.let { explanation ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.exam_explanation),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

