package com.gatishil.studyengine.presentation.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.data.remote.dto.ScheduleOverrideDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleOverridesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScheduleOverridesViewModel = hiltViewModel()
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
                title = { Text("Schedule Overrides") },
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
                Icon(Icons.Default.Add, contentDescription = "Add override")
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
                uiState.overrides.isEmpty() -> {
                    EmptyOverridesState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    OverridesList(
                        overrides = uiState.overrides,
                        onDelete = viewModel::deleteOverride
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddOverrideDialog(
            selectedDate = uiState.selectedDate,
            isOff = uiState.isOff,
            startTime = uiState.startTime,
            endTime = uiState.endTime,
            isLoading = uiState.isAddingNew,
            onDateSelected = viewModel::updateSelectedDate,
            onIsOffChanged = viewModel::updateIsOff,
            onStartTimeSelected = viewModel::updateStartTime,
            onEndTimeSelected = viewModel::updateEndTime,
            onConfirm = viewModel::addOverride,
            onDismiss = viewModel::hideAddDialog
        )
    }
}

@Composable
private fun EmptyOverridesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No schedule overrides", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Add days off or custom study hours",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverridesList(
    overrides: List<ScheduleOverrideDto>,
    onDelete: (String) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(overrides, key = { it.id }) { override ->
            OverrideCard(
                override = override,
                dateFormatter = dateFormatter,
                onDelete = { onDelete(override.id) }
            )
        }
    }
}

@Composable
private fun OverrideCard(
    override: ScheduleOverrideDto,
    dateFormatter: DateTimeFormatter,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val date = try {
        LocalDate.parse(override.overrideDate)
    } catch (e: Exception) {
        null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (override.isOff)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
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
                    text = date?.format(dateFormatter) ?: override.overrideDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (override.isOff) "Day Off" else "Custom: ${override.startTime} - ${override.endTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (override.isOff)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Override") },
            text = { Text("Are you sure you want to delete this schedule override?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOverrideDialog(
    selectedDate: LocalDate,
    isOff: Boolean,
    startTime: LocalTime?,
    endTime: LocalTime?,
    isLoading: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onIsOffChanged: (Boolean) -> Unit,
    onStartTimeSelected: (LocalTime) -> Unit,
    onEndTimeSelected: (LocalTime) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Schedule Override") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Date Picker
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date", style = MaterialTheme.typography.labelSmall)
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Day Off Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Day Off", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isOff, onCheckedChange = onIsOffChanged)
                }

                // Custom Time (only if not day off)
                if (!isOff) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedCard(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Start", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    startTime?.toString() ?: "Select",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        OutlinedCard(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("End", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    endTime?.toString() ?: "Select",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        TimePickerDialogOverride(
            initialTime = startTime ?: LocalTime.of(9, 0),
            onTimeSelected = { onStartTimeSelected(it); showStartTimePicker = false },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialogOverride(
            initialTime = endTime ?: LocalTime.of(17, 0),
            onTimeSelected = { onEndTimeSelected(it); showEndTimePicker = false },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogOverride(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(initialTime.hour, initialTime.minute)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(LocalTime.of(state.hour, state.minute)) }) {
                Text("OK")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

