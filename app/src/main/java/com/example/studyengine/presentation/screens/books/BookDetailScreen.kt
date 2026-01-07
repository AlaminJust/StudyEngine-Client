package com.example.studyengine.presentation.screens.books

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
import com.example.studyengine.R
import com.example.studyengine.domain.model.Book
import com.example.studyengine.domain.model.Chapter
import com.example.studyengine.presentation.common.components.*
import com.example.studyengine.ui.theme.StudyEngineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCreatePlan: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
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
        }
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
                onCreatePlan = onNavigateToCreatePlan
            )
        }

        // Chapters Section
        item {
            SectionHeader(
                title = stringResource(R.string.chapters),
                actionLabel = stringResource(R.string.add_chapter),
                onAction = { /* TODO: Add chapter */ }
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
    onCreatePlan: () -> Unit
) {
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
                    color = StudyEngineTheme.extendedColors.success
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

