package com.gatishil.studyengine.data.repository

import android.util.Log
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.mapper.ExamMapper.toDomain
import com.gatishil.studyengine.data.mapper.ExamMapper.toDto
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.domain.model.*
import com.gatishil.studyengine.domain.repository.ExamRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ExamRepository"

@Singleton
class ExamRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi
) : ExamRepository {

    // ==================== Tag Operations ====================

    override suspend fun getTags(): Resource<List<Tag>> {
        return try {
            val response = api.getTags()

            if (response.isSuccessful) {
                val tags = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(tags)
            } else {
                Log.e(TAG, "Failed to get tags: ${response.code()}")
                Resource.error(
                    Exception("Failed to get tags: ${response.code()}"),
                    "Failed to load tags"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tags", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getTagsByCategories(categoryIds: List<String>): Resource<List<Tag>> {
        return try {
            val response = api.getTagsByCategories(categoryIds)

            if (response.isSuccessful) {
                val tags = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(tags)
            } else {
                Log.e(TAG, "Failed to get tags by categories: ${response.code()}")
                Resource.error(
                    Exception("Failed to get tags by categories: ${response.code()}"),
                    "Failed to load tags"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tags by categories", e)
            Resource.error(e, e.message)
        }
    }

    // ==================== Category Operations ====================

    override suspend fun getCategories(includeInactive: Boolean): Resource<List<Category>> {
        return try {
            val response = api.getCategories(includeInactive)

            if (response.isSuccessful) {
                val categories = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(categories)
            } else {
                Log.e(TAG, "Failed to get categories: ${response.code()}")
                Resource.error(
                    Exception("Failed to get categories: ${response.code()}"),
                    "Failed to load categories"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting categories", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getCategoriesWithSubjects(includeInactive: Boolean): Resource<List<CategoryWithSubjects>> {
        return try {
            val response = api.getCategoriesWithSubjects(includeInactive)

            if (response.isSuccessful) {
                val categories = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(categories)
            } else {
                Log.e(TAG, "Failed to get categories with subjects: ${response.code()}")
                Resource.error(
                    Exception("Failed to get categories with subjects: ${response.code()}"),
                    "Failed to load categories"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting categories with subjects", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getCategoryById(categoryId: String): Resource<Category> {
        return try {
            val response = api.getCategoryById(categoryId)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Category not found"), "Category not found")
            } else {
                Log.e(TAG, "Failed to get category: ${response.code()}")
                Resource.error(
                    Exception("Failed to get category: ${response.code()}"),
                    "Failed to load category"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getCategoryWithSubjects(categoryId: String): Resource<CategoryWithSubjects> {
        return try {
            val response = api.getCategoryWithSubjects(categoryId)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Category not found"), "Category not found")
            } else {
                Log.e(TAG, "Failed to get category with subjects: ${response.code()}")
                Resource.error(
                    Exception("Failed to get category with subjects: ${response.code()}"),
                    "Failed to load category"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category with subjects", e)
            Resource.error(e, e.message)
        }
    }

    // ==================== Subject Operations ====================

    override suspend fun getSubjects(includeInactive: Boolean): Resource<List<Subject>> {
        return try {
            val response = api.getSubjects(includeInactive)

            if (response.isSuccessful) {
                val subjects = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(subjects)
            } else {
                Log.e(TAG, "Failed to get subjects: ${response.code()}")
                Resource.error(
                    Exception("Failed to get subjects: ${response.code()}"),
                    "Failed to load subjects"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subjects", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getSubjectsByCategory(categoryId: String, includeInactive: Boolean): Resource<List<Subject>> {
        return try {
            val response = api.getSubjectsByCategory(categoryId, includeInactive)

            if (response.isSuccessful) {
                val subjects = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(subjects)
            } else {
                Log.e(TAG, "Failed to get subjects by category: ${response.code()}")
                Resource.error(
                    Exception("Failed to get subjects by category: ${response.code()}"),
                    "Failed to load subjects"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subjects by category", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getSubjectById(subjectId: String): Resource<Subject> {
        return try {
            val response = api.getSubjectById(subjectId)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Subject not found"), "Subject not found")
            } else {
                Log.e(TAG, "Failed to get subject: ${response.code()}")
                Resource.error(
                    Exception("Failed to get subject: ${response.code()}"),
                    "Failed to load subject"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subject", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getSubjectWithChapters(subjectId: String): Resource<SubjectWithChapters> {
        return try {
            val response = api.getSubjectWithChapters(subjectId)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Subject not found"), "Subject not found")
            } else {
                Log.e(TAG, "Failed to get subject with chapters: ${response.code()}")
                Resource.error(
                    Exception("Failed to get subject with chapters: ${response.code()}"),
                    "Failed to load subject"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subject with chapters", e)
            Resource.error(e, e.message)
        }
    }

    // ==================== Subject Chapter Operations ====================

    override suspend fun getSubjectChapters(subjectId: String, includeInactive: Boolean): Resource<List<SubjectChapter>> {
        return try {
            val response = api.getSubjectChapters(subjectId, includeInactive)

            if (response.isSuccessful) {
                val chapters = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(chapters)
            } else {
                Log.e(TAG, "Failed to get subject chapters: ${response.code()}")
                Resource.error(
                    Exception("Failed to get subject chapters: ${response.code()}"),
                    "Failed to load chapters"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subject chapters", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getSubjectChapterById(chapterId: String): Resource<SubjectChapter> {
        return try {
            val response = api.getSubjectChapterById(chapterId)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Chapter not found"), "Chapter not found")
            } else {
                Log.e(TAG, "Failed to get subject chapter: ${response.code()}")
                Resource.error(
                    Exception("Failed to get subject chapter: ${response.code()}"),
                    "Failed to load chapter"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subject chapter", e)
            Resource.error(e, e.message)
        }
    }

    // ==================== Question Operations ====================

    override suspend fun getAvailableQuestionCount(
        subjectId: String,
        difficulty: QuestionDifficulty?
    ): Resource<Int> {
        return try {
            val response = api.getAvailableQuestionCount(subjectId, difficulty?.name)

            if (response.isSuccessful) {
                val count = response.body()?.availableCount ?: 0
                Resource.success(count)
            } else {
                Log.e(TAG, "Failed to get question count: ${response.code()}")
                Resource.error(
                    Exception("Failed to get question count: ${response.code()}"),
                    "Failed to load question count"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting question count", e)
            Resource.error(e, e.message)
        }
    }

    // ==================== Exam Operations ====================

    override suspend fun startExam(request: StartExamRequest): Resource<ExamQuestionSet> {
        return try {
            val response = api.startExam(request.toDto())

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Failed to start exam"), "Failed to start exam")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to start exam: ${response.code()}, $errorBody")
                Resource.error(
                    Exception("Failed to start exam: ${response.code()}"),
                    errorBody ?: "Failed to start exam"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting exam", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getCurrentExam(): Resource<ExamQuestionSet?> {
        return try {
            val response = api.getCurrentExam()

            when {
                response.isSuccessful -> {
                    val exam = response.body()?.toDomain()
                    Resource.success(exam)
                }
                response.code() == 204 -> {
                    Resource.success(null)
                }
                else -> {
                    Log.e(TAG, "Failed to get current exam: ${response.code()}")
                    Resource.error(
                        Exception("Failed to get current exam: ${response.code()}"),
                        "Failed to load current exam"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current exam", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun submitExam(request: SubmitExamRequest): Resource<ExamResult> {
        return try {
            val response = api.submitExam(request.toDto())

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Failed to submit exam"), "Failed to submit exam")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to submit exam: ${response.code()}, $errorBody")
                Resource.error(
                    Exception("Failed to submit exam: ${response.code()}"),
                    errorBody ?: "Failed to submit exam"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting exam", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getExamResult(examAttemptId: String): Resource<ExamResult> {
        return try {
            val response = api.getExamResult(examAttemptId)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Result not found"), "Result not found")
            } else {
                Log.e(TAG, "Failed to get exam result: ${response.code()}")
                Resource.error(
                    Exception("Failed to get exam result: ${response.code()}"),
                    "Failed to load exam result"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting exam result", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getExamHistory(
        subjectId: String?,
        status: ExamAttemptStatus?,
        page: Int,
        pageSize: Int
    ): Resource<ExamAttemptPagedResponse> {
        return try {
            val statusStr = status?.name
            val response = api.getExamHistory(subjectId, statusStr, page, pageSize)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.success(
                    ExamAttemptPagedResponse(
                        items = emptyList(),
                        totalCount = 0,
                        page = page,
                        pageSize = pageSize,
                        totalPages = 0
                    )
                )
            } else {
                Log.e(TAG, "Failed to get exam history: ${response.code()}")
                Resource.error(
                    Exception("Failed to get exam history: ${response.code()}"),
                    "Failed to load exam history"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting exam history", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun cancelExam(examAttemptId: String): Resource<Unit> {
        return try {
            val response = api.cancelExam(examAttemptId)

            if (response.isSuccessful) {
                Resource.success(Unit)
            } else {
                Log.e(TAG, "Failed to cancel exam: ${response.code()}")
                Resource.error(
                    Exception("Failed to cancel exam: ${response.code()}"),
                    "Failed to cancel exam"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling exam", e)
            Resource.error(e, e.message)
        }
    }
}
