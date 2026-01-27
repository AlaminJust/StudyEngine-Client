package com.gatishil.studyengine.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
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

        /**
         * Migration from version 1 to 2:
         * - Added completedPages, remainingPages, progressPercentage columns to books table
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN completedPages INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN remainingPages INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN progressPercentage REAL NOT NULL DEFAULT 0.0")
            }
        }
    }
}

