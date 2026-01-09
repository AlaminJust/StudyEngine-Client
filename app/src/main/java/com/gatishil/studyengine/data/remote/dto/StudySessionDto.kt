package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Study Session DTO
 */
@Serializable
data class StudySessionDto(
    @SerialName("id")
    val id: String,
    @SerialName("userId")
    val userId: String,
    @SerialName("bookId")
    val bookId: String,
    @SerialName("chapterId")
    val chapterId: String?,
    @SerialName("sessionDate")
    val sessionDate: String,
    @SerialName("startTime")
    val startTime: String,
    @SerialName("endTime")
    val endTime: String,
    @SerialName("plannedPages")
    val plannedPages: Int,
    @SerialName("completedPages")
    val completedPages: Int,
    @SerialName("status")
    val status: String,
    @SerialName("bookTitle")
    val bookTitle: String?,
    @SerialName("chapterTitle")
    val chapterTitle: String?
)

/**
 * Request DTO for completing a session
 */
@Serializable
data class CompleteSessionRequestDto(
    @SerialName("CompletedPages")
    val completedPages: Int,
    @SerialName("Notes")
    val notes: String? = null
)

/**
 * Session Log DTO
 */
@Serializable
data class SessionLogDto(
    @SerialName("id")
    val id: String,
    @SerialName("studySessionId")
    val studySessionId: String,
    @SerialName("actualStartTime")
    val actualStartTime: String,
    @SerialName("actualEndTime")
    val actualEndTime: String?,
    @SerialName("notes")
    val notes: String
)

