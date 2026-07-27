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
import com.example.expensetracker.data.dao.TimeEntryDao
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.Migration1to2
import com.example.expensetracker.data.local.Migration2to3
import com.example.expensetracker.data.local.Migration3to4
import com.example.expensetracker.data.local.Migration4to5
import com.example.expensetracker.data.local.Migration5to6
import com.example.expensetracker.data.local.Migration6to7
import com.example.expensetracker.data.local.Migration7to8
import com.example.expensetracker.data.local.Migration8to9
import com.example.expensetracker.data.local.Migration9to10
import com.example.expensetracker.data.local.Migration10to11
import com.example.expensetracker.data.local.Migration11to12
import com.example.expensetracker.data.local.Migration12to13
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.BudgetRepositoryImpl
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.CategoryRepositoryImpl
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.ExpenseRepositoryImpl
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.data.repository.SettingsRepositoryImpl
import com.example.expensetracker.data.repository.TimeRepository
import com.example.expensetracker.data.repository.TimeRepositoryImpl
import com.example.expensetracker.data.util.settingsDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

import com.example.expensetracker.data.dao.SubscriptionDao

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
                Migration6to7, Migration7to8, Migration8to9, Migration9to10, 
                Migration10to11, Migration11to12, Migration12to13
            )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()
    @Provides fun provideTimeEntryDao(db: AppDatabase): TimeEntryDao = db.timeEntryDao()
    @Provides fun provideSubscriptionDao(db: AppDatabase): SubscriptionDao = db.subscriptionDao()
}

// 2️⃣ Repositories
@Module
@InstallIn(SingletonComponent::class)
@Suppress("Unused") // Suppress 'unused' warning for Hilt-bound interfaces
interface RepositoryModule {

    @Binds @Suppress("Unused") fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds @Suppress("Unused") fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository
    @Binds @Suppress("Unused") fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
    @Binds @Suppress("Unused") fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds @Suppress("Unused") fun bindTimeRepository(impl: TimeRepositoryImpl): TimeRepository
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
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    @Suppress("unused")
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
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val clazz = Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
        val factory = workerFactories[clazz]
            ?: workerFactories.entries.firstOrNull { clazz.isAssignableFrom(it.key) }?.value
        return factory?.createWorker(appContext, workerClassName, workerParameters)
    }
}
