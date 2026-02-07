package com.gatishil.studyengine.di

import com.gatishil.studyengine.data.repository.AuthRepositoryImpl
import com.gatishil.studyengine.data.repository.BookRepositoryImpl
import com.gatishil.studyengine.data.repository.ExamRepositoryImpl
import com.gatishil.studyengine.data.repository.LiveExamRepositoryImpl
import com.gatishil.studyengine.data.repository.NotificationRepositoryImpl
import com.gatishil.studyengine.data.repository.ProfileRepositoryImpl
import com.gatishil.studyengine.data.repository.ReminderRepositoryImpl
import com.gatishil.studyengine.data.repository.SessionRepositoryImpl
import com.gatishil.studyengine.data.repository.UserRepositoryImpl
import com.gatishil.studyengine.domain.repository.AuthRepository
import com.gatishil.studyengine.domain.repository.BookRepository
import com.gatishil.studyengine.domain.repository.ExamRepository
import com.gatishil.studyengine.domain.repository.LiveExamRepository
import com.gatishil.studyengine.domain.repository.NotificationRepository
import com.gatishil.studyengine.domain.repository.ProfileRepository
import com.gatishil.studyengine.domain.repository.ReminderRepository
import com.gatishil.studyengine.domain.repository.SessionRepository
import com.gatishil.studyengine.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        bookRepositoryImpl: BookRepositoryImpl
    ): BookRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        reminderRepositoryImpl: ReminderRepositoryImpl
    ): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindExamRepository(
        examRepositoryImpl: ExamRepositoryImpl
    ): ExamRepository

    @Binds
    @Singleton
    abstract fun bindLiveExamRepository(
        liveExamRepositoryImpl: LiveExamRepositoryImpl
    ): LiveExamRepository
}

