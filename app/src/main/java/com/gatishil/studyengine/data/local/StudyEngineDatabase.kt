package com.gatishil.studyengine.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gatishil.studyengine.data.local.dao.*
import com.gatishil.studyengine.data.local.entity.*

/**
 * Room database for StudyEngine
 */
@Database(
    entities = [
        UserEntity::class,
        BookEntity::class,
        ChapterEntity::class,
        StudyPlanEntity::class,
        RecurrenceRuleEntity::class,
        StudySessionEntity::class,
        UserAvailabilityEntity::class,
        ScheduleOverrideEntity::class,
        ScheduleContextEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class StudyEngineDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun recurrenceRuleDao(): RecurrenceRuleDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun userAvailabilityDao(): UserAvailabilityDao
    abstract fun scheduleOverrideDao(): ScheduleOverrideDao
    abstract fun scheduleContextDao(): ScheduleContextDao

    companion object {
        const val DATABASE_NAME = "study_engine_db"
    }
}

