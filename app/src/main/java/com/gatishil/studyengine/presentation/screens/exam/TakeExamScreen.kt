package com.gatishil.studyengine.presentation.screens.exam

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.ExamQuestion
import com.gatishil.studyengine.domain.model.QuestionDifficulty
import com.gatishil.studyengine.domain.model.QuestionOption
import com.gatishil.studyengine.presentation.common.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeExamScreen(
    onNavigateBack: () -> Unit,
    onExamCompleted: (String) -> Unit,
    viewModel: TakeExamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button
    BackHandler {
        viewModel.showExitConfirmation()
    }

    // Navigate to result when exam is completed
    LaunchedEffect(uiState.result) {
        uiState.result?.let { result ->
            onExamCompleted(result.examAttemptId)
        }
    }

    // Show error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Exit confirmation dialog
    if (uiState.showExitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideExitConfirmation() },
            title = { Text(stringResource(R.string.exam_exit_title)) },
            text = { Text(stringResource(R.string.exam_exit_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelExam()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.exam_exit_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideExitConfirmation() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Submit confirmation dialog
    if (uiState.showSubmitDialog) {
        val answeredCount = uiState.answers.count { it.value.isNotEmpty() }
        val totalCount = uiState.exam?.questions?.size ?: 0
        val unansweredCount = totalCount - answeredCount

        AlertDialog(
            onDismissRequest = { viewModel.hideSubmitConfirmation() },
            title = { Text(stringResource(R.string.exam_submit_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.exam_submit_message))
                    if (unansweredCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.exam_unanswered_warning, unansweredCount),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.submitExam() }) {
                    Text(stringResource(R.string.exam_submit_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideSubmitConfirmation() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ExamTopBar(
                subjectNames = uiState.exam?.subjects?.joinToString(", ") { it.name } ?: "",
                remainingSeconds = uiState.remainingSeconds,
                onExitClick = { viewModel.showExitConfirmation() }
            )
        },
        bottomBar = {
            uiState.exam?.let { exam ->
                ExamBottomBar(
                    currentIndex = uiState.currentQuestionIndex,
                    totalQuestions = exam.questions.size,
                    answeredQuestions = uiState.answers.count { it.value.isNotEmpty() },
                    onPrevious = { viewModel.previousQuestion() },
                    onNext = { viewModel.nextQuestion() },
                    onSubmit = { viewModel.showSubmitConfirmation() }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading || uiState.isSubmitting) {
            LoadingScreen()
        } else if (uiState.exam != null) {
            val exam = uiState.exam!!
            val currentQuestion = exam.questions.getOrNull(uiState.currentQuestionIndex)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Question Navigator
                QuestionNavigator(
                    questions = exam.questions,
                    currentIndex = uiState.currentQuestionIndex,
                    answers = uiState.answers,
                    onQuestionSelect = { viewModel.goToQuestion(it) }
                )

                // Question Content
                currentQuestion?.let { question ->
                    QuestionContent(
                        question = question,
                        questionNumber = uiState.currentQuestionIndex + 1,
                        totalQuestions = exam.questions.size,
                        selectedOptions = uiState.answers[question.id] ?: emptyList(),
                        onOptionSelect = { optionId ->
                            viewModel.selectOption(
                                question.id,
                                optionId,
                                question.allowMultipleCorrectAnswers
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamTopBar(
    subjectNames: String,
    remainingSeconds: Long?,
    onExitClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = subjectNames,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onExitClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.exam_exit_title)
                )
            }
        },
        actions = {
            remainingSeconds?.let { seconds ->
                val minutes = seconds / 60
                val secs = seconds % 60
                val isLowTime = seconds < 60

                Surface(
                    color = if (isLowTime) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isLowTime) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%02d:%02d", minutes, secs),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowTime) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun QuestionNavigator(
    questions: List<ExamQuestion>,
    currentIndex: Int,
    answers: Map<String, List<String>>,
    onQuestionSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(questions) { index, question ->
            val isAnswered = answers[question.id]?.isNotEmpty() == true
            val isCurrent = index == currentIndex

            QuestionIndicator(
                number = index + 1,
                isAnswered = isAnswered,
                isCurrent = isCurrent,
                onClick = { onQuestionSelect(index) }
            )
        }
    }
}

@Composable
private fun QuestionIndicator(
    number: Int,
    isAnswered: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isAnswered -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimary
        isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isAnswered && !isCurrent) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Composable
private fun QuestionContent(
    question: ExamQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    selectedOptions: List<String>,
    onOptionSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Question header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.exam_question_number, questionNumber, totalQuestions),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            DifficultyBadge(difficulty = question.difficulty)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Question text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(R.string.exam_points, question.points),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (question.allowMultipleCorrectAnswers) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.exam_multiple_answers),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options
        Text(
            text = stringResource(R.string.exam_select_answer),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        question.options.forEachIndexed { index, option ->
            OptionCard(
                option = option,
                optionLabel = ('A' + index).toString(),
                isSelected = option.id in selectedOptions,
                isMultiple = question.allowMultipleCorrectAnswers,
                onClick = { onOptionSelect(option.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: QuestionDifficulty) {
    val (text, color) = when (difficulty) {
        QuestionDifficulty.EASY -> stringResource(R.string.exam_difficulty_easy) to Color(0xFF4CAF50)
        QuestionDifficulty.MEDIUM -> stringResource(R.string.exam_difficulty_medium) to Color(0xFFFF9800)
        QuestionDifficulty.HARD -> stringResource(R.string.exam_difficulty_hard) to Color(0xFFF44336)
        QuestionDifficulty.EXPERT -> stringResource(R.string.exam_difficulty_expert) to Color(0xFF9C27B0)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun OptionCard(
    option: QuestionOption,
    optionLabel: String,
    isSelected: Boolean,
    isMultiple: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                          else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option label
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Option text
            Text(
                text = option.optionText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // Checkbox/Radio indicator
            if (isMultiple) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
            } else {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
            }
        }
    }
}

@Composable
private fun ExamBottomBar(
    currentIndex: Int,
    totalQuestions: Int,
    answeredQuestions: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Progress
            LinearProgressIndicator(
                progress = { answeredQuestions.toFloat() / totalQuestions },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.exam_answered_count, answeredQuestions, totalQuestions),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Previous button
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.exam_previous))
                }

                // Next/Submit button
                if (currentIndex == totalQuestions - 1) {
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.exam_submit))
                    }
                } else {
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.exam_next))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

