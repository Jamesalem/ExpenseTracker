package com.example.expensetracker.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetracker.MainActivity
import com.example.expensetracker.R
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

@HiltWorker
class TimerWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepo: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = settingsRepo.appSettings.firstOrNull() ?: return Result.success()
            val taskTitle = inputData.getString("task_title") ?: "Focus Session"
            
            showTimerNotification(taskTitle, settings.timerSoundUri)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in TimerWorker")
            Result.failure()
        }
    }

    private fun showTimerNotification(taskTitle: String, soundUriString: String?) {
        val baseChannelId = "timer_alerts"
        val channelId = if (soundUriString != null) {
            "${baseChannelId}_${soundUriString.hashCode()}"
        } else {
            baseChannelId
        }

        val notificationId = 1002
        val soundUri = soundUriString?.let { Uri.parse(it) }

        createTimerNotificationChannel(channelId, soundUri)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Focus Session Complete!")
            .setContentText("Great job! '$taskTitle' focus time is up.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .setVibrate(longArrayOf(0, 500, 200, 500))

        if (soundUri != null) {
            builder.setSound(soundUri)
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    private fun createTimerNotificationChannel(channelId: String, soundUri: Uri?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(channelId) != null) return

        val channelName = "Timer Alerts"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Notifications for Pomodoro and Task timers"
            enableVibration(true)
            if (soundUri != null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
        }
        notificationManager.createNotificationChannel(channel)
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
