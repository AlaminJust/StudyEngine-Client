package com.example.studyengine.di

import com.example.studyengine.data.repository.AuthRepositoryImpl
import com.example.studyengine.data.repository.BookRepositoryImpl
import com.example.studyengine.data.repository.SessionRepositoryImpl
import com.example.studyengine.data.repository.UserRepositoryImpl
import com.example.studyengine.domain.repository.AuthRepository
import com.example.studyengine.domain.repository.BookRepository
import com.example.studyengine.domain.repository.SessionRepository
import com.example.studyengine.domain.repository.UserRepository
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
}

