package com.example.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for syncing data
 */
@Serializable
data class SyncRequestDto(
    @SerialName("lastSyncedAt")
    val lastSyncedAt: String,
    @SerialName("completedSessions")
    val completedSessions: List<SessionSyncItemDto>
)

/**
 * Session sync item DTO
 */
@Serializable
data class SessionSyncItemDto(
    @SerialName("sessionId")
    val sessionId: String,
    @SerialName("completedPages")
    val completedPages: Int,
    @SerialName("completedAt")
    val completedAt: String,
    @SerialName("notes")
    val notes: String?
)

/**
 * Response DTO for sync operation
 */
@Serializable
data class SyncResponseDto(
    @SerialName("syncedAt")
    val syncedAt: String,
    @SerialName("sessions")
    val sessions: List<StudySessionDto>,
    @SerialName("books")
    val books: List<BookDto>,
    @SerialName("syncedSessionsCount")
    val syncedSessionsCount: Int
)

/**
 * Health check response DTO
 */
@Serializable
data class HealthResponseDto(
    @SerialName("status")
    val status: String,
    @SerialName("database")
    val database: String,
    @SerialName("version")
    val version: String,
    @SerialName("timestamp")
    val timestamp: String
)

/**
 * Generic error response DTO
 */
@Serializable
data class ErrorResponseDto(
    @SerialName("error")
    val error: String
)

/**
 * Generic message response DTO
 */
@Serializable
data class MessageResponseDto(
    @SerialName("message")
    val message: String
)

