package com.gatishil.studyengine.data.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.local.dao.ScheduleContextDao
import com.gatishil.studyengine.data.local.dao.ScheduleOverrideDao
import com.gatishil.studyengine.data.local.dao.UserAvailabilityDao
import com.gatishil.studyengine.data.local.dao.UserDao
import com.gatishil.studyengine.data.mapper.ScheduleMapper
import com.gatishil.studyengine.data.mapper.UserMapper
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.UpdateUserRequestDto
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi,
    private val userDao: UserDao,
    private val availabilityDao: UserAvailabilityDao,
    private val scheduleOverrideDao: ScheduleOverrideDao,
    private val scheduleContextDao: ScheduleContextDao
) : UserRepository {

    override fun getCurrentUser(): Flow<Resource<User>> {
        return userDao.getUserById("current")
            .map { userEntity ->
                if (userEntity != null) {
                    Resource.success(with(UserMapper) { userEntity.toDomain() })
                } else {
                    Resource.error(Exception("User not found"))
                }
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override suspend fun updateUser(name: String, email: String, timeZone: String): Resource<User> {
        return try {
            val response = api.updateCurrentUser(
                UpdateUserRequestDto(name, email, timeZone)
            )

            if (response.isSuccessful) {
                response.body()?.let { userDto ->
                    userDao.insertUser(with(UserMapper) { userDto.toEntity() })
                    Resource.success(with(UserMapper) { userDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Update user failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun deleteAccount(): Resource<Boolean> {
        return try {
            val response = api.deleteCurrentUser()

            if (response.isSuccessful) {
                userDao.deleteAllUsers()
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Delete account failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override fun getAvailabilities(): Flow<Resource<List<UserAvailability>>> {
        return availabilityDao.getAllAvailabilities()
            .map { availabilities ->
                Resource.success(availabilities.map {
                    with(ScheduleMapper) { it.toDomain() }
                })
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override suspend fun addAvailability(
        request: CreateUserAvailabilityRequest
    ): Resource<UserAvailability> {
        return try {
            val response = api.addAvailability(with(ScheduleMapper) { request.toDto() })

            if (response.isSuccessful) {
                response.body()?.let { availabilityDto ->
                    availabilityDao.insertAvailability(
                        with(ScheduleMapper) { availabilityDto.toEntity() }
                    )
                    Resource.success(with(ScheduleMapper) { availabilityDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Add availability failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun removeAvailability(availabilityId: String): Resource<Boolean> {
        return try {
            val response = api.removeAvailability(availabilityId)

            if (response.isSuccessful) {
                availabilityDao.deleteAvailabilityById(availabilityId)
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Remove availability failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override fun getScheduleOverrides(): Flow<Resource<List<ScheduleOverride>>> {
        return scheduleOverrideDao.getAllOverrides()
            .map { overrides ->
                Resource.success(overrides.map {
                    with(ScheduleMapper) { it.toDomain() }
                })
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override suspend fun addScheduleOverride(
        request: CreateScheduleOverrideRequest
    ): Resource<ScheduleOverride> {
        return try {
            val response = api.addScheduleOverride(with(ScheduleMapper) { request.toDto() })

            if (response.isSuccessful) {
                response.body()?.let { overrideDto ->
                    scheduleOverrideDao.insertOverride(
                        with(ScheduleMapper) { overrideDto.toEntity() }
                    )
                    Resource.success(with(ScheduleMapper) { overrideDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Add schedule override failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override fun getScheduleContexts(): Flow<Resource<List<ScheduleContext>>> {
        return scheduleContextDao.getAllContexts()
            .map { contexts ->
                Resource.success(contexts.map {
                    with(ScheduleMapper) { it.toDomain() }
                })
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override suspend fun addScheduleContext(
        request: CreateScheduleContextRequest
    ): Resource<ScheduleContext> {
        return try {
            val response = api.addScheduleContext(with(ScheduleMapper) { request.toDto() })

            if (response.isSuccessful) {
                response.body()?.let { contextDto ->
                    scheduleContextDao.insertContext(
                        with(ScheduleMapper) { contextDto.toEntity() }
                    )
                    Resource.success(with(ScheduleMapper) { contextDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Add schedule context failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun refreshUser(): Resource<User> {
        return try {
            val response = api.getCurrentUser()

            if (response.isSuccessful) {
                response.body()?.let { userDto ->
                    userDao.insertUser(with(UserMapper) { userDto.toEntity() })

                    // Also refresh availabilities, overrides, and contexts
                    refreshAvailabilities()
                    refreshScheduleOverrides()
                    refreshScheduleContexts()

                    Resource.success(with(UserMapper) { userDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Refresh user failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    private suspend fun refreshAvailabilities() {
        try {
            val response = api.getAvailabilities()
            if (response.isSuccessful) {
                response.body()?.let { availabilities ->
                    availabilityDao.deleteAllAvailabilities()
                    availabilities.forEach { availabilityDto ->
                        availabilityDao.insertAvailability(
                            with(ScheduleMapper) { availabilityDto.toEntity() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail, keep local cache
        }
    }

    private suspend fun refreshScheduleOverrides() {
        try {
            val response = api.getScheduleOverrides()
            if (response.isSuccessful) {
                response.body()?.let { overrides ->
                    scheduleOverrideDao.deleteAllOverrides()
                    overrides.forEach { overrideDto ->
                        scheduleOverrideDao.insertOverride(
                            with(ScheduleMapper) { overrideDto.toEntity() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail, keep local cache
        }
    }

    private suspend fun refreshScheduleContexts() {
        try {
            val response = api.getScheduleContexts()
            if (response.isSuccessful) {
                response.body()?.let { contexts ->
                    scheduleContextDao.deleteAllContexts()
                    contexts.forEach { contextDto ->
                        scheduleContextDao.insertContext(
                            with(ScheduleMapper) { contextDto.toEntity() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail, keep local cache
        }
    }
}

