package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user operations
 */
interface UserRepository {

    /**
     * Get current user profile
     */
    fun getCurrentUser(): Flow<Resource<User>>

    /**
     * Update current user profile
     */
    suspend fun updateUser(name: String, email: String, timeZone: String): Resource<User>

    /**
     * Delete current user account
     */
    suspend fun deleteAccount(): Resource<Boolean>

    /**
     * Get user's availability schedule
     */
    fun getAvailabilities(): Flow<Resource<List<UserAvailability>>>

    /**
     * Add availability slot
     */
    suspend fun addAvailability(request: CreateUserAvailabilityRequest): Resource<UserAvailability>

    /**
     * Remove availability slot
     */
    suspend fun removeAvailability(availabilityId: String): Resource<Boolean>

    /**
     * Get schedule overrides
     */
    fun getScheduleOverrides(): Flow<Resource<List<ScheduleOverride>>>

    /**
     * Add schedule override
     */
    suspend fun addScheduleOverride(request: CreateScheduleOverrideRequest): Resource<ScheduleOverride>

    /**
     * Get schedule contexts
     */
    fun getScheduleContexts(): Flow<Resource<List<ScheduleContext>>>

    /**
     * Add schedule context
     */
    suspend fun addScheduleContext(request: CreateScheduleContextRequest): Resource<ScheduleContext>

    /**
     * Refresh user data from remote
     */
    suspend fun refreshUser(): Resource<User>
}

