package com.example.studyengine.domain.repository

import com.example.studyengine.core.util.Resource
import com.example.studyengine.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for book operations
 */
interface BookRepository {

    /**
     * Get all books for the current user
     */
    fun getBooks(): Flow<Resource<List<Book>>>

    /**
     * Get a specific book by ID
     */
    suspend fun getBookById(bookId: String): Resource<Book>

    /**
     * Create a new book
     */
    suspend fun createBook(request: CreateBookRequest): Resource<Book>

    /**
     * Update an existing book
     */
    suspend fun updateBook(bookId: String, request: UpdateBookRequest): Resource<Book>

    /**
     * Delete a book
     */
    suspend fun deleteBook(bookId: String): Resource<Boolean>

    /**
     * Get chapters for a book
     */
    fun getChapters(bookId: String): Flow<Resource<List<Chapter>>>

    /**
     * Get a specific chapter
     */
    suspend fun getChapter(bookId: String, chapterId: String): Resource<Chapter>

    /**
     * Add a chapter to a book
     */
    suspend fun addChapter(bookId: String, request: CreateChapterRequest): Resource<Chapter>

    /**
     * Update a chapter
     */
    suspend fun updateChapter(
        bookId: String,
        chapterId: String,
        request: UpdateChapterRequest
    ): Resource<Chapter>

    /**
     * Delete a chapter
     */
    suspend fun deleteChapter(bookId: String, chapterId: String): Resource<Boolean>

    /**
     * Ignore a chapter (exclude from study plan)
     */
    suspend fun ignoreChapter(
        bookId: String,
        chapterId: String,
        reason: String?
    ): Resource<Chapter>

    /**
     * Unignore a chapter
     */
    suspend fun unignoreChapter(bookId: String, chapterId: String): Resource<Chapter>

    /**
     * Get study plan for a book
     */
    suspend fun getStudyPlan(bookId: String): Resource<StudyPlan?>

    /**
     * Create a study plan for a book
     */
    suspend fun createStudyPlan(
        bookId: String,
        request: CreateStudyPlanRequest
    ): Resource<StudyPlan>

    /**
     * Refresh books from remote
     */
    suspend fun refreshBooks(): Resource<List<Book>>
}

