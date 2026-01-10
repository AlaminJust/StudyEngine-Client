package com.gatishil.studyengine.presentation.screens.schedule

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.data.remote.dto.ScheduleContextDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleContextsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScheduleContextsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Contexts") },
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
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Add context")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.contexts.isEmpty() -> {
                    EmptyContextsState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    ContextsList(
                        contexts = uiState.contexts,
                        activeContext = uiState.activeContext,
                        onDelete = viewModel::deleteContext,
                        onUpdateMultiplier = viewModel::updateContextLoadMultiplier
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddContextDialog(
            contextType = uiState.contextType,
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            loadMultiplier = uiState.loadMultiplier,
            isLoading = uiState.isAddingNew,
            onContextTypeChanged = viewModel::updateContextType,
            onStartDateSelected = viewModel::updateStartDate,
            onEndDateSelected = viewModel::updateEndDate,
            onLoadMultiplierChanged = viewModel::updateLoadMultiplier,
            onConfirm = viewModel::addContext,
            onDismiss = viewModel::hideAddDialog
        )
    }
}

@Composable
private fun EmptyContextsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No schedule contexts", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Add exam periods, vacations, or study intensity adjustments",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContextsList(
    contexts: List<ScheduleContextDto>,
    activeContext: ScheduleContextDto?,
    onDelete: (String) -> Unit,
    onUpdateMultiplier: (String, Float) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active context highlight
        activeContext?.let { active ->
            item {
                Text(
                    "Currently Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(contexts, key = { it.id }) { context ->
            ContextCard(
                context = context,
                isActive = context.id == activeContext?.id,
                dateFormatter = dateFormatter,
                onDelete = { onDelete(context.id) },
                onUpdateMultiplier = { onUpdateMultiplier(context.id, it) }
            )
        }
    }
}

@Composable
private fun ContextCard(
    context: ScheduleContextDto,
    isActive: Boolean,
    dateFormatter: DateTimeFormatter,
    onDelete: () -> Unit,
    onUpdateMultiplier: (Float) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val startDate = try { LocalDate.parse(context.startDate) } catch (e: Exception) { null }
    val endDate = try { LocalDate.parse(context.endDate) } catch (e: Exception) { null }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = context.contextType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge { Text("Active") }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${startDate?.format(dateFormatter) ?: context.startDate} - ${endDate?.format(dateFormatter) ?: context.endDate}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Load multiplier indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        context.loadMultiplier > 1.0f -> Icons.Default.TrendingUp
                        context.loadMultiplier < 1.0f -> Icons.Default.TrendingDown
                        else -> Icons.Default.TrendingFlat
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = when {
                        context.loadMultiplier > 1.0f -> MaterialTheme.colorScheme.error
                        context.loadMultiplier < 1.0f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Study Load: ${(context.loadMultiplier * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Context") },
            text = { Text("Are you sure you want to delete '${context.contextType}'?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditDialog) {
        EditMultiplierDialog(
            currentMultiplier = context.loadMultiplier,
            onConfirm = { onUpdateMultiplier(it); showEditDialog = false },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
private fun EditMultiplierDialog(
    currentMultiplier: Float,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var multiplier by remember { mutableFloatStateOf(currentMultiplier) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Study Load") },
        text = {
            Column {
                Text("Current: ${(multiplier * 100).toInt()}%")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = multiplier,
                    onValueChange = { multiplier = it },
                    valueRange = 0f..2f,
                    steps = 7
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = MaterialTheme.typography.bodySmall)
                    Text("100%", style = MaterialTheme.typography.bodySmall)
                    Text("200%", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(multiplier) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContextDialog(
    contextType: String,
    startDate: LocalDate,
    endDate: LocalDate,
    loadMultiplier: Float,
    isLoading: Boolean,
    onContextTypeChanged: (String) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onLoadMultiplierChanged: (Float) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Schedule Context") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Context Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = contextType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Context Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ScheduleContextsViewModel.CONTEXT_TYPES.forEach { (type, _) ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onContextTypeChanged(type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Date Range
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedCard(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Start", style = MaterialTheme.typography.labelSmall)
                            Text(
                                startDate.format(DateTimeFormatter.ofPattern("MMM d")),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    OutlinedCard(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("End", style = MaterialTheme.typography.labelSmall)
                            Text(
                                endDate.format(DateTimeFormatter.ofPattern("dd MMM")),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Load Multiplier
                Column {
                    Text(
                        "Study Load: ${(loadMultiplier * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = loadMultiplier,
                        onValueChange = onLoadMultiplierChanged,
                        valueRange = 0f..2f,
                        steps = 7
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showStartDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = startDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onStartDateSelected(LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000)))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = state) }
    }

    if (showEndDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = endDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onEndDateSelected(LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000)))
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = state) }
    }
}

