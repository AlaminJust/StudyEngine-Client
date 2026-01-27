package com.gatishil.studyengine.presentation.screens.books

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.domain.model.Book
import com.gatishil.studyengine.domain.model.Chapter
import com.gatishil.studyengine.presentation.common.components.*
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import com.gatishil.studyengine.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCreatePlan: () -> Unit,
    onNavigateToAddChapter: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    // Show error or success messages
    LaunchedEffect(uiState.error, uiState.successMessageResId) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // Handle success message separately since we need context
    uiState.successMessageResId?.let { resId ->
        val message = stringResource(resId)
        LaunchedEffect(resId) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.book?.title ?: stringResource(R.string.books))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                windowInsets = WindowInsets(0.dp),
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingScreen(modifier = Modifier.padding(paddingValues))
            }
            uiState.error != null -> {
                ErrorScreen(
                    message = uiState.error ?: stringResource(R.string.something_went_wrong),
                    onRetry = { viewModel.loadBook(bookId) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.book != null -> {
                BookDetailContent(
                    book = uiState.book!!,
                    onNavigateToCreatePlan = onNavigateToCreatePlan,
                    onAddChapter = onNavigateToAddChapter,
                    onActivateStudyPlan = {
                        uiState.book?.studyPlan?.let { plan ->
                            viewModel.activateStudyPlan(plan.id)
                        }
                    },
                    onPauseStudyPlan = {
                        uiState.book?.studyPlan?.let { plan ->
                            viewModel.pauseStudyPlan(plan.id)
                        }
                    },
                    onCompleteStudyPlan = {
                        uiState.book?.studyPlan?.let { plan ->
                            viewModel.completeStudyPlan(plan.id)
                        }
                    },
                    onDeleteStudyPlan = {
                        uiState.book?.studyPlan?.let { plan ->
                            viewModel.deleteStudyPlan(plan.id)
                        }
                    },
                    onEditStudyPlan = { startDate, endDate, recurrenceType, daysOfWeek ->
                        uiState.book?.studyPlan?.let { plan ->
                            val recurrenceRule = com.gatishil.studyengine.data.remote.dto.CreateRecurrenceRuleRequestDto(
                                type = recurrenceType,
                                interval = 1,
                                daysOfWeek = daysOfWeek
                            )
                            viewModel.updateStudyPlan(plan.id, startDate, endDate, recurrenceRule)
                        }
                    },
                    onEditChapter = { chapter, title, startPage, endPage, orderIndex ->
                        viewModel.updateChapter(bookId, chapter.id, title, startPage, endPage, orderIndex)
                    },
                    onDeleteChapter = { chapter ->
                        viewModel.deleteChapter(bookId, chapter.id)
                    },
                    onIgnoreChapter = { chapter ->
                        viewModel.ignoreChapter(bookId, chapter.id)
                    },
                    onUnignoreChapter = { chapter ->
                        viewModel.unignoreChapter(bookId, chapter.id)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text("Are you sure you want to delete this book?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteBook(bookId, onNavigateBack)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun BookDetailContent(
    book: Book,
    onNavigateToCreatePlan: () -> Unit,
    onAddChapter: () -> Unit,
    onActivateStudyPlan: () -> Unit = {},
    onPauseStudyPlan: () -> Unit = {},
    onCompleteStudyPlan: () -> Unit = {},
    onDeleteStudyPlan: () -> Unit = {},
    onEditStudyPlan: (startDate: String, endDate: String, recurrenceType: String, daysOfWeek: List<Int>) -> Unit = { _, _, _, _ -> },
    onEditChapter: (Chapter, title: String, startPage: Int, endPage: Int, orderIndex: Int) -> Unit = { _, _, _, _, _ -> },
    onDeleteChapter: (Chapter) -> Unit = {},
    onIgnoreChapter: (Chapter) -> Unit = {},
    onUnignoreChapter: (Chapter) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Book Info Card
        item {
            BookInfoCard(book)
        }

        // Study Plan Section
        item {
            StudyPlanCard(
                book = book,
                onCreatePlan = onNavigateToCreatePlan,
                onActivate = onActivateStudyPlan,
                onPause = onPauseStudyPlan,
                onComplete = onCompleteStudyPlan,
                onDelete = onDeleteStudyPlan,
                onEdit = onEditStudyPlan
            )
        }

        // Chapters Section
        item {
            SectionHeader(
                title = stringResource(R.string.chapters),
                actionLabel = stringResource(R.string.add_chapter),
                onAction = onAddChapter
            )
        }

        if (book.chapters.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No chapters added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsIndexed(book.chapters) { index, chapter ->
                ChapterItem(
                    chapter = chapter,
                    index = index,
                    onEdit = { title, startPage, endPage, orderIndex ->
                        onEditChapter(chapter, title, startPage, endPage, orderIndex)
                    },
                    onDelete = { onDeleteChapter(chapter) },
                    onIgnore = { onIgnoreChapter(chapter) },
                    onUnignore = { onUnignoreChapter(chapter) }
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BookInfoCard(book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    ) {
        // Colorful header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.subject,
                            style = MaterialTheme.typography.titleMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Book icon
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = book.effectiveTotalPages.toString(),
                        label = stringResource(R.string.pages)
                    )
                    StatItem(
                        value = book.chapters.size.toString(),
                        label = stringResource(R.string.chapters)
                    )
                    StatItem(
                        value = when(book.priority) {
                            3 -> "High"
                            2 -> "Medium"
                            else -> "Low"
                        },
                        label = stringResource(R.string.priority)
                    )
                    StatItem(
                        value = when(book.difficulty) {
                            3 -> "Hard"
                            2 -> "Medium"
                            else -> "Easy"
                        },
                        label = stringResource(R.string.difficulty)
                    )
                }
            }
        }

        // Target date if present
        book.targetEndDate?.let { date ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.target_date),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun StudyPlanCard(
    book: Book,
    onCreatePlan: () -> Unit,
    onActivate: () -> Unit = {},
    onPause: () -> Unit = {},
    onComplete: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEdit: (startDate: String, endDate: String, recurrenceType: String, daysOfWeek: List<Int>) -> Unit = { _, _, _, _ -> }
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showActivateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    StudyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.study_plan),
                style = MaterialTheme.typography.titleMedium
            )

            book.studyPlan?.let { plan ->
                val statusText = when (plan.status) {
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.ACTIVE -> stringResource(R.string.status_active)
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.PAUSED -> stringResource(R.string.status_paused)
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.COMPLETED -> stringResource(R.string.status_completed)
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.CANCELLED -> stringResource(R.string.status_cancelled)
                }
                StatusChip(
                    text = statusText,
                    color = when (plan.status) {
                        com.gatishil.studyengine.domain.model.StudyPlanStatus.ACTIVE -> StudyEngineTheme.extendedColors.success
                        com.gatishil.studyengine.domain.model.StudyPlanStatus.PAUSED -> MaterialTheme.colorScheme.tertiary
                        com.gatishil.studyengine.domain.model.StudyPlanStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (book.studyPlan != null) {
            val plan = book.studyPlan

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.start_date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = plan.startDate.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.end_date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = plan.endDate.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Recurrence info
            plan.recurrenceRule?.let { rule ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${rule.type.name.lowercase().replaceFirstChar { it.uppercase() }} • Every ${rule.interval} ${if (rule.interval == 1) "day" else "days"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action buttons based on status
            Spacer(modifier = Modifier.height(16.dp))

            // Edit button row (for ACTIVE and PAUSED states)
            if (plan.status == com.gatishil.studyengine.domain.model.StudyPlanStatus.ACTIVE ||
                plan.status == com.gatishil.studyengine.domain.model.StudyPlanStatus.PAUSED) {
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.edit_study_plan),
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (plan.status) {
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.ACTIVE -> {
                        OutlinedButton(
                            onClick = { showPauseDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.pause_plan),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Button(
                            onClick = { showCompleteDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.complete_plan),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.PAUSED -> {
                        Button(
                            onClick = { showActivateDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.resume_plan),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.delete_plan),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.COMPLETED -> {
                        OutlinedButton(
                            onClick = onCreatePlan,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.create_new_plan),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.delete_plan),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_study_plan),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.create_plan_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onCreatePlan) {
                    Text(stringResource(R.string.create_study_plan))
                }
            }
        }
    }

    // Pause Confirmation Dialog
    if (showPauseDialog) {
        StudyPlanActionDialog(
            title = stringResource(R.string.pause_study_plan),
            icon = Icons.Default.Pause,
            iconTint = MaterialTheme.colorScheme.tertiary,
            message = stringResource(R.string.pause_study_plan_message),
            consequences = listOf(
                stringResource(R.string.pause_study_plan_consequence_1),
                stringResource(R.string.pause_study_plan_consequence_2),
                stringResource(R.string.pause_study_plan_consequence_3),
                stringResource(R.string.pause_study_plan_consequence_4)
            ),
            confirmText = stringResource(R.string.pause_plan),
            confirmColor = MaterialTheme.colorScheme.tertiary,
            onConfirm = {
                onPause()
                showPauseDialog = false
            },
            onDismiss = { showPauseDialog = false }
        )
    }

    // Complete Confirmation Dialog
    if (showCompleteDialog) {
        StudyPlanActionDialog(
            title = stringResource(R.string.complete_study_plan),
            icon = Icons.Default.CheckCircle,
            iconTint = StudyEngineTheme.extendedColors.success,
            message = stringResource(R.string.complete_study_plan_message),
            consequences = listOf(
                stringResource(R.string.complete_study_plan_consequence_1),
                stringResource(R.string.complete_study_plan_consequence_2),
                stringResource(R.string.complete_study_plan_consequence_3),
                stringResource(R.string.complete_study_plan_consequence_4)
            ),
            confirmText = stringResource(R.string.complete_plan),
            confirmColor = StudyEngineTheme.extendedColors.success,
            onConfirm = {
                onComplete()
                showCompleteDialog = false
            },
            onDismiss = { showCompleteDialog = false }
        )
    }

    // Activate/Resume Confirmation Dialog
    if (showActivateDialog) {
        StudyPlanActionDialog(
            title = stringResource(R.string.resume_study_plan),
            icon = Icons.Default.PlayArrow,
            iconTint = StudyEngineTheme.extendedColors.success,
            message = stringResource(R.string.resume_study_plan_message),
            consequences = listOf(
                stringResource(R.string.resume_study_plan_consequence_1),
                stringResource(R.string.resume_study_plan_consequence_2),
                stringResource(R.string.resume_study_plan_consequence_3),
                stringResource(R.string.resume_study_plan_consequence_4)
            ),
            confirmText = stringResource(R.string.resume_plan),
            confirmColor = StudyEngineTheme.extendedColors.success,
            onConfirm = {
                onActivate()
                showActivateDialog = false
            },
            onDismiss = { showActivateDialog = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        StudyPlanActionDialog(
            title = stringResource(R.string.delete_study_plan),
            icon = Icons.Default.Delete,
            iconTint = MaterialTheme.colorScheme.error,
            message = stringResource(R.string.delete_study_plan_message),
            consequences = listOf(
                stringResource(R.string.delete_study_plan_consequence_1),
                stringResource(R.string.delete_study_plan_consequence_2),
                stringResource(R.string.delete_study_plan_consequence_3),
                stringResource(R.string.delete_study_plan_consequence_4)
            ),
            confirmText = stringResource(R.string.delete_plan),
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Edit Study Plan Dialog
    if (showEditDialog && book.studyPlan != null) {
        EditStudyPlanDialog(
            currentStartDate = book.studyPlan.startDate,
            currentEndDate = book.studyPlan.endDate,
            currentRecurrenceType = book.studyPlan.recurrenceRule?.type?.name?.lowercase()?.replaceFirstChar { it.uppercase() },
            currentDaysOfWeek = book.studyPlan.recurrenceRule?.daysOfWeek ?: emptyList(),
            onConfirm = { startDate, endDate, recurrenceType, daysOfWeek ->
                onEdit(startDate, endDate, recurrenceType, daysOfWeek)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
private fun StudyPlanActionDialog(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    message: String,
    consequences: List<String>,
    confirmText: String,
    confirmColor: androidx.compose.ui.graphics.Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.what_will_happen),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        consequences.forEach { consequence ->
                            Text(
                                text = consequence,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditStudyPlanDialog(
    currentStartDate: java.time.LocalDate,
    currentEndDate: java.time.LocalDate,
    currentRecurrenceType: String?,
    currentDaysOfWeek: List<java.time.DayOfWeek>,
    onConfirm: (startDate: String, endDate: String, recurrenceType: String, daysOfWeek: List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var startDate by remember { mutableStateOf(currentStartDate) }
    var endDate by remember { mutableStateOf(currentEndDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var recurrenceType by remember { mutableStateOf(currentRecurrenceType ?: "Daily") }
    var selectedDaysOfWeek by remember { mutableStateOf(currentDaysOfWeek.map { it.value }.toSet()) }
    var expanded by remember { mutableStateOf(false) }

    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")

    val recurrenceOptions = listOf("Daily", "Weekly", "Custom")
    val daysOfWeekOptions = listOf(
        1 to stringResource(R.string.monday),
        2 to stringResource(R.string.tuesday),
        3 to stringResource(R.string.wednesday),
        4 to stringResource(R.string.thursday),
        5 to stringResource(R.string.friday),
        6 to stringResource(R.string.saturday),
        7 to stringResource(R.string.sunday)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.edit_study_plan),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start Date
                OutlinedCard(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.start_date),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = startDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // End Date
                OutlinedCard(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.end_date),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = endDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Recurrence Type
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = recurrenceType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.recurrence_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        recurrenceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    recurrenceType = option
                                    expanded = false
                                    // Reset days selection when switching recurrence type
                                    if (option == "Daily") {
                                        selectedDaysOfWeek = (1..7).toSet()
                                    }
                                }
                            )
                        }
                    }
                }

                // Days of Week selection (only for Weekly or Custom)
                if (recurrenceType == "Weekly" || recurrenceType == "Custom") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.select_days),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            daysOfWeekOptions.forEach { (dayValue, dayName) ->
                                FilterChip(
                                    selected = selectedDaysOfWeek.contains(dayValue),
                                    onClick = {
                                        selectedDaysOfWeek = if (selectedDaysOfWeek.contains(dayValue)) {
                                            selectedDaysOfWeek - dayValue
                                        } else {
                                            selectedDaysOfWeek + dayValue
                                        }
                                    },
                                    label = { Text(dayName.take(3)) }
                                )
                            }
                        }
                    }
                }

                // Validation message
                if (endDate <= startDate) {
                    Text(
                        text = "End date must be after start date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysToSend = if (recurrenceType == "Daily") {
                        (1..7).toList()
                    } else {
                        selectedDaysOfWeek.toList().sorted()
                    }
                    onConfirm(
                        startDate.format(java.time.format.DateTimeFormatter.ISO_DATE),
                        endDate.format(java.time.format.DateTimeFormatter.ISO_DATE),
                        recurrenceType,
                        daysToSend
                    )
                },
                enabled = endDate > startDate && (recurrenceType == "Daily" || selectedDaysOfWeek.isNotEmpty())
            ) {
                Text(stringResource(R.string.update_plan))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    // Start Date Picker
    if (showStartDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = startDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        startDate = java.time.LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                    }
                    showStartDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { DatePicker(state = state) }
    }

    // End Date Picker
    if (showEndDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = endDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        endDate = java.time.LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                    }
                    showEndDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun ChapterItem(
    chapter: Chapter,
    index: Int = 0,
    onEdit: (title: String, startPage: Int, endPage: Int, orderIndex: Int) -> Unit = { _, _, _, _ -> },
    onDelete: () -> Unit = {},
    onIgnore: () -> Unit = {},
    onUnignore: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showIgnoreDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Colorful chapter colors based on index
    val chapterColors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Violet
        Color(0xFFEC4899), // Pink
        Color(0xFFF97316), // Orange
        Color(0xFF14B8A6), // Teal
        Color(0xFF22C55E), // Green
        Color(0xFF3B82F6), // Blue
        Color(0xFFA855F7), // Purple
        Color(0xFFEF4444), // Red
        Color(0xFF06B6D4)  // Cyan
    )
    val chapterColor = chapterColors[index % chapterColors.size]

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.isIgnored) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                chapterColor.copy(alpha = 0.08f)
            }
        ),
        border = if (!chapter.isIgnored) {
            BorderStroke(1.dp, chapterColor.copy(alpha = 0.3f))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chapter number badge
            Surface(
                shape = CircleShape,
                color = if (chapter.isIgnored) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    chapterColor
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (chapter.isIgnored) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color.White
                        }
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (chapter.isIgnored) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (chapter.isIgnored) {
                        StatusChip(
                            text = stringResource(R.string.ignored),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (chapter.isIgnored) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            } else {
                                chapterColor
                            }
                        )
                        Text(
                            text = "${chapter.pageCount} pages",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "p.${chapter.startPage} - ${chapter.endPage}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more),
                        tint = if (chapter.isIgnored) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            chapterColor
                        }
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            showMenu = false
                            showEditDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )

                    if (chapter.isIgnored) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.unignore)) },
                            onClick = {
                                showMenu = false
                                onUnignore()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ignore)) },
                            onClick = {
                                showMenu = false
                                showIgnoreDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.VisibilityOff, contentDescription = null)
                            }
                        )
                    }

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.delete_chapter)) },
            text = {
                Text(stringResource(R.string.delete_chapter_message, chapter.title))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Ignore Confirmation Dialog
    if (showIgnoreDialog) {
        AlertDialog(
            onDismissRequest = { showIgnoreDialog = false },
            icon = {
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            },
            title = { Text(stringResource(R.string.ignore_chapter)) },
            text = {
                Text(stringResource(R.string.ignore_chapter_message, chapter.title))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onIgnore()
                        showIgnoreDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ignore))
                }
            },
            dismissButton = {
                TextButton(onClick = { showIgnoreDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Edit Chapter Dialog
    if (showEditDialog) {
        EditChapterDialog(
            chapter = chapter,
            onConfirm = { title, startPage, endPage, orderIndex ->
                onEdit(title, startPage, endPage, orderIndex)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
private fun EditChapterDialog(
    chapter: Chapter,
    onConfirm: (title: String, startPage: Int, endPage: Int, orderIndex: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(chapter.title) }
    var startPage by remember { mutableStateOf(chapter.startPage.toString()) }
    var endPage by remember { mutableStateOf(chapter.endPage.toString()) }

    val isValid = title.isNotBlank() &&
                  startPage.toIntOrNull() != null &&
                  endPage.toIntOrNull() != null &&
                  (startPage.toIntOrNull() ?: 0) <= (endPage.toIntOrNull() ?: 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text(stringResource(R.string.edit_chapter)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.chapter_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startPage,
                        onValueChange = { startPage = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.start_page)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )

                    OutlinedTextField(
                        value = endPage,
                        onValueChange = { endPage = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.end_page)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }

                if (!isValid && title.isNotBlank()) {
                    Text(
                        text = "Please enter valid page numbers (start <= end)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title,
                        startPage.toIntOrNull() ?: chapter.startPage,
                        endPage.toIntOrNull() ?: chapter.endPage,
                        chapter.orderIndex
                    )
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

