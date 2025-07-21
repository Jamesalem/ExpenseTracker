package com.example.expensetracker.data.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.BackupFrequency
import com.example.expensetracker.data.model.BudgetPeriod
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.ThemeMode
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.workers.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val expenseRepo: ExpenseRepository,
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) : ViewModel() {

    private companion object {
        val jsonFormat = Json { prettyPrint = true }
    }

    // Internal UI state, if you need it elsewhere
    sealed class SettingsUiState {
        object Loading : SettingsUiState()
        data class Success(val settings: AppSettings) : SettingsUiState()
        data class Error(val message: String) : SettingsUiState()
    }
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage

    private val _backupResult = MutableStateFlow<Pair<Boolean, Uri?>?>(null)
    val backupResult: StateFlow<Pair<Boolean, Uri?>?> = _backupResult.asStateFlow()

    private val _restoreResult = MutableStateFlow<Pair<Boolean, String?>?>(null)
    val restoreResult: StateFlow<Pair<Boolean, String?>?> = _restoreResult.asStateFlow()

    /** Expose the raw AppSettings flow directly for screens to collect */
    val appSettings = settingsRepo.appSettings

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepo.appSettings
                .catch { e ->
                    _uiState.value = SettingsUiState.Error(
                        "Error loading settings: ${e.message ?: "Unknown error"}"
                    )
                }
                .collect { settings ->
                    _uiState.value = SettingsUiState.Success(settings)
                }
        }
    }

    private fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            try {
                settingsRepo.updateSettings(newSettings)
                _userMessage.emit("Settings updated successfully")
            } catch (e: Exception) {
                _userMessage.emit("Failed to update settings: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun setCurrency(currency: String) =
        updateSettings(currentSettings().copy(defaultCurrency = currency))

    fun setBudget(amount: Double, period: BudgetPeriod) =
        updateSettings(currentSettings().copy(budgetAmount = amount, budgetPeriod = period))

    fun setTheme(theme: ThemeMode) =
        updateSettings(currentSettings().copy(themeMode = theme))

    fun setAutoBackup(enabled: Boolean, frequency: BackupFrequency) =
        updateSettings(
            currentSettings().copy(
                autoBackupEnabled = enabled,
                autoBackupFrequency = frequency
            )
        )

    fun setSecurity(useLock: Boolean, pin: String? = null, useBiometrics: Boolean = false) =
        updateSettings(
            currentSettings().copy(
                useAppLock = useLock,
                appLockPin = pin,
                useBiometrics = useBiometrics
            )
        )

    fun updateCustomCategories(categories: List<String>) =
        updateSettings(currentSettings().copy(customCategories = categories))

    fun setNotifications(
        enabled: Boolean,
        time: String? = null,
        weeklyDay: DayOfWeek? = null
    ) {
        val base = currentSettings()
        updateSettings(
            base.copy(
                enableNotifications = enabled,
                notificationTime     = time ?: base.notificationTime,
                weeklyReminderDay    = weeklyDay ?: base.weeklyReminderDay
            )
        )
        scheduleNotifications(enabled, time ?: base.notificationTime, weeklyDay)
    }

    fun performBackup() {
        viewModelScope.launch {
            try {
                val uri = withContext(Dispatchers.IO) {
                    val expenses = expenseRepo.getBetweenDates(LocalDate.MIN, LocalDate.MAX)
                    val json     = jsonFormat.encodeToString(expenses)
                    val file     = File(context.filesDir, "expense_backup_${System.currentTimeMillis()}.json")
                    file.writeText(json)
                    FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                }
                _backupResult.value = true to uri
                _userMessage.emit("Backup created successfully")
            } catch (e: Exception) {
                _backupResult.value = false to null
                _userMessage.emit("Failed to create backup: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun performRestore(uri: Uri) {
        viewModelScope.launch {
            try {
                val message = withContext(Dispatchers.IO) {
                    val json     = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        .orEmpty()
                    val expenses = jsonFormat.decodeFromString<List<Expense>>(json)
                    expenseRepo.replaceAllExpenses(expenses)
                    context.getString(R.string.restore_success)
                }
                _restoreResult.value = true to message
                _userMessage.emit("Data restored successfully")
            } catch (e: Exception) {
                _restoreResult.value = false to (e.localizedMessage ?: "Unknown error")
                _userMessage.emit("Failed to restore data: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun clearBackupResult() { _backupResult.value = null }
    fun clearRestoreResult() { _restoreResult.value = null }

    private fun scheduleNotifications(
        enabled: Boolean,
        time: String,
        weeklyDay: DayOfWeek?
    ) {
        workManager.cancelUniqueWork("budget_notification")
        if (!enabled) return

        val parts  = time.split(":")
        val hour   = parts.getOrNull(0)?.toIntOrNull() ?: return
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return

        val now         = ZonedDateTime.now(ZoneId.systemDefault())
        val todayTarget = now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)

        val firstRun = if (weeklyDay != null) {
            now.with(TemporalAdjusters.nextOrSame(weeklyDay))
                .withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        } else {
            if (todayTarget.isAfter(now)) todayTarget else todayTarget.plusDays(1)
        }

        val initialDelay = ChronoUnit.SECONDS.between(now, firstRun)
        val intervalDays  = if (weeklyDay != null) 7L else 1L

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            intervalDays, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "budget_notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun currentSettings(): AppSettings =
        (_uiState.value as? SettingsUiState.Success)?.settings
            ?: AppSettings.getDefault()
}
