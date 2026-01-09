package com.gatishil.studyengine.presentation.screens.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.gatishil.studyengine.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    onNavigateBack: () -> Unit,
    onBookCreated: (String) -> Unit,
    viewModel: AddBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.createdBookId) {
        uiState.createdBookId?.let { bookId ->
            onBookCreated(bookId)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_book)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text(stringResource(R.string.book_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            // Subject
            OutlinedTextField(
                value = uiState.subject,
                onValueChange = viewModel::updateSubject,
                label = { Text(stringResource(R.string.book_subject)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            // Total Pages
            OutlinedTextField(
                value = uiState.totalPages,
                onValueChange = viewModel::updateTotalPages,
                label = { Text(stringResource(R.string.total_pages)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.isLoading
            )

            // Priority
            Text(
                text = stringResource(R.string.priority),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    1 to R.string.priority_low,
                    2 to R.string.priority_medium,
                    3 to R.string.priority_high
                ).forEach { (value, labelRes) ->
                    FilterChip(
                        selected = uiState.priority == value,
                        onClick = { viewModel.updatePriority(value) },
                        label = { Text(stringResource(labelRes)) },
                        enabled = !uiState.isLoading
                    )
                }
            }

            // Difficulty
            Text(
                text = stringResource(R.string.difficulty),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    1 to R.string.difficulty_easy,
                    2 to R.string.difficulty_medium,
                    3 to R.string.difficulty_hard
                ).forEach { (value, labelRes) ->
                    FilterChip(
                        selected = uiState.difficulty == value,
                        onClick = { viewModel.updateDifficulty(value) },
                        label = { Text(stringResource(labelRes)) },
                        enabled = !uiState.isLoading
                    )
                }
            }

            // Target End Date
            OutlinedTextField(
                value = uiState.targetEndDate?.format(DateTimeFormatter.ISO_DATE) ?: "",
                onValueChange = { },
                label = { Text(stringResource(R.string.target_end_date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = !uiState.isLoading,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.select_date)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Create Button
            Button(
                onClick = viewModel::createBook,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.targetEndDate?.toEpochDay()?.times(86400000)
                ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofEpochDay(millis / 86400000)
                            viewModel.updateTargetEndDate(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

