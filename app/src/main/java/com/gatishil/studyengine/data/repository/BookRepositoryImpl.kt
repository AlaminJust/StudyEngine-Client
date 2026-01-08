package com.gatishil.studyengine.data.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.local.dao.BookDao
import com.gatishil.studyengine.data.local.dao.ChapterDao
import com.gatishil.studyengine.data.local.dao.RecurrenceRuleDao
import com.gatishil.studyengine.data.local.dao.StudyPlanDao
import com.gatishil.studyengine.data.mapper.BookMapper
import com.gatishil.studyengine.data.mapper.ChapterMapper
import com.gatishil.studyengine.data.mapper.RecurrenceRuleMapper
import com.gatishil.studyengine.data.mapper.StudyPlanMapper
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.IgnoreChapterRequestDto
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val studyPlanDao: StudyPlanDao,
    private val recurrenceRuleDao: RecurrenceRuleDao
) : BookRepository {

    override fun getBooks(): Flow<Resource<List<Book>>> {
        return bookDao.getAllBooks()
            .map { bookEntities ->
                val books = bookEntities.map { bookEntity ->
                    val chapters = chapterDao.getChaptersByBookId(bookEntity.id).first()
                        .map { with(ChapterMapper) { it.toDomain() } }

                    val studyPlanEntity = studyPlanDao.getStudyPlanByBookId(bookEntity.id)
                    val recurrenceRule = studyPlanEntity?.let { sp ->
                        recurrenceRuleDao.getRecurrenceRuleByStudyPlanId(sp.id)
                            ?.let { with(RecurrenceRuleMapper) { it.toDomain() } }
                    }
                    val studyPlan = studyPlanEntity?.let {
                        with(StudyPlanMapper) { it.toDomain(recurrenceRule) }
                    }

                    with(BookMapper) { bookEntity.toDomain(studyPlan, chapters) }
                }
                Resource.success(books)
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override suspend fun getBookById(bookId: String): Resource<Book> {
        return try {
            val bookEntity = bookDao.getBookById(bookId)
                ?: return Resource.error(Exception("Book not found"))

            val chapters = chapterDao.getChaptersByBookId(bookId).first()
                .map { with(ChapterMapper) { it.toDomain() } }

            val studyPlanEntity = studyPlanDao.getStudyPlanByBookId(bookId)
            val recurrenceRule = studyPlanEntity?.let { sp ->
                recurrenceRuleDao.getRecurrenceRuleByStudyPlanId(sp.id)
                    ?.let { with(RecurrenceRuleMapper) { it.toDomain() } }
            }
            val studyPlan = studyPlanEntity?.let {
                with(StudyPlanMapper) { it.toDomain(recurrenceRule) }
            }

            Resource.success(with(BookMapper) { bookEntity.toDomain(studyPlan, chapters) })
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun createBook(request: CreateBookRequest): Resource<Book> {
        return try {
            val response = api.createBook(with(BookMapper) { request.toDto() })

            if (response.isSuccessful) {
                response.body()?.let { bookDto ->
                    // Save to local database
                    bookDao.insertBook(with(BookMapper) { bookDto.toEntity() })
                    bookDto.chapters.forEach { chapterDto ->
                        chapterDao.insertChapter(with(ChapterMapper) { chapterDto.toEntity() })
                    }
                    bookDto.studyPlan?.let { studyPlanDto ->
                        studyPlanDao.insertStudyPlan(with(StudyPlanMapper) { studyPlanDto.toEntity() })
                        studyPlanDto.recurrenceRule?.let { ruleDto ->
                            recurrenceRuleDao.insertRecurrenceRule(
                                with(RecurrenceRuleMapper) { ruleDto.toEntity() }
                            )
                        }
                    }

                    Resource.success(with(BookMapper) { bookDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Create book failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateBook(bookId: String, request: UpdateBookRequest): Resource<Book> {
        return try {
            val response = api.updateBook(bookId, with(BookMapper) { request.toDto() })

            if (response.isSuccessful) {
                response.body()?.let { bookDto ->
                    bookDao.insertBook(with(BookMapper) { bookDto.toEntity() })
                    Resource.success(with(BookMapper) { bookDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Update book failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun deleteBook(bookId: String): Resource<Boolean> {
        return try {
            val response = api.deleteBook(bookId)

            if (response.isSuccessful) {
                // Delete from local database
                bookDao.deleteBookById(bookId)
                chapterDao.deleteChaptersByBookId(bookId)
                studyPlanDao.deleteStudyPlanByBookId(bookId)
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Delete book failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override fun getChapters(bookId: String): Flow<Resource<List<Chapter>>> {
        return chapterDao.getChaptersByBookId(bookId)
            .map { chapters ->
                Resource.success(chapters.map { with(ChapterMapper) { it.toDomain() } })
            }
            .catch { e -> emit(Resource.error(e, e.message)) }
    }

    override suspend fun getChapter(bookId: String, chapterId: String): Resource<Chapter> {
        return try {
            val chapter = chapterDao.getChapterById(chapterId)
                ?: return Resource.error(Exception("Chapter not found"))

            Resource.success(with(ChapterMapper) { chapter.toDomain() })
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun addChapter(bookId: String, request: CreateChapterRequest): Resource<Chapter> {
        return try {
            val response = api.addChapter(bookId, with(ChapterMapper) { request.toDto() })

            if (response.isSuccessful) {
                response.body()?.let { chapterDto ->
                    chapterDao.insertChapter(with(ChapterMapper) { chapterDto.toEntity() })
                    Resource.success(with(ChapterMapper) { chapterDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Add chapter failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun updateChapter(
        bookId: String,
        chapterId: String,
        request: UpdateChapterRequest
    ): Resource<Chapter> {
        return try {
            val response = api.updateChapter(bookId, chapterId, with(ChapterMapper) { request.toDto() })

            if (response.isSuccessful) {
                response.body()?.let { chapterDto ->
                    chapterDao.insertChapter(with(ChapterMapper) { chapterDto.toEntity() })
                    Resource.success(with(ChapterMapper) { chapterDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Update chapter failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun deleteChapter(bookId: String, chapterId: String): Resource<Boolean> {
        return try {
            val response = api.deleteChapter(bookId, chapterId)

            if (response.isSuccessful) {
                chapterDao.getChapterById(chapterId)?.let { chapterDao.deleteChapter(it) }
                Resource.success(true)
            } else {
                Resource.error(
                    Exception("Delete chapter failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun ignoreChapter(
        bookId: String,
        chapterId: String,
        reason: String?
    ): Resource<Chapter> {
        return try {
            val response = api.ignoreChapter(bookId, chapterId, IgnoreChapterRequestDto(reason))

            if (response.isSuccessful) {
                response.body()?.let { chapterDto ->
                    chapterDao.insertChapter(with(ChapterMapper) { chapterDto.toEntity() })
                    Resource.success(with(ChapterMapper) { chapterDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Ignore chapter failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun unignoreChapter(bookId: String, chapterId: String): Resource<Chapter> {
        return try {
            val response = api.unignoreChapter(bookId, chapterId)

            if (response.isSuccessful) {
                response.body()?.let { chapterDto ->
                    chapterDao.insertChapter(with(ChapterMapper) { chapterDto.toEntity() })
                    Resource.success(with(ChapterMapper) { chapterDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Unignore chapter failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun getStudyPlan(bookId: String): Resource<StudyPlan?> {
        return try {
            val response = api.getStudyPlan(bookId)

            if (response.isSuccessful) {
                response.body()?.let { studyPlanDto ->
                    studyPlanDao.insertStudyPlan(with(StudyPlanMapper) { studyPlanDto.toEntity() })
                    studyPlanDto.recurrenceRule?.let { ruleDto ->
                        recurrenceRuleDao.insertRecurrenceRule(
                            with(RecurrenceRuleMapper) { ruleDto.toEntity() }
                        )
                    }
                    Resource.success(with(StudyPlanMapper) { studyPlanDto.toDomain() })
                } ?: Resource.success(null)
            } else if (response.code() == 404) {
                Resource.success(null)
            } else {
                Resource.error(
                    Exception("Get study plan failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            // Try local cache
            val localPlan = studyPlanDao.getStudyPlanByBookId(bookId)
            if (localPlan != null) {
                val recurrenceRule = recurrenceRuleDao.getRecurrenceRuleByStudyPlanId(localPlan.id)
                    ?.let { with(RecurrenceRuleMapper) { it.toDomain() } }
                Resource.success(with(StudyPlanMapper) { localPlan.toDomain(recurrenceRule) })
            } else {
                Resource.error(e, e.message)
            }
        }
    }

    override suspend fun createStudyPlan(
        bookId: String,
        request: CreateStudyPlanRequest
    ): Resource<StudyPlan> {
        return try {
            val response = api.createStudyPlan(bookId, with(StudyPlanMapper) { request.toDto() })

            if (response.isSuccessful) {
                response.body()?.let { studyPlanDto ->
                    studyPlanDao.insertStudyPlan(with(StudyPlanMapper) { studyPlanDto.toEntity() })
                    studyPlanDto.recurrenceRule?.let { ruleDto ->
                        recurrenceRuleDao.insertRecurrenceRule(
                            with(RecurrenceRuleMapper) { ruleDto.toEntity() }
                        )
                    }
                    Resource.success(with(StudyPlanMapper) { studyPlanDto.toDomain() })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Create study plan failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }

    override suspend fun refreshBooks(): Resource<List<Book>> {
        return try {
            val response = api.getBooks()

            if (response.isSuccessful) {
                response.body()?.let { bookDtos ->
                    // Clear and repopulate local cache
                    bookDao.deleteAllBooks()
                    chapterDao.deleteAllChapters()
                    studyPlanDao.deleteAllStudyPlans()
                    recurrenceRuleDao.deleteAllRecurrenceRules()

                    bookDtos.forEach { bookDto ->
                        bookDao.insertBook(with(BookMapper) { bookDto.toEntity() })
                        bookDto.chapters.forEach { chapterDto ->
                            chapterDao.insertChapter(with(ChapterMapper) { chapterDto.toEntity() })
                        }
                        bookDto.studyPlan?.let { studyPlanDto ->
                            studyPlanDao.insertStudyPlan(with(StudyPlanMapper) { studyPlanDto.toEntity() })
                            studyPlanDto.recurrenceRule?.let { ruleDto ->
                                recurrenceRuleDao.insertRecurrenceRule(
                                    with(RecurrenceRuleMapper) { ruleDto.toEntity() }
                                )
                            }
                        }
                    }

                    Resource.success(bookDtos.map { with(BookMapper) { it.toDomain() } })
                } ?: Resource.error(Exception("Empty response body"))
            } else {
                Resource.error(
                    Exception("Refresh books failed: ${response.code()}"),
                    response.message()
                )
            }
        } catch (e: Exception) {
            Resource.error(e, e.message)
        }
    }
}

