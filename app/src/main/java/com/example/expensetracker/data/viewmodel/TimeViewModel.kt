package com.example.expensetracker.data.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.TimeEntry
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.data.repository.TimeRepository
import com.example.expensetracker.receivers.TimerReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TimeViewModel @Inject constructor(
    private val timeRepository: TimeRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val timeEntries: StateFlow<List<TimeEntry>> = timeRepository.observeAllTimeEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val runningEntry: StateFlow<TimeEntry?> = timeRepository.observeRunningEntry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    val todayDateString: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val todaySeconds: StateFlow<Long?> = timeRepository.observeTotalSecondsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private var tickerJob: Job? = null
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        viewModelScope.launch {
            runningEntry.collect { running ->
                tickerJob?.cancel()
                if (running != null) {
                    tickerJob = viewModelScope.launch {
                        while (isActive) {
                            val now = System.currentTimeMillis()
                            val diff = ((now - running.startTimeMillis) / 1000).coerceAtLeast(0)
                            _elapsedSeconds.value = diff
                            delay(1000)
                        }
                    }
                } else {
                    _elapsedSeconds.value = 0L
                    cancelAlarm()
                }
            }
        }
    }

    fun startTimer(
        title: String,
        category: String,
        isBillable: Boolean,
        hourlyRate: Double?,
        isPomodoro: Boolean = false
    ) {
        viewModelScope.launch {
            timeRepository.startTimer(title, category, isBillable, hourlyRate)
            if (isPomodoro) {
                val settings = settingsRepository.appSettings.first()
                scheduleAlarm(title, settings.pomodoroDurationMinutes)
            }
        }
    }

    private fun scheduleAlarm(taskTitle: String, durationMinutes: Int) {
        val intent = Intent(context, TimerReceiver::class.java).apply {
            putExtra("task_title", taskTitle)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelAlarm() {
        val intent = Intent(context, TimerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun stopTimer(id: Long) {
        viewModelScope.launch {
            timeRepository.stopTimer(id)
            cancelAlarm()
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            timeRepository.deleteTimeEntry(id)
        }
    }

    fun convertToExpense(entry: TimeEntry, defaultCurrency: String) {
        viewModelScope.launch {
            timeRepository.convertToExpense(entry, defaultCurrency)
        }
    }
}
