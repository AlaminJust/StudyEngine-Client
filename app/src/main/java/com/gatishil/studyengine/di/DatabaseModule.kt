package com.gatishil.studyengine.di

import android.content.Context
import androidx.room.Room
import com.gatishil.studyengine.data.local.StudyEngineDatabase
import com.gatishil.studyengine.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): StudyEngineDatabase {
        return Room.databaseBuilder(
            context,
            StudyEngineDatabase::class.java,
            StudyEngineDatabase.DATABASE_NAME
        )
            .addMigrations(StudyEngineDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: StudyEngineDatabase): UserDao = database.userDao()

    @Provides
    fun provideBookDao(database: StudyEngineDatabase): BookDao = database.bookDao()

    @Provides
    fun provideChapterDao(database: StudyEngineDatabase): ChapterDao = database.chapterDao()

    @Provides
    fun provideStudyPlanDao(database: StudyEngineDatabase): StudyPlanDao = database.studyPlanDao()

    @Provides
    fun provideRecurrenceRuleDao(database: StudyEngineDatabase): RecurrenceRuleDao =
        database.recurrenceRuleDao()

    @Provides
    fun provideStudySessionDao(database: StudyEngineDatabase): StudySessionDao =
        database.studySessionDao()

    @Provides
    fun provideUserAvailabilityDao(database: StudyEngineDatabase): UserAvailabilityDao =
        database.userAvailabilityDao()

    @Provides
    fun provideScheduleOverrideDao(database: StudyEngineDatabase): ScheduleOverrideDao =
        database.scheduleOverrideDao()

    @Provides
    fun provideScheduleContextDao(database: StudyEngineDatabase): ScheduleContextDao =
        database.scheduleContextDao()
}

