package com.gatishil.studyengine.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Book DTO
 */
@Serializable
data class BookDto(
    @SerialName("id")
    val id: String,
    @SerialName("userId")
    val userId: String,
    @SerialName("title")
    val title: String,
    @SerialName("subject")
    val subject: String,
    @SerialName("totalPages")
    val totalPages: Int,
    @SerialName("effectiveTotalPages")
    val effectiveTotalPages: Int,
    @SerialName("difficulty")
    val difficulty: Int,
    @SerialName("priority")
    val priority: Int,
    @SerialName("targetEndDate")
    val targetEndDate: String?,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("ignoredChapterCount")
    val ignoredChapterCount: Int,
    @SerialName("completedPages")
    val completedPages: Int = 0,
    @SerialName("remainingPages")
    val remainingPages: Int = 0,
    @SerialName("progressPercentage")
    val progressPercentage: Double = 0.0,
    @SerialName("studyPlan")
    val studyPlan: StudyPlanDto?,
    @SerialName("chapters")
    val chapters: List<ChapterDto>
)

/**
 * Request DTO for creating a book
 */
@Serializable
data class CreateBookRequestDto(
    @SerialName("Title")
    val title: String,
    @SerialName("Subject")
    val subject: String,
    @SerialName("TotalPages")
    val totalPages: Int,
    @SerialName("Difficulty")
    val difficulty: Int = 1,
    @SerialName("Priority")
    val priority: Int = 1,
    @SerialName("TargetEndDate")
    val targetEndDate: String? = null
)

/**
 * Request DTO for updating a book
 */
@Serializable
data class UpdateBookRequestDto(
    @SerialName("Title")
    val title: String,
    @SerialName("Subject")
    val subject: String,
    @SerialName("TotalPages")
    val totalPages: Int,
    @SerialName("Difficulty")
    val difficulty: Int,
    @SerialName("Priority")
    val priority: Int,
    @SerialName("TargetEndDate")
    val targetEndDate: String?
)

/**
 * Chapter DTO
 */
@Serializable
data class ChapterDto(
    @SerialName("id")
    val id: String,
    @SerialName("bookId")
    val bookId: String,
    @SerialName("title")
    val title: String,
    @SerialName("startPage")
    val startPage: Int,
    @SerialName("endPage")
    val endPage: Int,
    @SerialName("orderIndex")
    val orderIndex: Int,
    @SerialName("pageCount")
    val pageCount: Int,
    @SerialName("isIgnored")
    val isIgnored: Boolean,
    @SerialName("ignoreReason")
    val ignoreReason: String?
)

/**
 * Request DTO for creating a chapter
 */
@Serializable
data class CreateChapterRequestDto(
    @SerialName("Title")
    val title: String,
    @SerialName("StartPage")
    val startPage: Int,
    @SerialName("EndPage")
    val endPage: Int,
    @SerialName("OrderIndex")
    val orderIndex: Int
)

/**
 * Request DTO for updating a chapter
 */
@Serializable
data class UpdateChapterRequestDto(
    @SerialName("Title")
    val title: String,
    @SerialName("StartPage")
    val startPage: Int,
    @SerialName("EndPage")
    val endPage: Int,
    @SerialName("OrderIndex")
    val orderIndex: Int
)

/**
 * Request DTO for ignoring a chapter
 */
@Serializable
data class IgnoreChapterRequestDto(
    @SerialName("Reason")
    val reason: String? = null
)

