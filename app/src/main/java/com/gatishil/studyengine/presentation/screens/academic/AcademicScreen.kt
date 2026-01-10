package com.gatishil.studyengine.presentation.screens.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gatishil.studyengine.R
import com.gatishil.studyengine.domain.model.UserAcademicProfile
import com.gatishil.studyengine.presentation.common.components.ErrorScreen
import com.gatishil.studyengine.presentation.common.components.LoadingScreen
import com.gatishil.studyengine.ui.theme.StudyEngineTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicScreen(
    onNavigateBack: () -> Unit,
    viewModel: AcademicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AcademicEvent.ProfileUpdated -> {
                    snackbarHostState.showSnackbar("Profile updated successfully")
                }
                is AcademicEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.academic_profile)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.academicProfile == null -> LoadingScreen()
                uiState.error != null && uiState.academicProfile == null -> {
                    ErrorScreen(
                        message = uiState.error ?: "Something went wrong",
                        onRetry = { viewModel.loadAcademicProfile() }
                    )
                }
                uiState.academicProfile != null -> {
                    AcademicContent(
                        profile = uiState.academicProfile!!,
                        onEditBasicInfo = { viewModel.showEditBasicInfoDialog() },
                        onEditStudentInfo = { viewModel.showEditStudentInfoDialog() },
                        onEditInstitution = { viewModel.showEditInstitutionDialog() },
                        onEditTeaching = { viewModel.showEditTeachingDialog() },
                        onEditSocial = { viewModel.showEditSocialDialog() }
                    )
                }
            }
        }

        // Dialogs
        if (uiState.editBasicInfoDialogVisible) {
            EditBasicInfoDialog(
                role = uiState.editRole,
                roleDescription = uiState.editRoleDescription,
                bio = uiState.editBio,
                onRoleChange = { viewModel.updateEditBasicInfo(role = it) },
                onRoleDescriptionChange = { viewModel.updateEditBasicInfo(roleDescription = it) },
                onBioChange = { viewModel.updateEditBasicInfo(bio = it) },
                onConfirm = { viewModel.saveBasicInfo() },
                onDismiss = { viewModel.hideEditBasicInfoDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editStudentInfoDialogVisible) {
            EditStudentInfoDialog(
                academicLevel = uiState.editAcademicLevel,
                currentClass = uiState.editCurrentClass,
                major = uiState.editMajor,
                department = uiState.editDepartment,
                studentId = uiState.editStudentId,
                academicYear = uiState.editAcademicYear,
                currentSemester = uiState.editCurrentSemester,
                onAcademicLevelChange = { viewModel.updateEditStudentInfo(academicLevel = it) },
                onCurrentClassChange = { viewModel.updateEditStudentInfo(currentClass = it) },
                onMajorChange = { viewModel.updateEditStudentInfo(major = it) },
                onDepartmentChange = { viewModel.updateEditStudentInfo(department = it) },
                onStudentIdChange = { viewModel.updateEditStudentInfo(studentId = it) },
                onAcademicYearChange = { viewModel.updateEditStudentInfo(academicYear = it) },
                onCurrentSemesterChange = { viewModel.updateEditStudentInfo(currentSemester = it) },
                onConfirm = { viewModel.saveStudentInfo() },
                onDismiss = { viewModel.hideEditStudentInfoDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editInstitutionDialogVisible) {
            EditInstitutionDialog(
                name = uiState.editInstitutionName,
                type = uiState.editInstitutionType,
                country = uiState.editInstitutionCountry,
                city = uiState.editInstitutionCity,
                state = uiState.editInstitutionState,
                onNameChange = { viewModel.updateEditInstitution(name = it) },
                onTypeChange = { viewModel.updateEditInstitution(type = it) },
                onCountryChange = { viewModel.updateEditInstitution(country = it) },
                onCityChange = { viewModel.updateEditInstitution(city = it) },
                onStateChange = { viewModel.updateEditInstitution(state = it) },
                onConfirm = { viewModel.saveInstitutionInfo() },
                onDismiss = { viewModel.hideEditInstitutionDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editTeachingDialogVisible) {
            EditTeachingDialog(
                subjects = uiState.editTeachingSubjects,
                qualifications = uiState.editQualifications,
                years = uiState.editYearsOfExperience,
                onSubjectsChange = { viewModel.updateEditTeaching(subjects = it) },
                onQualificationsChange = { viewModel.updateEditTeaching(qualifications = it) },
                onYearsChange = { viewModel.updateEditTeaching(years = it) },
                onConfirm = { viewModel.saveTeachingInfo() },
                onDismiss = { viewModel.hideEditTeachingDialog() },
                isLoading = uiState.isUpdating
            )
        }

        if (uiState.editSocialDialogVisible) {
            EditSocialDialog(
                researchInterests = uiState.editResearchInterests,
                website = uiState.editWebsite,
                linkedIn = uiState.editLinkedIn,
                gitHub = uiState.editGitHub,
                onResearchChange = { viewModel.updateEditSocial(research = it) },
                onWebsiteChange = { viewModel.updateEditSocial(website = it) },
                onLinkedInChange = { viewModel.updateEditSocial(linkedIn = it) },
                onGitHubChange = { viewModel.updateEditSocial(gitHub = it) },
                onConfirm = { viewModel.saveSocialLinks() },
                onDismiss = { viewModel.hideEditSocialDialog() },
                isLoading = uiState.isUpdating
            )
        }
    }
}

@Composable
private fun AcademicContent(
    profile: UserAcademicProfile,
    onEditBasicInfo: () -> Unit,
    onEditStudentInfo: () -> Unit,
    onEditInstitution: () -> Unit,
    onEditTeaching: () -> Unit,
    onEditSocial: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item { AcademicHeader(profile = profile) }

        // Basic Info
        item { EditableSectionTitle(title = stringResource(R.string.basic_info), onEdit = onEditBasicInfo) }
        item { BasicInfoCard(profile = profile, onEdit = onEditBasicInfo) }

        // Student Info (if student role)
        if (profile.role.equals("Student", ignoreCase = true)) {
            item { EditableSectionTitle(title = stringResource(R.string.student_info), onEdit = onEditStudentInfo) }
            item { StudentInfoCard(profile = profile, onEdit = onEditStudentInfo) }
        }

        // Teaching Info (if teacher role)
        if (profile.role.equals("Teacher", ignoreCase = true) || profile.role.equals("Professor", ignoreCase = true)) {
            item { EditableSectionTitle(title = stringResource(R.string.teaching_info), onEdit = onEditTeaching) }
            item { TeachingInfoCard(profile = profile, onEdit = onEditTeaching) }
        }

        // Institution
        item { EditableSectionTitle(title = stringResource(R.string.institution), onEdit = onEditInstitution) }
        item { InstitutionCard(profile = profile, onEdit = onEditInstitution) }

        // Social & Research
        item { EditableSectionTitle(title = stringResource(R.string.social_research), onEdit = onEditSocial) }
        item { SocialResearchCard(profile = profile, onEdit = onEditSocial) }
    }
}

@Composable
private fun AcademicHeader(profile: UserAcademicProfile) {
    Box(
        modifier = Modifier.fillMaxWidth().height(160.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Role icon
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (profile.role.lowercase()) {
                            "student" -> Icons.Default.School
                            "teacher", "professor" -> Icons.Default.Person
                            "researcher" -> Icons.Default.Science
                            else -> Icons.Default.AccountCircle
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profile.role,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            profile.roleDescription?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            if (profile.isVerified) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudyEngineTheme.extendedColors.success.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.verified), style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableSectionTitle(title: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.edit))
        }
    }
}

@Composable
private fun BasicInfoCard(profile: UserAcademicProfile, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoRow(icon = Icons.Default.Person, label = stringResource(R.string.role), value = profile.role)
            profile.roleDescription?.let {
                HorizontalDivider()
                InfoRow(icon = Icons.Default.Description, label = stringResource(R.string.role_description), value = it)
            }
            profile.bio?.let {
                HorizontalDivider()
                InfoRow(icon = Icons.Default.Info, label = stringResource(R.string.bio), value = it)
            }
        }
    }
}

