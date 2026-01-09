package com.gatishil.studyengine.presentation.screens.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
                onDelete = onDeleteStudyPlan
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
            items(book.chapters) { chapter ->
                ChapterItem(chapter)
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
    StudyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = book.subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.total_pages),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = book.effectiveTotalPages.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Column {
                Text(
                    text = stringResource(R.string.chapters),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = book.chapters.size.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriorityIndicator(priority = book.priority)
            DifficultyIndicator(difficulty = book.difficulty)
        }

        book.targetEndDate?.let { date ->
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Target: $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StudyPlanCard(
    book: Book,
    onCreatePlan: () -> Unit,
    onActivate: () -> Unit = {},
    onPause: () -> Unit = {},
    onComplete: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showActivateDialog by remember { mutableStateOf(false) }

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
                StatusChip(
                    text = plan.status.name,
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
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }
                        Button(
                            onClick = { showCompleteDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete")
                        }
                    }
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.PAUSED -> {
                        Button(
                            onClick = { showActivateDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                    com.gatishil.studyengine.domain.model.StudyPlanStatus.COMPLETED -> {
                        OutlinedButton(
                            onClick = onCreatePlan,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create New Plan")
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
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Plan")
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

@Composable
private fun ChapterItem(chapter: Chapter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.isIgnored) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.bodyLarge,
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
                Text(
                    text = "Pages ${chapter.startPage} - ${chapter.endPage} (${chapter.pageCount} pages)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { /* TODO: Chapter menu */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more)
                )
            }
        }
    }
}

