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
    @SerialName("title")
    val title: String,
    @SerialName("subject")
    val subject: String,
    @SerialName("totalPages")
    val totalPages: Int,
    @SerialName("difficulty")
    val difficulty: Int = 1,
    @SerialName("priority")
    val priority: Int = 1,
    @SerialName("targetEndDate")
    val targetEndDate: String? = null
)

/**
 * Request DTO for updating a book
 */
@Serializable
data class UpdateBookRequestDto(
    @SerialName("title")
    val title: String,
    @SerialName("subject")
    val subject: String,
    @SerialName("totalPages")
    val totalPages: Int,
    @SerialName("difficulty")
    val difficulty: Int,
    @SerialName("priority")
    val priority: Int,
    @SerialName("targetEndDate")
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
    @SerialName("title")
    val title: String,
    @SerialName("startPage")
    val startPage: Int,
    @SerialName("endPage")
    val endPage: Int,
    @SerialName("orderIndex")
    val orderIndex: Int
)

/**
 * Request DTO for updating a chapter
 */
@Serializable
data class UpdateChapterRequestDto(
    @SerialName("title")
    val title: String,
    @SerialName("startPage")
    val startPage: Int,
    @SerialName("endPage")
    val endPage: Int,
    @SerialName("orderIndex")
    val orderIndex: Int
)

/**
 * Request DTO for ignoring a chapter
 */
@Serializable
data class IgnoreChapterRequestDto(
    @SerialName("reason")
    val reason: String? = null
)

