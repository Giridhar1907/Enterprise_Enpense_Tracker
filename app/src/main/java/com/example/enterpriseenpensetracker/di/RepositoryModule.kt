package com.example.enterpriseenpensetracker.di

import com.example.enterpriseenpensetracker.data.repository.AuthRepositoryImpl
import com.example.enterpriseenpensetracker.data.repository.ExpenseRepositoryImpl
import com.example.enterpriseenpensetracker.domain.repository.AuthRepository
import com.example.enterpriseenpensetracker.domain.repository.ExpenseRepository
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
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository
}
