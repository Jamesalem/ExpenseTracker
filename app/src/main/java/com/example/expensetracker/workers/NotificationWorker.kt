package com.example.expensetracker.workers

import android.Manifest
import android.annotation.SuppressLint
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
import com.example.expensetracker.data.dao.SubscriptionDao
import com.example.expensetracker.data.model.BudgetPeriod
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val expenseRepo: ExpenseRepository,
    private val settingsRepo: SettingsRepository,
    private val subscriptionDao: SubscriptionDao
) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        return try {
            val settings = settingsRepo.appSettings.firstOrNull() ?: return Result.success()

            if (!settings.enableNotifications) return Result.success()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return Result.failure()
                }
            }

            val today = LocalDate.now()
            val (startDate, endDate) = calculateDateRange(settings.budgetPeriod, today)

            val totalSpent = expenseRepo.getTotalSpentBetween(startDate, endDate)
            
            val notificationMessage = createNotificationMessage(
                totalSpent = totalSpent,
                budgetAmount = settings.budgetAmount,
                currency = settings.defaultCurrency
            )

            val dueSubs = subscriptionDao.getSubscriptionsDueOn(today.toString())

            val finalMessage = if (dueSubs.isNotEmpty()) {
                val names = dueSubs.joinToString { it.title }
                "$notificationMessage | Bills Due Today: $names"
            } else {
                notificationMessage
            }

            showNotification(finalMessage)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in NotificationWorker")
            Result.retry()
        }
    }

    private fun calculateDateRange(period: BudgetPeriod, today: LocalDate): kotlin.Pair<LocalDate, LocalDate> {
        return when (period) {
            BudgetPeriod.DAILY -> kotlin.Pair(today, today)
            BudgetPeriod.WEEKLY -> {
                val firstDayOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                kotlin.Pair(firstDayOfWeek, today)
            }
            BudgetPeriod.MONTHLY -> {
                val firstDayOfMonth = today.withDayOfMonth(1)
                kotlin.Pair(firstDayOfMonth, today)
            }
            BudgetPeriod.YEARLY -> {
                val firstDayOfYear = today.withDayOfYear(1)
                kotlin.Pair(firstDayOfYear, today)
            }
        }
    }

    private fun createNotificationMessage(
        totalSpent: Double,
        budgetAmount: Double,
        currency: String
    ): String {
        val budgetLeft = budgetAmount - totalSpent

        return if (budgetAmount <= 0) {
            "Total spent so far: ${String.format(java.util.Locale.getDefault(), "%.2f", totalSpent)} $currency"
        } else if (budgetLeft >= 0) {
            "Budget status: ${String.format(java.util.Locale.getDefault(), "%.2f", budgetLeft)} $currency remaining"
        } else {
            "Budget Alert: Over budget by ${String.format(java.util.Locale.getDefault(), "%.2f", -budgetLeft)} $currency!"
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(message: String) {
        val channelId = "budget_alerts"
        val notificationId = (System.currentTimeMillis() % 10000).toInt()

        createNotificationChannel(channelId)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Expense Tracker Summary")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            channelId,
            "Expense & Budget Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily notifications for budget progress and recurring bill reminders"
            enableVibration(true)
        }

        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