@Composable
private fun StudentInfoCard(profile: UserAcademicProfile, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            profile.academicLevel?.let { InfoRow(icon = Icons.Default.Grade, label = stringResource(R.string.academic_level), value = it) }
            profile.currentClass?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Class, label = stringResource(R.string.current_class), value = it) }
            profile.major?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Book, label = stringResource(R.string.major), value = it) }
            profile.department?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Business, label = stringResource(R.string.department), value = it) }
            profile.studentId?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Badge, label = stringResource(R.string.student_id), value = it) }
            profile.academicYear?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.CalendarMonth, label = stringResource(R.string.academic_year), value = it.toString()) }
            profile.currentSemester?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Event, label = stringResource(R.string.current_semester), value = it) }

            if (profile.academicLevel == null && profile.currentClass == null && profile.major == null) {
                Text(
                    text = stringResource(R.string.no_student_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TeachingInfoCard(profile: UserAcademicProfile, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            profile.teachingSubjects?.let { InfoRow(icon = Icons.Default.MenuBook, label = stringResource(R.string.teaching_subjects), value = it) }
            profile.qualifications?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.School, label = stringResource(R.string.qualifications), value = it) }
            profile.yearsOfExperience?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.WorkHistory, label = stringResource(R.string.years_experience), value = "$it years") }

            if (profile.teachingSubjects == null && profile.qualifications == null) {
                Text(
                    text = stringResource(R.string.no_teaching_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InstitutionCard(profile: UserAcademicProfile, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (profile.institution != null) {
                InfoRow(icon = Icons.Default.AccountBalance, label = stringResource(R.string.institution_name), value = profile.institution.name)
                HorizontalDivider()
                InfoRow(icon = Icons.Default.Category, label = stringResource(R.string.institution_type), value = profile.institution.type)
                HorizontalDivider()
                InfoRow(icon = Icons.Default.Public, label = stringResource(R.string.country), value = profile.institution.country)
                profile.institution.city?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.LocationCity, label = stringResource(R.string.city), value = it) }
                profile.institution.state?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Map, label = stringResource(R.string.state), value = it) }
            } else {
                Text(
                    text = stringResource(R.string.no_institution_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SocialResearchCard(profile: UserAcademicProfile, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onEdit() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            profile.researchInterests?.let { InfoRow(icon = Icons.Default.Science, label = stringResource(R.string.research_interests), value = it) }
            profile.socialLinks?.website?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Language, label = stringResource(R.string.website), value = it) }
            profile.socialLinks?.linkedIn?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Work, label = "LinkedIn", value = it) }
            profile.socialLinks?.gitHub?.let { HorizontalDivider(); InfoRow(icon = Icons.Default.Code, label = "GitHub", value = it) }

            if (profile.researchInterests == null && profile.socialLinks == null) {
                Text(
                    text = stringResource(R.string.no_social_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.4f)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f))
    }
}

