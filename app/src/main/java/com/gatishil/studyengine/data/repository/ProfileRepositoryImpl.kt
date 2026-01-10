package com.gatishil.studyengine.data.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.mapper.ProfileMapper
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.*
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi
) : ProfileRepository {

    override fun getProfile(): Flow<Resource<UserProfile>> = flow {
        emit(Resource.loading())
        try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                response.body()?.let { profileDto ->
                    emit(Resource.success(ProfileMapper.toDomain(profileDto)))
                } ?: emit(Resource.error(Exception("Empty response body")))
            } else {
                emit(Resource.error(
                    Exception("Failed to get profile: ${response.code()}"),
                    response.message()
                ))
            }
        } catch (e: Exception) {
            emit(Resource.error(e, e.message))
        }
    }

    override suspend fun updateProfile(name: String, timeZone: String): Resource<UserProfile> {
        return try {
            val response = api.updateProfile(UpdateProfileRequestDto(name, timeZone))
            if (response.isSuccessful) {
                response.body()?.let { profileDto ->
                    Resource.success(ProfileMapper.toDomain(profileDto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update profile: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override fun getPreferences(): Flow<Resource<UserPreferences>> = flow {
        emit(Resource.loading())
        try {
            val response = api.getPreferences()
            if (response.isSuccessful) {
                response.body()?.let { preferencesDto ->
                    emit(Resource.success(ProfileMapper.toPreferencesDomain(preferencesDto)))
                } ?: emit(Resource.error(Exception("Empty response body")))
            } else {
                emit(Resource.error(
                    Exception("Failed to get preferences: ${response.code()}"),
                    response.message()
                ))
            }
        } catch (e: Exception) {
            emit(Resource.error(e, e.message))
        }
    }

    override suspend fun updateStudyGoals(
        dailyPagesGoal: Int,
        dailyMinutesGoal: Int,
        weeklyStudyDaysGoal: Int
    ): Resource<UserPreferences> {
        return try {
            val response = api.updateStudyGoals(
                UpdateStudyGoalsRequestDto(dailyPagesGoal, dailyMinutesGoal, weeklyStudyDaysGoal)
            )
            if (response.isSuccessful) {
                response.body()?.let { preferencesDto ->
                    Resource.success(ProfileMapper.toPreferencesDomain(preferencesDto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update study goals: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateReadingSpeed(pagesPerHour: Int): Resource<UserPreferences> {
        return try {
            val response = api.updateReadingSpeed(UpdateReadingSpeedRequestDto(pagesPerHour))
            if (response.isSuccessful) {
                response.body()?.let { preferencesDto ->
                    Resource.success(ProfileMapper.toPreferencesDomain(preferencesDto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update reading speed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateSessionPreferences(
        preferredDuration: Int,
        minDuration: Int,
        maxDuration: Int
    ): Resource<UserPreferences> {
        return try {
            val response = api.updateSessionPreferences(
                UpdateSessionPreferencesRequestDto(preferredDuration, minDuration, maxDuration)
            )
            if (response.isSuccessful) {
                response.body()?.let { preferencesDto ->
                    Resource.success(ProfileMapper.toPreferencesDomain(preferencesDto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update session preferences: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateNotificationPreferences(
        enableSessionReminders: Boolean,
        reminderMinutesBefore: Int,
        enableStreakReminders: Boolean,
        enableWeeklyDigest: Boolean,
        enableAchievementNotifications: Boolean
    ): Resource<UserPreferences> {
        return try {
            val response = api.updateNotificationPreferences(
                UpdateNotificationPreferencesRequestDto(
                    enableSessionReminders,
                    reminderMinutesBefore,
                    enableStreakReminders,
                    enableWeeklyDigest,
                    enableAchievementNotifications
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { preferencesDto ->
                    Resource.success(ProfileMapper.toPreferencesDomain(preferencesDto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update notification preferences: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateUIPreferences(
        theme: String,
        language: String,
        showMotivationalQuotes: Boolean
    ): Resource<UserPreferences> {
        return try {
            val response = api.updateUIPreferences(
                UpdateUIPreferencesRequestDto(theme, language, showMotivationalQuotes)
            )
            if (response.isSuccessful) {
                response.body()?.let { preferencesDto ->
                    Resource.success(ProfileMapper.toPreferencesDomain(preferencesDto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update UI preferences: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updatePrivacySettings(
        showProfilePublicly: Boolean,
        showStatsPublicly: Boolean
    ): Resource<UserPreferences> {
        return try {
            val response = api.updatePrivacySettings(
                UpdatePrivacySettingsRequestDto(showProfilePublicly, showStatsPublicly)
            )
            if (response.isSuccessful) {
                response.body()?.let { preferencesDto ->
                    Resource.success(ProfileMapper.toPreferencesDomain(preferencesDto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update privacy settings: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun deactivateAccount(): Resource<Boolean> {
        return try {
            val response = api.deactivateAccount()
            if (response.isSuccessful) {
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Failed to deactivate account: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun reactivateAccount(): Resource<Boolean> {
        return try {
            val response = api.reactivateAccount()
            if (response.isSuccessful) {
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Failed to reactivate account: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun deleteAccount(confirmationPhrase: String): Resource<Boolean> {
        return try {
            val response = api.deleteAccount(DeleteAccountRequestDto(confirmationPhrase))
            if (response.isSuccessful) {
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Failed to delete account: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    // ==================== Academic Profile ====================

    override fun getAcademicProfile(): Flow<Resource<UserAcademicProfile>> = flow {
        emit(Resource.loading())
        try {
            val response = api.getAcademicProfile()
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    emit(Resource.success(ProfileMapper.toAcademicProfileDomain(dto)))
                } ?: emit(Resource.error(Exception("Empty response body")))
            } else {
                emit(Resource.error(
                    Exception("Failed to get academic profile: ${response.code()}"),
                    response.message()
                ))
            }
        } catch (e: Exception) {
            emit(Resource.error(e, e.message))
        }
    }

    override suspend fun updateAcademicBasicInfo(
        role: String,
        roleDescription: String?,
        bio: String?
    ): Resource<UserAcademicProfile> {
        return try {
            val response = api.updateAcademicBasicInfo(
                UpdateAcademicBasicInfoRequestDto(role, roleDescription, bio)
            )
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ProfileMapper.toAcademicProfileDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update academic info: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateStudentInfo(
        academicLevel: String?,
        currentClass: String?,
        major: String?,
        department: String?,
        studentType: String?,
        studentId: String?,
        academicYear: Int?,
        currentSemester: String?,
        enrollmentDate: String?,
        expectedGraduationDate: String?
    ): Resource<UserAcademicProfile> {
        return try {
            val response = api.updateStudentInfo(
                UpdateStudentInfoRequestDto(
                    academicLevel, currentClass, major, department,
                    studentType, studentId, academicYear, currentSemester,
                    enrollmentDate, expectedGraduationDate
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ProfileMapper.toAcademicProfileDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update student info: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateInstitutionInfo(
        name: String,
        type: String,
        country: String,
        city: String?,
        state: String?
    ): Resource<UserAcademicProfile> {
        return try {
            val response = api.updateInstitutionInfo(
                UpdateInstitutionInfoRequestDto(name, type, country, city, state)
            )
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ProfileMapper.toAcademicProfileDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update institution info: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateTeachingInfo(
        teachingSubjects: String?,
        qualifications: String?,
        yearsOfExperience: Int?
    ): Resource<UserAcademicProfile> {
        return try {
            val response = api.updateTeachingInfo(
                UpdateTeachingInfoRequestDto(teachingSubjects, qualifications, yearsOfExperience)
            )
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ProfileMapper.toAcademicProfileDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update teaching info: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateSocialLinks(
        researchInterests: String?,
        website: String?,
        linkedIn: String?,
        gitHub: String?
    ): Resource<UserAcademicProfile> {
        return try {
            val response = api.updateSocialLinks(
                UpdateSocialLinksRequestDto(researchInterests, website, linkedIn, gitHub)
            )
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(ProfileMapper.toAcademicProfileDomain(dto))
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Failed to update social links: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }
}

