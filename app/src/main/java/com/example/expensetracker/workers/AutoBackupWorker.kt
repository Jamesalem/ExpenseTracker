package com.example.expensetracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.time.LocalDate

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val expenseRepo: ExpenseRepository,
    private val settingsRepo: SettingsRepository
) : CoroutineWorker(context, params) {

    private val jsonFormat = Json { prettyPrint = true }

    override suspend fun doWork(): Result {
        return try {
            val settings = settingsRepo.appSettings.firstOrNull() ?: return Result.success()
            if (!settings.autoBackupEnabled) return Result.success()

            withContext(Dispatchers.IO) {
                val expenses = expenseRepo.getBetweenDates(LocalDate.MIN, LocalDate.MAX)
                val json = jsonFormat.encodeToString(expenses)

                val backupDir = File(context.filesDir, "auto_backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                // Maintain a rolling window of recent backups (keep latest 5)
                val backupFile = File(backupDir, "expense_backup_${System.currentTimeMillis()}.json")
                backupFile.writeText(json)

                // Clean old backups keeping only the 5 most recent
                val existingBackups = backupDir.listFiles()?.sortedByDescending { it.lastModified() }
                if (existingBackups != null && existingBackups.size > 5) {
                    existingBackups.drop(5).forEach { it.delete() }
                }

                // Update settings with last backup timestamp
                val now = System.currentTimeMillis()
                settingsRepo.updateSettings(settings.copy(lastBackupTimestamp = now))
            }

            Timber.d("Auto-backup successfully completed")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "AutoBackupWorker failed")
            Result.retry()
        }
    }
}
