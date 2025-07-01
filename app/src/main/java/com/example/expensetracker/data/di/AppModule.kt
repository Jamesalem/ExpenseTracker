package com.example.expensetracker.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 1) Define the DataStore<Preferences> extension on Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.getInstance(ctx)

    @Provides @Singleton
    fun provideExpenseDao(db: AppDatabase): ExpenseDao =
        db.expenseDao()

    @Provides @Singleton
    fun provideExpenseRepository(dao: ExpenseDao): ExpenseRepository =
        ExpenseRepository(dao)

    // 2) Provide the DataStore<Preferences> so Hilt knows how to inject it
    @Provides @Singleton
    fun providePreferencesDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        ctx.dataStore

    // 3) Now provide SettingsRepository using that DataStore
    @Provides @Singleton
    fun provideSettingsRepo(dataStore: DataStore<Preferences>): SettingsRepository =
        SettingsRepository(dataStore)
}
