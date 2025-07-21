package com.example.expensetracker

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.expensetracker.data.dao.SettingsDao
import com.example.expensetracker.data.local.InitialDataPopulator
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.util.PreferenceKeys
import com.example.expensetracker.data.util.runDataStoreMigrations
import com.example.expensetracker.data.util.settingsDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var initialDataPopulator: InitialDataPopulator
    @Inject lateinit var settingsDao: SettingsDao

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
                applicationContext.settingsDataStore.runDataStoreMigrations(settingsDao)

                // ✅ Migrate default currency from preferences to database
                migrateCurrencyFromPreferences()

                // ✅ Populate initial data (e.g., default categories, budgets)
                initialDataPopulator.populateInitialData()
            }.onFailure { e ->
                // Replace with proper logging in production (e.g., Crashlytics)
                e.printStackTrace()
            }
        }
    }

    private suspend fun migrateCurrencyFromPreferences() {
        val dataStore: DataStore<Preferences> = applicationContext.settingsDataStore
        val prefs = dataStore.data.first()

        prefs[PreferenceKeys.DEFAULT_CURRENCY]?.let { currencyCode ->
            val settings = settingsDao.getSettings() ?: AppSettings.getDefault()
            settingsDao.saveSettings(settings.copy(defaultCurrency = currencyCode))

            dataStore.edit { it.remove(PreferenceKeys.DEFAULT_CURRENCY) }
        }
    }
}
