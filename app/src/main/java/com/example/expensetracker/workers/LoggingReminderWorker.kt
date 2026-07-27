package com.example.expensetracker.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetracker.MainActivity
import com.example.expensetracker.R
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.LocalDate

@HiltWorker
class LoggingReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val expenseRepo: ExpenseRepository,
    private val settingsRepo: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = settingsRepo.appSettings.firstOrNull() ?: return Result.success()
            if (!settings.loggingReminderEnabled) return Result.success()

            val today = LocalDate.now()
            val expensesToday = expenseRepo.getBetweenDates(today, today)

            if (expensesToday.isEmpty()) {
                showReminderNotification()
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in LoggingReminderWorker")
            Result.retry()
        }
    }

    private fun showReminderNotification() {
        val channelId = "daily_reminders"
        val notificationId = 1003

        createReminderChannel(channelId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Forget something?")
            .setContentText("You haven't logged any expenses today. Keep your records up to date!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun createReminderChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminders"
            val descriptionText = "Nudges to log your daily expenses"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
