package com.gatishil.studyengine.data.repository

import android.util.Log
import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.data.mapper.ExamMapper.toDomain
import com.gatishil.studyengine.data.mapper.LiveExamMapper.toDomain
import com.gatishil.studyengine.data.remote.api.StudyEngineApi
import com.gatishil.studyengine.data.remote.dto.JoinLiveExamRequestDto
import com.gatishil.studyengine.domain.model.CompletedLiveExam
import com.gatishil.studyengine.domain.model.ExamQuestionSet
import com.gatishil.studyengine.domain.model.LiveExam
import com.gatishil.studyengine.domain.model.LiveExamStandings
import com.gatishil.studyengine.domain.repository.LiveExamRepository
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LiveExamRepository"

@Singleton
class LiveExamRepositoryImpl @Inject constructor(
    private val api: StudyEngineApi
) : LiveExamRepository {

    override suspend fun getLiveExams(): Resource<List<LiveExam>> {
        return try {
            val response = api.getLiveExams()

            if (response.isSuccessful) {
                val exams = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(exams)
            } else {
                Log.e(TAG, "Failed to get live exams: ${response.code()}")
                Resource.error(
                    Exception("Failed to get live exams: ${response.code()}"),
                    "Failed to load live exams"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting live exams", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getLiveExamById(id: String): Resource<LiveExam> {
        return try {
            val response = api.getLiveExamById(id)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Live exam not found"), "Live exam not found")
            } else {
                Log.e(TAG, "Failed to get live exam: ${response.code()}")
                Resource.error(
                    Exception("Failed to get live exam: ${response.code()}"),
                    "Failed to load live exam"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting live exam", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun joinLiveExam(id: String, accessCode: String?): Resource<ExamQuestionSet> {
        return try {
            val response = api.joinLiveExam(id, JoinLiveExamRequestDto(accessCode = accessCode))

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Failed to join live exam"), "Failed to join live exam")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to join live exam: ${response.code()}, $errorBody")
                val errorMessage = try {
                    val json = JSONObject(errorBody ?: "{}")
                    json.optString("message").takeIf { it.isNotBlank() }
                        ?: json.optString("title").takeIf { it.isNotBlank() }
                        ?: "Failed to join exam"
                } catch (e: Exception) {
                    errorBody ?: "Failed to join exam"
                }
                Resource.error(Exception(errorMessage), errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error joining live exam", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getCompletedLiveExams(count: Int): Resource<List<CompletedLiveExam>> {
        return try {
            val response = api.getCompletedLiveExams(count)

            if (response.isSuccessful) {
                val exams = response.body()?.map { it.toDomain() } ?: emptyList()
                Resource.success(exams)
            } else {
                Log.e(TAG, "Failed to get completed live exams: ${response.code()}")
                Resource.error(
                    Exception("Failed to get completed live exams: ${response.code()}"),
                    "Failed to load completed exams"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting completed live exams", e)
            Resource.error(e, e.message)
        }
    }

    override suspend fun getLiveExamStandings(id: String): Resource<LiveExamStandings> {
        return try {
            val response = api.getLiveExamStandings(id)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Resource.success(dto.toDomain())
                } ?: Resource.error(Exception("Standings not found"), "Standings not found")
            } else {
                Log.e(TAG, "Failed to get standings: ${response.code()}")
                Resource.error(
                    Exception("Failed to get standings: ${response.code()}"),
                    "Failed to load standings"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting standings", e)
            Resource.error(e, e.message)
        }
    }
}
