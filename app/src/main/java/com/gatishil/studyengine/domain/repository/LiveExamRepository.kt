package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.CompletedLiveExam
import com.gatishil.studyengine.domain.model.ExamQuestionSet
import com.gatishil.studyengine.domain.model.LiveExam
import com.gatishil.studyengine.domain.model.LiveExamStandings

interface LiveExamRepository {

    suspend fun getLiveExams(): Resource<List<LiveExam>>

    suspend fun getLiveExamById(id: String): Resource<LiveExam>

    suspend fun joinLiveExam(id: String, accessCode: String? = null): Resource<ExamQuestionSet>

    suspend fun getCompletedLiveExams(count: Int = 10): Resource<List<CompletedLiveExam>>

    suspend fun getLiveExamStandings(id: String): Resource<LiveExamStandings>
}
