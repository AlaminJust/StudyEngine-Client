package com.gatishil.studyengine.presentation.screens.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.UserAcademicProfile
import com.gatishil.studyengine.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AcademicUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val academicProfile: UserAcademicProfile? = null,
    val error: String? = null,
    val isUpdating: Boolean = false,

    // Basic Info Dialog
    val editBasicInfoDialogVisible: Boolean = false,
    val editRole: String = "Student",
    val editRoleDescription: String = "",
    val editBio: String = "",

    // Student Info Dialog
    val editStudentInfoDialogVisible: Boolean = false,
    val editAcademicLevel: String = "",
    val editCurrentClass: String = "",
    val editMajor: String = "",
    val editDepartment: String = "",
    val editStudentType: String = "",
    val editStudentId: String = "",
    val editAcademicYear: String = "",
    val editCurrentSemester: String = "",

    // Institution Dialog
    val editInstitutionDialogVisible: Boolean = false,
    val editInstitutionName: String = "",
    val editInstitutionType: String = "",
    val editInstitutionCountry: String = "",
    val editInstitutionCity: String = "",
    val editInstitutionState: String = "",

    // Teaching Info Dialog
    val editTeachingDialogVisible: Boolean = false,
    val editTeachingSubjects: String = "",
    val editQualifications: String = "",
    val editYearsOfExperience: String = "",

    // Social Links Dialog
    val editSocialDialogVisible: Boolean = false,
    val editResearchInterests: String = "",
    val editWebsite: String = "",
    val editLinkedIn: String = "",
    val editGitHub: String = ""
)

sealed class AcademicEvent {
    data object ProfileUpdated : AcademicEvent()
    data class Error(val message: String) : AcademicEvent()
}

