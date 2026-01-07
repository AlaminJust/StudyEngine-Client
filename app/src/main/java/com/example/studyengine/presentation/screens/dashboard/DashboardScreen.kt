package com.example.studyengine.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyengine.R
import com.example.studyengine.domain.model.Book
import com.example.studyengine.domain.model.StudySession
import com.example.studyengine.domain.model.StudySessionStatus
import com.example.studyengine.presentation.common.components.*
import com.example.studyengine.ui.theme.StudyEngineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToBooks: () -> Unit,
    onNavigateToBook: (String) -> Unit,
    onNavigateToSession: (String) -> Unit,
    onNavigateToAddBook: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddBook,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_book)
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.todaySessions.isEmpty()) {
                LoadingScreen()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stats Section
                    item {
                        StatsSection(
                            pagesReadToday = uiState.totalPagesReadToday,
                            upcomingSessionsCount = uiState.upcomingSessionsCount,
                            completedTodayCount = uiState.todayCompletedCount
                        )
                    }

                    // Today's Sessions Section
                    item {
                        SectionHeader(
                            title = stringResource(R.string.today_sessions),
                            actionLabel = stringResource(R.string.view_all),
                            onAction = { /* Navigate to all sessions */ }
                        )
                    }

                    if (uiState.todaySessions.isEmpty()) {
                        item {
                            EmptySessionCard()
                        }
                    } else {
                        items(uiState.todaySessions.take(3)) { session ->
                            SessionCard(
                                session = session,
                                onClick = { onNavigateToSession(session.id) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Recent Books Section
                    item {
                        SectionHeader(
                            title = stringResource(R.string.my_books),
                            actionLabel = stringResource(R.string.view_all),
                            onAction = onNavigateToBooks
                        )
                    }

                    if (uiState.recentBooks.isEmpty()) {
                        item {
                            EmptyBooksCard(onAddBook = onNavigateToAddBook)
                        }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.recentBooks) { book ->
                                    BookCard(
                                        book = book,
                                        onClick = { onNavigateToBook(book.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSection(
    pagesReadToday: Int,
    upcomingSessionsCount: Int,
    completedTodayCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = stringResource(R.string.pages_read_today),
            value = pagesReadToday.toString(),
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        )

        StatCard(
            title = stringResource(R.string.upcoming_sessions),
            value = upcomingSessionsCount.toString(),
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        )
    }
}

@Composable
private fun SessionCard(
    session: StudySession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (session.status) {
        StudySessionStatus.COMPLETED -> StudyEngineTheme.extendedColors.sessionCompleted
        StudySessionStatus.IN_PROGRESS -> StudyEngineTheme.extendedColors.sessionInProgress
        StudySessionStatus.MISSED -> StudyEngineTheme.extendedColors.sessionMissed
        StudySessionStatus.CANCELLED -> StudyEngineTheme.extendedColors.sessionCancelled
        else -> StudyEngineTheme.extendedColors.sessionPlanned
    }

    val statusLabel = when (session.status) {
        StudySessionStatus.COMPLETED -> stringResource(R.string.status_completed)
        StudySessionStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
        StudySessionStatus.MISSED -> stringResource(R.string.status_missed)
        StudySessionStatus.CANCELLED -> stringResource(R.string.status_cancelled)
        else -> stringResource(R.string.status_planned)
    }

    StudyCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.bookTitle ?: "Book",
                    style = MaterialTheme.typography.titleMedium
                )
                session.chapterTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${session.startTime} - ${session.endTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.planned_pages, session.plannedPages),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusChip(text = statusLabel, color = statusColor)
        }

        if (session.isCompleted || session.status == StudySessionStatus.IN_PROGRESS) {
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedProgressBar(
                progress = session.progressPercentage / 100f,
                color = statusColor
            )
        }
    }
}

@Composable
private fun BookCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Text(
                    text = book.subject,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                PriorityIndicator(priority = book.priority)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${book.effectiveTotalPages} pages",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptySessionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventAvailable,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_sessions_today),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyBooksCard(onAddBook: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LibraryBooks,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_books),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.add_your_first_book),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddBook) {
                Text(stringResource(R.string.add_book))
            }
        }
    }
}

