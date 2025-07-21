package com.example.expensetracker.workers

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager // NEW: Import PackageManager
import android.os.Build
import android.util.Pair
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat // NEW: Import ContextCompat
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetracker.MainActivity
import com.example.expensetracker.R
import com.example.expensetracker.data.model.BudgetPeriod
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Suppress("SameParameterValue")
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val expenseRepo: ExpenseRepository,
    private val settingsRepo: SettingsRepository
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            val settings = settingsRepo.appSettings.firstOrNull() ?: return Result.success()

            if (!settings.enableNotifications) return Result.success()

            // NEW: Runtime permission check for notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // Permission not granted, cannot show notification. Fail gracefully.
                    // Log this situation using your preferred logging solution.
                    // Log.w("NotificationWorker", "Notification permission not granted. Skipping notification display.")
                    return Result.failure()
                }
            }

            val today = LocalDate.now()
            val (startDate, endDate) = calculateDateRange(settings.budgetPeriod, today)

            val expenses = expenseRepo.getBetweenDates(start = startDate, end = endDate)
            val notificationMessage = createNotificationMessage(
                expenses = expenses,
                budgetAmount = settings.budgetAmount
            )

            showNotification(notificationMessage)
            Result.success()
        } catch (e: Exception) {
            // NEW: Replace with proper error logging in production (e.g., Crashlytics)
            // Log.e("NotificationWorker", "Error showing notification", e)
            e.printStackTrace() // Keep for now, but replace for production
            Result.retry()
        }
    }

    private fun calculateDateRange(period: BudgetPeriod, today: LocalDate): Pair<LocalDate, LocalDate> {
        return when (period) {
            BudgetPeriod.DAILY -> Pair(today, today)
            BudgetPeriod.WEEKLY -> {
                val firstDayOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                Pair(firstDayOfWeek, today)
            }
            BudgetPeriod.MONTHLY -> {
                val firstDayOfMonth = today.withDayOfMonth(1)
                Pair(firstDayOfMonth, today)
            }
            BudgetPeriod.YEARLY -> {
                val firstDayOfYear = today.withDayOfYear(1)
                Pair(firstDayOfYear, today)
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun createNotificationMessage(
        expenses: List<Expense>,
        budgetAmount: Double
    ): String {
        val totalSpent = expenses
            .filter { it.type == Expense.ExpenseType.EXPENSE }
            .sumOf { it.amount }

        val budgetLeft = budgetAmount - totalSpent

        return if (budgetLeft >= 0) {
            context.getString(R.string.notification_budget_under, budgetLeft)
        } else {
            context.getString(R.string.notification_budget_over, -budgetLeft)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(message: String) {
        val channelId = "budget_alerts"
        val notificationId = System.currentTimeMillis().toInt()

        createNotificationChannel(channelId)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.budget_update))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.budget_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.budget_notification_description)
        }

        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "budget")
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}