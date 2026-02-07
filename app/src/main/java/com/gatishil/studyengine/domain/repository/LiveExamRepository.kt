package com.gatishil.studyengine.domain.repository

import com.gatishil.studyengine.core.util.Resource
import com.gatishil.studyengine.domain.model.ExamQuestionSet
import com.gatishil.studyengine.domain.model.LiveExam

interface LiveExamRepository {

    suspend fun getLiveExams(): Resource<List<LiveExam>>

    suspend fun getLiveExamById(id: String): Resource<LiveExam>

    suspend fun joinLiveExam(id: String): Resource<ExamQuestionSet>
}
