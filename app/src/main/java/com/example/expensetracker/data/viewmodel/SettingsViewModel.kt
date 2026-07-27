package com.example.expensetracker.data.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
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
import com.example.expensetracker.workers.LoggingReminderWorker
import com.example.expensetracker.workers.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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

    sealed class SettingsUiState {
        data object Loading : SettingsUiState()
        data class Success(val settings: AppSettings) : SettingsUiState()
        data class Error(val message: String) : SettingsUiState()
    }
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _backupResult = MutableStateFlow<Pair<Boolean, Uri?>?>(null)
    val backupResult: StateFlow<Pair<Boolean, Uri?>?> = _backupResult.asStateFlow()

    private val _restoreResult = MutableStateFlow<Pair<Boolean, String?>?>(null)
    val restoreResult: StateFlow<Pair<Boolean, String?>?> = _restoreResult.asStateFlow()

    val appSettings = settingsRepo.appSettings

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepo.appSettings
                .catch { e ->
                    _uiState.value = SettingsUiState.Error("Error loading settings: ${e.message}")
                }
                .collect { settings ->
                    _uiState.value = SettingsUiState.Success(settings)
                }
        }
    }

    private suspend fun updateSettings(newSettings: AppSettings) {
        try {
            settingsRepo.updateSettings(newSettings)
        } catch (e: Exception) {
            _userMessage.emit("Failed to update settings: ${e.message}")
        }
    }

    fun completeOnboarding(selectedCurrency: String) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(defaultCurrency = selectedCurrency, hasCompletedOnboarding = true))
    }

    fun setCurrency(currency: String) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(defaultCurrency = currency))
    }

    fun convertHistoricalCurrency(newCurrency: String, exchangeRate: Double) = viewModelScope.launch {
        val current = appSettings.first()
        val updatedBudget = current.budgetAmount * exchangeRate
        updateSettings(current.copy(defaultCurrency = newCurrency, budgetAmount = updatedBudget))
        expenseRepo.convertCurrencyAmounts(newCurrency, exchangeRate)
        _userMessage.emit("Currency converted to $newCurrency")
    }

    fun setDecimalPlaces(decimalPlaces: Int) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(decimalPlaces = decimalPlaces))
    }

    fun setGroupingSeparator(useGrouping: Boolean) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(useGroupingSeparator = useGrouping))
    }

    fun setBudget(amount: Double, period: BudgetPeriod) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(budgetAmount = amount, budgetPeriod = period))
    }

    fun setTheme(theme: ThemeMode) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(themeMode = theme))
    }

    fun setAutoBackup(enabled: Boolean, frequency: BackupFrequency) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(autoBackupEnabled = enabled, autoBackupFrequency = frequency))
    }

    fun setSecurity(useLock: Boolean, pin: String? = null, useBiometrics: Boolean = false) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(useAppLock = useLock, appLockPin = pin, useBiometrics = useBiometrics))
    }

    fun updateCustomCategories(categories: List<String>) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(customCategories = categories))
    }

    fun setNotifications(
        enabled: Boolean,
        time: String? = null,
        weeklyDay: DayOfWeek? = null
    ) = viewModelScope.launch {
        val base = appSettings.first()
        updateSettings(
            base.copy(
                enableNotifications = enabled,
                notificationTime = time ?: base.notificationTime,
                weeklyReminderDay = weeklyDay ?: base.weeklyReminderDay
            )
        )
        scheduleNotifications(enabled, time ?: base.notificationTime, weeklyDay)
    }

    fun setLoggingReminder(enabled: Boolean, time: String? = null) = viewModelScope.launch {
        val base = appSettings.first()
        updateSettings(
            base.copy(
                loggingReminderEnabled = enabled,
                loggingReminderTime = time ?: base.loggingReminderTime
            )
        )
        scheduleLoggingReminder(enabled, time ?: base.loggingReminderTime)
    }

    fun setTimerSound(uri: String?) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(timerSoundUri = uri))
        _userMessage.emit("Timer sound updated")
    }

    fun setPomodoroDuration(minutes: Int) = viewModelScope.launch {
        val current = appSettings.first()
        updateSettings(current.copy(pomodoroDurationMinutes = minutes))
        _userMessage.emit("Pomodoro duration updated to $minutes mins")
    }

    fun exportBackupToUri(targetUri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val expenses = expenseRepo.getBetweenDates(LocalDate.MIN, LocalDate.MAX)
                    val json = jsonFormat.encodeToString(expenses)
                    context.contentResolver.openOutputStream(targetUri)?.use { it.write(json.toByteArray()) }
                }
                _userMessage.emit("Backup saved successfully!")
            } catch (e: Exception) {
                _userMessage.emit("Failed to save backup: ${e.message}")
            }
        }
    }

    fun performBackup() {
        viewModelScope.launch {
            try {
                val uri = withContext(Dispatchers.IO) {
                    val expenses = expenseRepo.getBetweenDates(LocalDate.MIN, LocalDate.MAX)
                    val json = jsonFormat.encodeToString(expenses)
                    val file = File(context.filesDir, "expense_backup_${System.currentTimeMillis()}.json")
                    file.writeText(json)
                    FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                }
                _backupResult.value = true to uri
                _userMessage.emit("Backup created successfully")
            } catch (e: Exception) {
                _backupResult.value = false to null
                _userMessage.emit("Failed to create backup: ${e.message}")
            }
        }
    }

    fun performRestore(uri: Uri) {
        viewModelScope.launch {
            try {
                val message = withContext(Dispatchers.IO) {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                    val expenses = jsonFormat.decodeFromString<List<Expense>>(json)
                    expenseRepo.replaceAllExpenses(expenses)
                    context.getString(R.string.restore_success)
                }
                _restoreResult.value = true to message
                _userMessage.emit("Data restored successfully")
            } catch (e: Exception) {
                _restoreResult.value = false to (e.localizedMessage ?: "Unknown error")
                _userMessage.emit("Failed to restore data: ${e.message}")
            }
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            try {
                val testWorkRequest = androidx.work.OneTimeWorkRequestBuilder<NotificationWorker>().build()
                workManager.enqueue(testWorkRequest)
                _userMessage.emit("Test notification triggered!")
            } catch (e: Exception) {
                _userMessage.emit("Failed to trigger notification: ${e.message}")
            }
        }
    }

    fun clearBackupResult() { _backupResult.value = null }
    fun clearRestoreResult() { _restoreResult.value = null }

    private suspend fun scheduleNotifications(
        enabled: Boolean,
        time: String,
        weeklyDay: DayOfWeek?
    ) {
        workManager.cancelUniqueWork("budget_notification")
        if (!enabled) return

        try {
            val parts = time.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: return

            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val todayTarget = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

            val firstRun = if (weeklyDay != null) {
                now.with(TemporalAdjusters.nextOrSame(weeklyDay)).withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            } else {
                if (todayTarget.isAfter(now)) todayTarget else todayTarget.plusDays(1)
            }

            val initialDelay = ChronoUnit.SECONDS.between(now, firstRun)
            val intervalDays = if (weeklyDay != null) 7L else 1L

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(intervalDays, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "budget_notification",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } catch (e: Exception) {
            _userMessage.emit("Failed to schedule notifications: ${e.message}")
        }
    }

    private suspend fun scheduleLoggingReminder(enabled: Boolean, time: String) {
        workManager.cancelUniqueWork("logging_reminder")
        if (!enabled) return

        try {
            val parts = time.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: return

            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            val firstRun = if (target.isAfter(now)) target else target.plusDays(1)
            val initialDelay = ChronoUnit.SECONDS.between(now, firstRun)

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<LoggingReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "logging_reminder",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } catch (e: Exception) {
            _userMessage.emit("Failed to schedule logging reminder: ${e.message}")
        }
    }
}
