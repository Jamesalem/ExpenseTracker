package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.expensetracker.data.model.TimeEntry
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.data.repository.TimeRepository
import com.example.expensetracker.workers.TimerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TimeViewModel @Inject constructor(
    private val timeRepository: TimeRepository,
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager
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
                    workManager.cancelUniqueWork("pomodoro_timer")
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
                val duration = settings.pomodoroDurationMinutes.toLong()
                
                val timerRequest = OneTimeWorkRequestBuilder<TimerWorker>()
                    .setInitialDelay(duration, TimeUnit.MINUTES)
                    .setInputData(workDataOf("task_title" to title))
                    .addTag("pomodoro_timer")
                    .build()

                workManager.enqueueUniqueWork(
                    "pomodoro_timer",
                    ExistingWorkPolicy.REPLACE,
                    timerRequest
                )
            }
        }
    }

    fun stopTimer(id: Long) {
        viewModelScope.launch {
            timeRepository.stopTimer(id)
            workManager.cancelUniqueWork("pomodoro_timer")
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
