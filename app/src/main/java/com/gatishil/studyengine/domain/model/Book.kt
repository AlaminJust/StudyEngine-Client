package com.gatishil.studyengine.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Domain model for Book
 */
data class Book(
    val id: String,
    val userId: String,
    val title: String,
    val subject: String,
    val totalPages: Int,
    val effectiveTotalPages: Int,
    val difficulty: Int,
    val priority: Int,
    val targetEndDate: LocalDate?,
    val createdAt: LocalDateTime,
    val ignoredChapterCount: Int,
    val studyPlan: StudyPlan?,
    val chapters: List<Chapter>
) {
    val progressPercentage: Float
        get() = if (effectiveTotalPages > 0) {
            // Calculate based on completed pages in study sessions
            0f // This will be calculated from sessions
        } else 0f
}

/**
 * Domain model for Chapter
 */
data class Chapter(
    val id: String,
    val bookId: String,
    val title: String,
    val startPage: Int,
    val endPage: Int,
    val orderIndex: Int,
    val pageCount: Int,
    val isIgnored: Boolean,
    val ignoreReason: String?
)

/**
 * Domain model for creating a new book
 */
data class CreateBookRequest(
    val title: String,
    val subject: String,
    val totalPages: Int,
    val difficulty: Int = 1,
    val priority: Int = 1,
    val targetEndDate: LocalDate? = null
)

/**
 * Domain model for updating a book
 */
data class UpdateBookRequest(
    val title: String,
    val subject: String,
    val totalPages: Int,
    val difficulty: Int,
    val priority: Int,
    val targetEndDate: LocalDate?
)

/**
 * Domain model for creating a chapter
 */
data class CreateChapterRequest(
    val title: String,
    val startPage: Int,
    val endPage: Int,
    val orderIndex: Int
)

/**
 * Domain model for updating a chapter
 */
data class UpdateChapterRequest(
    val title: String,
    val startPage: Int,
    val endPage: Int,
    val orderIndex: Int
)

