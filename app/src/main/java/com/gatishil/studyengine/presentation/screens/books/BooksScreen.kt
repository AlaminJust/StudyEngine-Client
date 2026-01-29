package com.gatishil.studyengine.presentation.screens.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.Book
import com.gatishil.studyengine.domain.model.StudyPlanStatus
import com.gatishil.studyengine.presentation.common.components.*
import com.gatishil.studyengine.ui.theme.StudyEngineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onNavigateToBook: (String) -> Unit,
    onNavigateToAddBook: () -> Unit,
    viewModel: BooksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_books)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddBook,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_book)) }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.books.isEmpty() -> {
                    LoadingScreen()
                }
                uiState.error != null && uiState.books.isEmpty() -> {
                    ErrorScreen(
                        message = uiState.error ?: stringResource(R.string.something_went_wrong),
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.books.isEmpty() -> {
                    EmptyBooksState(onAddBook = onNavigateToAddBook)
                }
                else -> {
                    BooksContent(
                        books = uiState.books,
                        onBookClick = onNavigateToBook
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyBooksState(onAddBook: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.no_books),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.add_your_first_book),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddBook,
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_book))
        }
    }
}

@Composable
private fun BooksContent(
    books: List<Book>,
    onBookClick: (String) -> Unit
) {
    val activeBooks = books.filter { it.studyPlan?.status == StudyPlanStatus.ACTIVE }
    val completedBooks = books.filter { it.studyPlan?.status == StudyPlanStatus.COMPLETED }
    val otherBooks = books.filter { it.studyPlan == null || it.studyPlan?.status == StudyPlanStatus.PAUSED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Stats Summary
        item {
            BooksSummaryCard(
                totalBooks = books.size,
                activeBooks = activeBooks.size,
                completedBooks = completedBooks.size,
                totalPages = books.sumOf { it.effectiveTotalPages }
            )
        }

        // Active Books Section
        if (activeBooks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.currently_reading),
                    count = activeBooks.size,
                    icon = Icons.Default.PlayCircle,
                    iconColor = StudyEngineTheme.extendedColors.success
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeBooks) { book ->
                        ActiveBookCard(
                            book = book,
                            onClick = { onBookClick(book.id) }
                        )
                    }
                }
            }
        }

        // All Books Grid
        item {
            SectionHeader(
                title = stringResource(R.string.all_books),
                count = books.size,
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconColor = MaterialTheme.colorScheme.primary
            )
        }

        items(books) { book ->
            BookListCard(
                book = book,
                onClick = { onBookClick(book.id) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun BooksSummaryCard(
    totalBooks: Int,
    activeBooks: Int,
    completedBooks: Int,
    totalPages: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(value = totalBooks.toString(), label = stringResource(R.string.total_books))
                VerticalDivider()
                SummaryItem(value = activeBooks.toString(), label = stringResource(R.string.active))
                VerticalDivider()
                SummaryItem(value = completedBooks.toString(), label = stringResource(R.string.completed_label))
                VerticalDivider()
                SummaryItem(value = totalPages.toString(), label = stringResource(R.string.pages))
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Color.White.copy(alpha = 0.3f))
    )
}

@Composable
private fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ActiveBookCard(
    book: Book,
    onClick: () -> Unit
) {
    // Use actual progress from API
    val progressPercentage = book.progressPercentage
    val progress = (progressPercentage / 100).toFloat().coerceIn(0f, 1f)
    val isCompleted = progressPercentage >= 100

    Card(
        onClick = onClick,
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Book icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getBookColor(book.subject),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted)
                        StudyEngineTheme.extendedColors.success.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = if (isCompleted)
                                StudyEngineTheme.extendedColors.success
                            else
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isCompleted)
                                stringResource(R.string.status_completed)
                            else
                                stringResource(R.string.active),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted)
                                StudyEngineTheme.extendedColors.success
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = book.subject,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pages completed info
            Text(
                text = "${book.completedPages}/${book.effectiveTotalPages} ${stringResource(R.string.pages).lowercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.progress),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted)
                            StudyEngineTheme.extendedColors.success
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCompleted)
                        StudyEngineTheme.extendedColors.success
                    else
                        MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BookListCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book icon with color
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getBookColor(book.subject),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = book.subject,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress bar if book has progress
                if (book.progressPercentage > 0 || book.studyPlan != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { (book.progressPercentage / 100).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (book.progressPercentage >= 100)
                                StudyEngineTheme.extendedColors.success
                            else
                                MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${book.progressPercentage.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (book.progressPercentage >= 100)
                                StudyEngineTheme.extendedColors.success
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pages info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (book.completedPages > 0)
                                "${book.completedPages}/${book.effectiveTotalPages}"
                            else
                                "${book.effectiveTotalPages}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Chapters
                    if (book.chapters.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${book.chapters.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Priority & Difficulty
                    PriorityChip(priority = book.priority)
                    DifficultyChip(difficulty = book.difficulty)
                }
            }

            // Status or arrow
            Column(
                horizontalAlignment = Alignment.End
            ) {
                book.studyPlan?.let { plan ->
                    StatusBadge(status = plan.status)
                } ?: Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PriorityChip(priority: Int) {
    val (color, text) = when (priority) {
        3 -> Pair(MaterialTheme.colorScheme.error, "H")      // HIGH
        2 -> Pair(MaterialTheme.colorScheme.tertiary, "M")    // MEDIUM
        else -> Pair(StudyEngineTheme.extendedColors.success, "L")  // LOW
    }

    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.size(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun DifficultyChip(difficulty: Int) {
    val (color, text) = when (difficulty) {
        3 -> Pair(MaterialTheme.colorScheme.error, "★★★")      // HARD
        2 -> Pair(MaterialTheme.colorScheme.tertiary, "★★")    // MEDIUM
        else -> Pair(StudyEngineTheme.extendedColors.success, "★")  // EASY
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}

@Composable
private fun StatusBadge(status: StudyPlanStatus) {
    val (color, icon, text) = when (status) {
        StudyPlanStatus.ACTIVE -> Triple(
            StudyEngineTheme.extendedColors.success,
            Icons.Default.PlayCircle,
            stringResource(R.string.active)
        )
        StudyPlanStatus.COMPLETED -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle,
            stringResource(R.string.completed_label)
        )
        StudyPlanStatus.PAUSED -> Triple(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.PauseCircle,
            stringResource(R.string.paused)
        )
        StudyPlanStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel,
            stringResource(R.string.cancelled)
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun getBookColor(subject: String): Color {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val colors = if (isDarkTheme) {
        // Dracula theme colors for dark mode
        listOf(
            MaterialTheme.colorScheme.primary,      // Purple
            MaterialTheme.colorScheme.secondary,    // Cyan
            MaterialTheme.colorScheme.tertiary,     // Orange
            StudyEngineTheme.extendedColors.success, // Green
            StudyEngineTheme.extendedColors.priorityHigh, // Red
            StudyEngineTheme.extendedColors.info,   // Cyan variant
            StudyEngineTheme.extendedColors.draculaPink,
            StudyEngineTheme.extendedColors.draculaYellow
        )
    } else {
        listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF8B5CF6), // Violet
            Color(0xFFEC4899), // Pink
            Color(0xFFF59E0B), // Amber
            Color(0xFF10B981), // Emerald
            Color(0xFF3B82F6), // Blue
            Color(0xFFEF4444), // Red
            Color(0xFF14B8A6)  // Teal
        )
    }
    return colors[subject.hashCode().mod(colors.size).let { if (it < 0) it + colors.size else it }]
}

