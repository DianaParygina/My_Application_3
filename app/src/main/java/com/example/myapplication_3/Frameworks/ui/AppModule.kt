package com.example.myapplication_3.Frameworks.ui // Или другой подходящий пакет

import android.content.Context
import com.example.myapplication_3.Frameworks.database.PersonDatabaseHelper
import com.example.myapplication_3.repository.ExpenseRepositoryImpl
import com.example.myapplication_3.repository.IncomeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePersonDatabaseHelper(@ApplicationContext context: Context): PersonDatabaseHelper {
        return PersonDatabaseHelper(context)
    }

    @Provides
    @Singleton
    fun provideIncomeRepositoryImpl(@ApplicationContext context: Context): IncomeRepositoryImpl {
        return IncomeRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideExpenseRepositoryImpl(@ApplicationContext context: Context): ExpenseRepositoryImpl {
        return ExpenseRepositoryImpl(context)
    }
}