@HiltViewModel
class AcademicViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademicUiState())
    val uiState: StateFlow<AcademicUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AcademicEvent>()
    val events: SharedFlow<AcademicEvent> = _events.asSharedFlow()

    init {
        loadAcademicProfile()
    }

    fun loadAcademicProfile() {
        viewModelScope.launch {
            profileRepository.getAcademicProfile().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                academicProfile = resource.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = resource.message ?: "Failed to load academic profile"
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadAcademicProfile()
    }

    // ==================== Basic Info ====================

    fun showEditBasicInfoDialog() {
        val profile = _uiState.value.academicProfile
        _uiState.update {
            it.copy(
                editBasicInfoDialogVisible = true,
                editRole = profile?.role ?: "Student",
                editRoleDescription = profile?.roleDescription ?: "",
                editBio = profile?.bio ?: ""
            )
        }
    }

    fun hideEditBasicInfoDialog() {
        _uiState.update { it.copy(editBasicInfoDialogVisible = false) }
    }

    fun updateEditBasicInfo(role: String? = null, roleDescription: String? = null, bio: String? = null) {
        _uiState.update {
            it.copy(
                editRole = role ?: it.editRole,
                editRoleDescription = roleDescription ?: it.editRoleDescription,
                editBio = bio ?: it.editBio
            )
        }
    }

    fun saveBasicInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateAcademicBasicInfo(
                role = _uiState.value.editRole,
                roleDescription = _uiState.value.editRoleDescription.takeIf { it.isNotBlank() },
                bio = _uiState.value.editBio.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isUpdating = false, editBasicInfoDialogVisible = false, academicProfile = result.data)
                    }
                    _events.emit(AcademicEvent.ProfileUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(AcademicEvent.Error(result.message ?: "Failed to update"))
                }
                else -> {}
            }
        }
    }

    // ==================== Student Info ====================

    fun showEditStudentInfoDialog() {
        val profile = _uiState.value.academicProfile
        _uiState.update {
            it.copy(
                editStudentInfoDialogVisible = true,
                editAcademicLevel = profile?.academicLevel ?: "",
                editCurrentClass = profile?.currentClass ?: "",
                editMajor = profile?.major ?: "",
                editDepartment = profile?.department ?: "",
                editStudentType = profile?.studentType ?: "",
                editStudentId = profile?.studentId ?: "",
                editAcademicYear = profile?.academicYear?.toString() ?: "",
                editCurrentSemester = profile?.currentSemester ?: ""
            )
        }
    }

    fun hideEditStudentInfoDialog() {
        _uiState.update { it.copy(editStudentInfoDialogVisible = false) }
    }

    fun updateEditStudentInfo(
        academicLevel: String? = null,
        currentClass: String? = null,
        major: String? = null,
        department: String? = null,
        studentType: String? = null,
        studentId: String? = null,
        academicYear: String? = null,
        currentSemester: String? = null
    ) {
        _uiState.update {
            it.copy(
                editAcademicLevel = academicLevel ?: it.editAcademicLevel,
                editCurrentClass = currentClass ?: it.editCurrentClass,
                editMajor = major ?: it.editMajor,
                editDepartment = department ?: it.editDepartment,
                editStudentType = studentType ?: it.editStudentType,
                editStudentId = studentId ?: it.editStudentId,
                editAcademicYear = academicYear ?: it.editAcademicYear,
                editCurrentSemester = currentSemester ?: it.editCurrentSemester
            )
        }
    }

    fun saveStudentInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateStudentInfo(
                academicLevel = _uiState.value.editAcademicLevel.takeIf { it.isNotBlank() },
                currentClass = _uiState.value.editCurrentClass.takeIf { it.isNotBlank() },
                major = _uiState.value.editMajor.takeIf { it.isNotBlank() },
                department = _uiState.value.editDepartment.takeIf { it.isNotBlank() },
                studentType = _uiState.value.editStudentType.takeIf { it.isNotBlank() },
                studentId = _uiState.value.editStudentId.takeIf { it.isNotBlank() },
                academicYear = _uiState.value.editAcademicYear.toIntOrNull(),
                currentSemester = _uiState.value.editCurrentSemester.takeIf { it.isNotBlank() },
                enrollmentDate = null,
                expectedGraduationDate = null
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isUpdating = false, editStudentInfoDialogVisible = false, academicProfile = result.data)
                    }
                    _events.emit(AcademicEvent.ProfileUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(AcademicEvent.Error(result.message ?: "Failed to update"))
                }
                else -> {}
            }
        }
    }

    // ==================== Institution Info ====================

    fun showEditInstitutionDialog() {
        val profile = _uiState.value.academicProfile?.institution
        _uiState.update {
            it.copy(
                editInstitutionDialogVisible = true,
                editInstitutionName = profile?.name ?: "",
                editInstitutionType = profile?.type ?: "",
                editInstitutionCountry = profile?.country ?: "",
                editInstitutionCity = profile?.city ?: "",
                editInstitutionState = profile?.state ?: ""
            )
        }
    }

    fun hideEditInstitutionDialog() {
        _uiState.update { it.copy(editInstitutionDialogVisible = false) }
    }

    fun updateEditInstitution(
        name: String? = null,
        type: String? = null,
        country: String? = null,
        city: String? = null,
        state: String? = null
    ) {
        _uiState.update {
            it.copy(
                editInstitutionName = name ?: it.editInstitutionName,
                editInstitutionType = type ?: it.editInstitutionType,
                editInstitutionCountry = country ?: it.editInstitutionCountry,
                editInstitutionCity = city ?: it.editInstitutionCity,
                editInstitutionState = state ?: it.editInstitutionState
            )
        }
    }

    fun saveInstitutionInfo() {
        val name = _uiState.value.editInstitutionName
        val type = _uiState.value.editInstitutionType
        val country = _uiState.value.editInstitutionCountry

        if (name.isBlank() || type.isBlank() || country.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateInstitutionInfo(
                name = name,
                type = type,
                country = country,
                city = _uiState.value.editInstitutionCity.takeIf { it.isNotBlank() },
                state = _uiState.value.editInstitutionState.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isUpdating = false, editInstitutionDialogVisible = false, academicProfile = result.data)
                    }
                    _events.emit(AcademicEvent.ProfileUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(AcademicEvent.Error(result.message ?: "Failed to update"))
                }
                else -> {}
            }
        }
    }

    // ==================== Teaching Info ====================

    fun showEditTeachingDialog() {
        val profile = _uiState.value.academicProfile
        _uiState.update {
            it.copy(
                editTeachingDialogVisible = true,
                editTeachingSubjects = profile?.teachingSubjects ?: "",
                editQualifications = profile?.qualifications ?: "",
                editYearsOfExperience = profile?.yearsOfExperience?.toString() ?: ""
            )
        }
    }

    fun hideEditTeachingDialog() {
        _uiState.update { it.copy(editTeachingDialogVisible = false) }
    }

    fun updateEditTeaching(subjects: String? = null, qualifications: String? = null, years: String? = null) {
        _uiState.update {
            it.copy(
                editTeachingSubjects = subjects ?: it.editTeachingSubjects,
                editQualifications = qualifications ?: it.editQualifications,
                editYearsOfExperience = years ?: it.editYearsOfExperience
            )
        }
    }

    fun saveTeachingInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateTeachingInfo(
                teachingSubjects = _uiState.value.editTeachingSubjects.takeIf { it.isNotBlank() },
                qualifications = _uiState.value.editQualifications.takeIf { it.isNotBlank() },
                yearsOfExperience = _uiState.value.editYearsOfExperience.toIntOrNull()
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isUpdating = false, editTeachingDialogVisible = false, academicProfile = result.data)
                    }
                    _events.emit(AcademicEvent.ProfileUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(AcademicEvent.Error(result.message ?: "Failed to update"))
                }
                else -> {}
            }
        }
    }

    // ==================== Social Links ====================

    fun showEditSocialDialog() {
        val profile = _uiState.value.academicProfile
        _uiState.update {
            it.copy(
                editSocialDialogVisible = true,
                editResearchInterests = profile?.researchInterests ?: "",
                editWebsite = profile?.socialLinks?.website ?: "",
                editLinkedIn = profile?.socialLinks?.linkedIn ?: "",
                editGitHub = profile?.socialLinks?.gitHub ?: ""
            )
        }
    }

    fun hideEditSocialDialog() {
        _uiState.update { it.copy(editSocialDialogVisible = false) }
    }

    fun updateEditSocial(research: String? = null, website: String? = null, linkedIn: String? = null, gitHub: String? = null) {
        _uiState.update {
            it.copy(
                editResearchInterests = research ?: it.editResearchInterests,
                editWebsite = website ?: it.editWebsite,
                editLinkedIn = linkedIn ?: it.editLinkedIn,
                editGitHub = gitHub ?: it.editGitHub
            )
        }
    }

    fun saveSocialLinks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateSocialLinks(
                researchInterests = _uiState.value.editResearchInterests.takeIf { it.isNotBlank() },
                website = _uiState.value.editWebsite.takeIf { it.isNotBlank() },
                linkedIn = _uiState.value.editLinkedIn.takeIf { it.isNotBlank() },
                gitHub = _uiState.value.editGitHub.takeIf { it.isNotBlank() }
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isUpdating = false, editSocialDialogVisible = false, academicProfile = result.data)
                    }
                    _events.emit(AcademicEvent.ProfileUpdated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    _events.emit(AcademicEvent.Error(result.message ?: "Failed to update"))
                }
                else -> {}
            }
        }
    }
}

