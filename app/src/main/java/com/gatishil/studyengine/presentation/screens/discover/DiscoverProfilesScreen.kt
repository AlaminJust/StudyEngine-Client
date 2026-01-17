package com.gatishil.studyengine.presentation.screens.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.PublicProfileCard
import com.gatishil.studyengine.presentation.common.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverProfilesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: DiscoverProfilesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Load more when reaching end of list
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= uiState.profiles.size - 3) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            DiscoverTopBar(
                onNavigateBack = onNavigateBack,
                searchTerm = uiState.searchTerm,
                onSearchTermChange = viewModel::onSearchTermChange,
                onSearch = {
                    focusManager.clearFocus()
                    viewModel.onSearch()
                },
                showFilters = uiState.showFilters,
                onToggleFilters = viewModel::toggleFilters
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Filters Section
            AnimatedVisibility(
                visible = uiState.showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                FiltersSection(
                    selectedRole = uiState.selectedRole,
                    onRoleSelected = viewModel::onRoleSelected,
                    selectedAcademicLevel = uiState.selectedAcademicLevel,
                    onAcademicLevelSelected = viewModel::onAcademicLevelSelected,
                    onClearFilters = viewModel::clearFilters
                )
            }

            // Results count
            if (uiState.totalCount > 0 && !uiState.isLoading) {
                Text(
                    text = stringResource(R.string.discover_results_count, uiState.totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> LoadingScreen()
                    uiState.profiles.isEmpty() -> EmptyDiscoverState()
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.profiles) { profile ->
                                DiscoverProfileCard(
                                    profile = profile,
                                    onClick = { onNavigateToProfile(profile.id) }
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
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

    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error then clear it
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverTopBar(
    onNavigateBack: () -> Unit,
    searchTerm: String,
    onSearchTermChange: (String) -> Unit,
    onSearch: () -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarHeight)
        ) {
            // Top row with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }

                Text(
                    text = stringResource(R.string.discover_profiles),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onToggleFilters) {
                    Icon(
                        imageVector = if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.filters),
                        tint = Color.White
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchTerm,
                onValueChange = onSearchTermChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_profiles_hint),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                trailingIcon = {
                    if (searchTerm.isNotEmpty()) {
                        IconButton(onClick = { onSearchTermChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear),
                                tint = Color.White
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun FiltersSection(
    selectedRole: String?,
    onRoleSelected: (String?) -> Unit,
    selectedAcademicLevel: String?,
    onAcademicLevelSelected: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    val roles = listOf("Student", "Teacher", "Researcher", "Professional", "Enthusiast")
    val academicLevels = listOf("School", "Undergraduate", "Graduate", "PostGraduate", "Doctorate")

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_by_role),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = onClearFilters) {
                    Text(stringResource(R.string.clear_filters))
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roles) { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = {
                            onRoleSelected(if (selectedRole == role) null else role)
                        },
                        label = { Text(role) }
                    )
                }
            }

            Text(
                text = stringResource(R.string.filter_by_academic_level),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(academicLevels) { level ->
                    FilterChip(
                        selected = selectedAcademicLevel == level,
                        onClick = {
                            onAcademicLevelSelected(if (selectedAcademicLevel == level) null else level)
                        },
                        label = { Text(level) }
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverProfileCard(
    profile: PublicProfileCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profile.profilePictureUrl != null) {
                    AsyncImage(
                        model = profile.profilePictureUrl,
                        contentDescription = profile.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = profile.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Profile Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Role & Academic Level
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.role?.let { role ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = role,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    profile.academicLevel?.let { level ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = level,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                // Major or Department
                (profile.major ?: profile.department)?.let { field ->
                    Text(
                        text = field,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Institution
                profile.institutionName?.let { institution ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = buildString {
                                append(institution)
                                profile.institutionCountry?.let { append(", $it") }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    profile.currentStreak?.let { streak ->
                        if (streak > 0) {
                            StatItem(
                                icon = Icons.Default.Whatshot,
                                value = streak.toString(),
                                label = stringResource(R.string.streak),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    profile.totalBooksCompleted?.let { books ->
                        if (books > 0) {
                            StatItem(
                                icon = Icons.Default.MenuBook,
                                value = books.toString(),
                                label = stringResource(R.string.books),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    profile.totalStudyDays?.let { days ->
                        if (days > 0) {
                            StatItem(
                                icon = Icons.Default.CalendarToday,
                                value = days.toString(),
                                label = stringResource(R.string.days),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun EmptyDiscoverState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonSearch,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = stringResource(R.string.no_profiles_found),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.try_adjusting_filters),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

