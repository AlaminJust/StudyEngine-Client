package com.gatishil.studyengine.presentation.screens.exam

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.gatishil.studyengine.domain.model.CategoryWithSubjects
import com.gatishil.studyengine.domain.model.Subject
import com.gatishil.studyengine.presentation.common.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSubjectsScreen(
    onNavigateBack: () -> Unit,
    onSubjectsSelected: (List<Pair<String, List<String>?>>) -> Unit,
    viewModel: SelectSubjectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = { Text(stringResource(R.string.exam_select_subjects)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (uiState.selectedSubjectIds.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearSelection() }) {
                            Text(stringResource(R.string.clear))
                        }
                    }
                    if (uiState.subjects.isNotEmpty() && uiState.selectedSubjectIds.size < uiState.subjects.size) {
                        TextButton(onClick = { viewModel.selectAllSubjects() }) {
                            Text(stringResource(R.string.select_all))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Selection summary
                    AnimatedVisibility(
                        visible = uiState.selectedSubjectIds.isNotEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.exam_subjects_selected,
                                    uiState.selectedSubjectIds.size
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(
                                    R.string.exam_total_questions_available,
                                    viewModel.getTotalQuestionCount()
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Continue Button
                    Button(
                        onClick = {
                            onSubjectsSelected(viewModel.getSelectedSubjectsWithChapters())
                        },
                        enabled = uiState.selectedSubjectIds.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.exam_continue_to_config),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
            if (uiState.isLoading && uiState.subjects.isEmpty() && uiState.categoryItems.isEmpty()) {
                LoadingScreen()
            } else if (uiState.subjects.isEmpty() && uiState.categoryItems.isEmpty()) {
                EmptySubjectsMessage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    item {
                        SelectSubjectsHeader()
                    }

                    // Display categories with subjects
                    if (uiState.categoryItems.isNotEmpty()) {
                        uiState.categoryItems.forEach { categoryItem ->
                            if (categoryItem.subjectItems.isNotEmpty()) {
                                item(key = "category_${categoryItem.category.id}") {
                                    CategorySection(
                                        category = categoryItem.category,
                                        isExpanded = categoryItem.isExpanded,
                                        onToggleExpand = { viewModel.toggleCategoryExpanded(categoryItem.category.id) },
                                        selectedCount = viewModel.getSelectedCountInCategory(categoryItem.category.id),
                                        isAllSelected = viewModel.isAllSelectedInCategory(categoryItem.category.id),
                                        onSelectAll = { viewModel.selectAllSubjectsInCategory(categoryItem.category.id) },
                                        onDeselectAll = { viewModel.deselectAllSubjectsInCategory(categoryItem.category.id) }
                                    )
                                }

                                if (categoryItem.isExpanded) {
                                    items(
                                        items = categoryItem.subjectItems,
                                        key = { "subject_${it.subject.id}" }
                                    ) { subjectItem ->
                                        SelectableSubjectWithChaptersCard(
                                            subjectItem = subjectItem,
                                            onToggleSelection = { viewModel.toggleSubjectSelection(subjectItem.subject.id) },
                                            onToggleExpanded = { viewModel.toggleSubjectExpanded(subjectItem.subject.id) },
                                            onToggleChapter = { chapterId -> viewModel.toggleChapterSelection(subjectItem.subject.id, chapterId) },
                                            onSelectAllChapters = { viewModel.selectAllChapters(subjectItem.subject.id) },
                                            onDeselectAllChapters = { viewModel.deselectAllChapters(subjectItem.subject.id) },
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Fallback: show subjects without categories
                        items(
                            items = uiState.subjectItems,
                            key = { "subject_${it.subject.id}" }
                        ) { subjectItem ->
                            SelectableSubjectWithChaptersCard(
                                subjectItem = subjectItem,
                                onToggleSelection = { viewModel.toggleSubjectSelection(subjectItem.subject.id) },
                                onToggleExpanded = { viewModel.toggleSubjectExpanded(subjectItem.subject.id) },
                                onToggleChapter = { chapterId -> viewModel.toggleChapterSelection(subjectItem.subject.id, chapterId) },
                                onSelectAllChapters = { viewModel.selectAllChapters(subjectItem.subject.id) },
                                onDeselectAllChapters = { viewModel.deselectAllChapters(subjectItem.subject.id) }
                            )
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectSubjectsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.exam_choose_subjects),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.exam_choose_subjects_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptySubjectsMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Category,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.exam_no_subjects),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.exam_no_subjects_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CategorySection(
    category: CategoryWithSubjects,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedCount: Int,
    isAllSelected: Boolean,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.name),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Category Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.exam_subject_count, category.subjects.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedCount > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "$selectedCount selected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Select/Deselect All Button
                IconButton(
                    onClick = if (isAllSelected) onDeselectAll else onSelectAll
                ) {
                    Icon(
                        imageVector = if (isAllSelected) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                        contentDescription = if (isAllSelected) "Deselect all" else "Select all",
                        tint = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expand/Collapse Icon
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            // Category description
            category.description?.let { desc ->
                AnimatedVisibility(visible = isExpanded) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(categoryName: String): ImageVector {
    val name = categoryName.lowercase()
    return when {
        name.contains("bcs") -> Icons.Outlined.WorkspacePremium
        name.contains("hsc") -> Icons.Outlined.School
        name.contains("ssc") -> Icons.AutoMirrored.Outlined.MenuBook
        name.contains("admission") -> Icons.AutoMirrored.Outlined.Assignment
        name.contains("university") -> Icons.Outlined.AccountBalance
        name.contains("job") -> Icons.Outlined.Work
        name.contains("bank") -> Icons.Outlined.AccountBalance
        else -> Icons.Outlined.Category
    }
}

@Composable
private fun SelectableSubjectWithChaptersCard(
    subjectItem: SubjectSelectionItem,
    onToggleSelection: () -> Unit,
    onToggleExpanded: () -> Unit,
    onToggleChapter: (String) -> Unit,
    onSelectAllChapters: () -> Unit,
    onDeselectAllChapters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (subjectItem.isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val borderColor = if (subjectItem.isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (subjectItem.isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Subject Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleSelection)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Checkbox(
                    checked = subjectItem.isSelected,
                    onCheckedChange = { onToggleSelection() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Subject Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (subjectItem.isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = if (subjectItem.isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Subject Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subjectItem.subject.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (subjectItem.isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Question count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.exam_question_count, subjectItem.subject.questionCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }

                    // Chapter count (on separate line to prevent breaking)
                    if (subjectItem.chapters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (subjectItem.selectedChapterIds.isEmpty()) {
                                    stringResource(R.string.exam_all_chapters, subjectItem.chapters.size)
                                } else {
                                    stringResource(R.string.exam_chapters_selected, subjectItem.selectedChapterIds.size, subjectItem.chapters.size)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Expand chapters button (only if has chapters and is selected)
                if (subjectItem.chapters.isNotEmpty() && subjectItem.isSelected) {
                    IconButton(onClick = onToggleExpanded) {
                        Icon(
                            imageVector = if (subjectItem.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (subjectItem.isExpanded) "Collapse" else "Expand chapters",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Chapter List (animated)
            AnimatedVisibility(
                visible = subjectItem.isExpanded && subjectItem.isSelected && subjectItem.chapters.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    // Chapter selection header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.exam_select_chapters),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row {
                            TextButton(
                                onClick = onSelectAllChapters,
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    stringResource(R.string.select_all),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            TextButton(
                                onClick = onDeselectAllChapters,
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    stringResource(R.string.clear),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    // Chapters
                    subjectItem.chapters.forEach { chapter ->
                        val isChapterSelected = chapter.id in subjectItem.selectedChapterIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleChapter(chapter.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChapterSelected,
                                onCheckedChange = { onToggleChapter(chapter.id) },
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isChapterSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isChapterSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.exam_question_count, chapter.questionCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

