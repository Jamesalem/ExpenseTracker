package com.example.expensetracker.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.expensetracker.data.dao.BudgetDao
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

// 1) Your DataStore<Preferences> extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 2) Provide the Room database (with both Expense & Budget tables)
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(
            ctx,
            AppDatabase::class.java,
            "expense_tracker_db"
        )
            // use the new overload; `true` means drop all tables on migration
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    // 3) Expose both DAOs
    @Provides
    @Singleton
    fun provideExpenseDao(db: AppDatabase): ExpenseDao =
        db.expenseDao()

    @Provides
    @Singleton
    fun provideBudgetDao(db: AppDatabase): BudgetDao =
        db.budgetDao()

    // 4) Wire up the repository with both DAOs
    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao,
        budgetDao: BudgetDao
    ): ExpenseRepository =
        ExpenseRepository(expenseDao, budgetDao)

    // 5) Your existing DataStore and SettingsRepo providers
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext ctx: Context
    ): DataStore<Preferences> =
        ctx.dataStore

    @Provides
    @Singleton
    fun provideSettingsRepo(
        dataStore: DataStore<Preferences>
    ): SettingsRepository =
        SettingsRepository(dataStore)
}
