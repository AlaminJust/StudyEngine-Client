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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
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
import com.gatishil.studyengine.domain.model.Tag
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

                Spacer(modifier = Modifier.height(16.dp))

                // Chapter Selection Section (if subjects have chapters)
                if (uiState.subjectsWithChapters.any { it.chapters.isNotEmpty() }) {
                    Text(
                        text = stringResource(R.string.exam_select_chapters),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.exam_select_chapters_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.subjectsWithChapters.forEach { swc ->
                        if (swc.chapters.isNotEmpty()) {
                            SubjectChapterSelector(
                                subjectWithChapterSelection = swc,
                                onToggleExpanded = { viewModel.toggleSubjectExpanded(swc.subject.id) },
                                onToggleChapter = { chapterId -> viewModel.toggleChapterSelection(swc.subject.id, chapterId) },
                                onSelectAll = { viewModel.selectAllChapters(swc.subject.id) },
                                onDeselectAll = { viewModel.deselectAllChapters(swc.subject.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tag Selection Section (if tags are available)
                if (uiState.availableTags.isNotEmpty()) {
                    TagSelector(
                        availableTags = uiState.availableTags,
                        selectedTagIds = uiState.selectedTagIds,
                        isExpanded = uiState.isTagSectionExpanded,
                        onToggleExpanded = { viewModel.toggleTagSectionExpanded() },
                        onToggleTag = { tagId -> viewModel.toggleTagSelection(tagId) },
                        onClearAll = { viewModel.clearAllTags() },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                    selectedTagCount = uiState.selectedTagIds.size,
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
                    imageVector = Icons.Outlined.Quiz,
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
    selectedTagCount: Int = 0,
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
                    icon = Icons.Outlined.Quiz,
                    value = questionCount.toString(),
                    label = stringResource(R.string.exam_summary_questions)
                )

                val difficultyText = when (difficulty) {
                    QuestionDifficulty.EASY -> stringResource(R.string.exam_difficulty_easy)
                    QuestionDifficulty.MEDIUM -> stringResource(R.string.exam_difficulty_medium)
                    QuestionDifficulty.HARD -> stringResource(R.string.exam_difficulty_hard)
                    QuestionDifficulty.EXPERT -> stringResource(R.string.exam_difficulty_expert)
                    null -> stringResource(R.string.exam_difficulty_all)
                }
                SummaryItem(
                    icon = Icons.Outlined.Speed,
                    value = difficultyText,
                    label = stringResource(R.string.exam_summary_difficulty)
                )

                SummaryItem(
                    icon = Icons.Outlined.Timer,
                    value = timeLimit?.let { stringResource(R.string.exam_time_minutes, it) }
                           ?: stringResource(R.string.exam_time_no_limit),
                    label = stringResource(R.string.exam_summary_time)
                )

                if (selectedTagCount > 0) {
                    SummaryItem(
                        icon = Icons.Outlined.Label,
                        value = selectedTagCount.toString(),
                        label = stringResource(R.string.exam_summary_tags)
                    )
                }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagSelector(
    availableTags: List<Tag>,
    selectedTagIds: Set<String>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleTag: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Header (clickable to expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.exam_tags_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (selectedTagIds.isEmpty()) {
                            stringResource(R.string.exam_tags_hint)
                        } else {
                            stringResource(R.string.exam_tags_selected, selectedTagIds.size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Selected tags preview (when collapsed and tags are selected)
            if (!isExpanded && selectedTagIds.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableTags.filter { it.id in selectedTagIds }.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.exam_tag_remove, tag.name),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onToggleTag(tag.id) },
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Expanded tag list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    // Clear All button
                    if (selectedTagIds.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onClearAll) {
                                Text(stringResource(R.string.clear))
                            }
                        }
                    }

                    // Tag chips in flow layout
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableTags.forEach { tag ->
                            val isSelected = tag.id in selectedTagIds
                            TagChip(
                                tag = tag,
                                isSelected = isSelected,
                                onClick = { onToggleTag(tag.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TagChip(
    tag: Tag,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = MaterialTheme.colorScheme.tertiary

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, tagColor, RoundedCornerShape(20.dp))
                else Modifier
            ),
        color = if (isSelected) tagColor.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = tagColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) tagColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (tag.usageCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${tag.usageCount})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SubjectChapterSelector(
    subjectWithChapterSelection: SubjectWithChapterSelection,
    onToggleExpanded: () -> Unit,
    onToggleChapter: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swc = subjectWithChapterSelection
    val selectedCount = swc.selectedChapterIds.size

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Subject Header (clickable to expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Subject Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = swc.subject.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (selectedCount == 0) {
                            stringResource(R.string.exam_all_chapters, swc.chapters.size)
                        } else {
                            stringResource(R.string.exam_chapters_selected, selectedCount, swc.chapters.size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Expand/Collapse Icon
                Icon(
                    imageVector = if (swc.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (swc.isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chapter List (animated)
            AnimatedVisibility(
                visible = swc.isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    // Select All / Deselect All buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onSelectAll) {
                            Text(stringResource(R.string.select_all))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onDeselectAll) {
                            Text(stringResource(R.string.clear))
                        }
                    }

                    // Chapters
                    swc.chapters.forEach { chapter ->
                        val isSelected = chapter.id in swc.selectedChapterIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleChapter(chapter.id) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleChapter(chapter.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                                Text(
                                    text = stringResource(R.string.exam_question_count, chapter.questionCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

