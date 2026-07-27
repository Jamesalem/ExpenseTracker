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
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var initialDataPopulator: InitialDataPopulator
    @Inject lateinit var settingsDao: SettingsDao
    @Inject lateinit var dataStore: DataStore<Preferences>

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for structured logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        initializeData()
    }

    private fun initializeData() {
        applicationScope.launch {
            runCatching {
                dataStore.runDataStoreMigrations(settingsDao)
                initialDataPopulator.populateInitialData()
            }.onFailure { e ->
                Timber.e(e, "Application initialization failed")
            }
        }
    }
}
