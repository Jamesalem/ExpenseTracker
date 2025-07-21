package com.example.expensetracker

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.local.InitialDataPopulator
import com.example.expensetracker.data.util.runDataStoreMigrations
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var initialDataPopulator: InitialDataPopulator
    @Inject lateinit var settingsDao: SettingsDao
    @Inject lateinit var dataStore: DataStore<Preferences> // Ensure DataStore is injected

    // Application-level coroutine scope (cancels on process death)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeData()
    }

    private fun initializeData() {
        applicationScope.launch {
            runCatching {
                // ✅ Run DataStore migrations that require injected dependencies
                // This function already handles migrating DEFAULT_CURRENCY and setting THEME_MODE
                dataStore.runDataStoreMigrations(settingsDao)

                // ✅ Populate initial data (e.g., default categories, budgets)
                // This will only run if tables are empty.
                initialDataPopulator.populateInitialData()
            }.onFailure { e ->
                // NEW: Replace with proper logging in production (e.g., Crashlytics)
                // Log.e("App", "Error during app initialization", e)
                e.printStackTrace() // Keep for now, but replace for production
            }
        }
    }

    // REMOVED: migrateCurrencyFromPreferences() function is no longer needed
    // as its logic is now covered by dataStore.runDataStoreMigrations(settingsDao)
}