// ==================== Dialogs ====================

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EditBasicInfoDialog(
    role: String, roleDescription: String, bio: String,
    onRoleChange: (String) -> Unit, onRoleDescriptionChange: (String) -> Unit, onBioChange: (String) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit, isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Student", "Teacher", "Professor", "Researcher", "Self-learner", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.basic_info)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = role, onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.role)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { onRoleChange(r); expanded = false })
                        }
                    }
                }
                OutlinedTextField(value = roleDescription, onValueChange = onRoleDescriptionChange, label = { Text(stringResource(R.string.role_description)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bio, onValueChange = onBioChange, label = { Text(stringResource(R.string.bio)) }, minLines = 3, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditStudentInfoDialog(
    academicLevel: String, currentClass: String, major: String, department: String,
    studentId: String, academicYear: String, currentSemester: String,
    onAcademicLevelChange: (String) -> Unit, onCurrentClassChange: (String) -> Unit,
    onMajorChange: (String) -> Unit, onDepartmentChange: (String) -> Unit,
    onStudentIdChange: (String) -> Unit, onAcademicYearChange: (String) -> Unit,
    onCurrentSemesterChange: (String) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit, isLoading: Boolean
) {
    var levelExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }

    val academicLevels = listOf("Primary", "Secondary", "Higher Secondary", "Undergraduate", "Postgraduate", "PhD", "Other")
    val classes = listOf(
        "Class 1", "Class 2", "Class 3", "Class 4", "Class 5",
        "Class 6", "Class 7", "Class 8", "Class 9", "Class 10",
        "Class 11", "Class 12",
        "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year",
        "Masters 1st Year", "Masters 2nd Year",
        "PhD 1st Year", "PhD 2nd Year", "PhD 3rd Year", "PhD 4th Year",
        "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.student_info)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 450.dp).verticalScroll(rememberScrollState())
            ) {
                // Academic Level Dropdown
                ExposedDropdownMenuBox(expanded = levelExpanded, onExpandedChange = { levelExpanded = it }) {
                    OutlinedTextField(
                        value = academicLevel, onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.academic_level)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = levelExpanded, onDismissRequest = { levelExpanded = false }) {
                        academicLevels.forEach { level ->
                            DropdownMenuItem(text = { Text(level) }, onClick = { onAcademicLevelChange(level); levelExpanded = false })
                        }
                    }
                }

                // Current Class Dropdown
                ExposedDropdownMenuBox(expanded = classExpanded, onExpandedChange = { classExpanded = it }) {
                    OutlinedTextField(
                        value = currentClass, onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.current_class)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                        classes.forEach { cls ->
                            DropdownMenuItem(text = { Text(cls) }, onClick = { onCurrentClassChange(cls); classExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = major, onValueChange = onMajorChange, label = { Text(stringResource(R.string.major)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = department, onValueChange = onDepartmentChange, label = { Text(stringResource(R.string.department)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = studentId, onValueChange = onStudentIdChange, label = { Text(stringResource(R.string.student_id)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = academicYear, onValueChange = onAcademicYearChange, label = { Text(stringResource(R.string.year)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = currentSemester, onValueChange = onCurrentSemesterChange, label = { Text(stringResource(R.string.semester)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditInstitutionDialog(
    name: String, type: String, country: String, city: String, state: String,
    onNameChange: (String) -> Unit, onTypeChange: (String) -> Unit, onCountryChange: (String) -> Unit,
    onCityChange: (String) -> Unit, onStateChange: (String) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit, isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("University", "College", "High School", "Middle School", "Primary School", "Institute", "Academy", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.institution)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text(stringResource(R.string.institution_name) + " *") }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = type, onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.institution_type) + " *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { onTypeChange(t); expanded = false })
                        }
                    }
                }
                OutlinedTextField(value = country, onValueChange = onCountryChange, label = { Text(stringResource(R.string.country) + " *") }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = city, onValueChange = onCityChange, label = { Text(stringResource(R.string.city)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = state, onValueChange = onStateChange, label = { Text(stringResource(R.string.state)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank() && type.isNotBlank() && country.isNotBlank() && !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun EditTeachingDialog(
    subjects: String, qualifications: String, years: String,
    onSubjectsChange: (String) -> Unit, onQualificationsChange: (String) -> Unit, onYearsChange: (String) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit, isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.teaching_info)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = subjects, onValueChange = onSubjectsChange, label = { Text(stringResource(R.string.teaching_subjects)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qualifications, onValueChange = onQualificationsChange, label = { Text(stringResource(R.string.qualifications)) }, minLines = 2, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = years, onValueChange = onYearsChange, label = { Text(stringResource(R.string.years_experience)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun EditSocialDialog(
    researchInterests: String, website: String, linkedIn: String, gitHub: String,
    onResearchChange: (String) -> Unit, onWebsiteChange: (String) -> Unit,
    onLinkedInChange: (String) -> Unit, onGitHubChange: (String) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit, isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.social_research)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = researchInterests, onValueChange = onResearchChange, label = { Text(stringResource(R.string.research_interests)) }, minLines = 2, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = website, onValueChange = onWebsiteChange, label = { Text(stringResource(R.string.website)) }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = linkedIn, onValueChange = onLinkedInChange, label = { Text("LinkedIn URL") }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = gitHub, onValueChange = onGitHubChange, label = { Text("GitHub URL") }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text(stringResource(R.string.cancel)) } }
    )
}

