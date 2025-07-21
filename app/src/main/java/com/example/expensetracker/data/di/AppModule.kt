// AppModule.kt
package com.example.expensetracker.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.expensetracker.data.dao.BudgetDao
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.Migration1to2
import com.example.expensetracker.data.local.Migration2to3
import com.example.expensetracker.data.local.Migration3to4
import com.example.expensetracker.data.local.Migration4to5
import com.example.expensetracker.data.local.Migration5to6
import com.example.expensetracker.data.local.Migration6to7 // NEW: Import the Migration6to7
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.BudgetRepositoryImpl
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.CategoryRepositoryImpl
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.ExpenseRepositoryImpl
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.data.repository.SettingsRepositoryImpl
import com.example.expensetracker.data.util.settingsDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

// 1️⃣ Database + DAOs
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "expense_tracker_db"
        )
            .addMigrations(
                Migration1to2, Migration2to3, Migration3to4, Migration4to5, Migration5to6,
                Migration6to7 // NEW: Add Migration6to7
            )
            // *** IMPORTANT CHANGE: Addressed deprecation by using explicit parameter ***
            // This will recreate the database if schema changes and no explicit migration is found.
            // Useful for development, but wipes user data.
            .fallbackToDestructiveMigration(true) // Updated to specify 'true'
            // Removed .fallbackToDestructiveMigrationOnDowngrade(true) as fallbackToDestructiveMigration()
            // covers both upgrades and downgrades when a migration path is missing.
            .build()
    }

    @Provides fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()
}

// 2️⃣ Repositories
@Module
@InstallIn(SingletonComponent::class)
@Suppress("Unused") // Suppress 'unused' warning for Hilt-bound interfaces
interface RepositoryModule {

    // @Binds tells Hilt: when someone asks for CategoryRepository, give CategoryRepositoryImpl
    @Binds @Suppress("Unused") fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds @Suppress("Unused") fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository
    @Binds @Suppress("Unused") fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
    @Binds @Suppress("Unused") fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

// 3️⃣ DataStore & WorkManager
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

// 4️⃣ Custom WorkerFactory (if needed)
// ⚠ If you don’t use it, you can delete this whole module & factory.
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    // Keep @Multi-binds only if you have custom workers registered elsewhere
    @Suppress("unused") // keep to avoid warning if not yet used
    @dagger.multibindings.Multibinds
    abstract fun bindWorkerFactories(): Map<Class<out ListenableWorker>, WorkerFactory>

    companion object {
        @Provides
        @Singleton
        fun provideHiltWorkerFactory(
            workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards WorkerFactory>
        ): WorkerFactory = HiltWorkerFactory(workerFactories)
    }
}

class HiltWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards WorkerFactory>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters // ✅ match parent parameter name
    ): ListenableWorker? {
        val clazz = Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
        val factory = workerFactories[clazz]
            ?: workerFactories.entries.firstOrNull { clazz.isAssignableFrom(it.key) }?.value
        return factory?.createWorker(appContext, workerClassName, workerParameters)
    }
}