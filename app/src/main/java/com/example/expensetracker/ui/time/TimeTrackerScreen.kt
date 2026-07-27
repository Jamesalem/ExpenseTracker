package com.example.expensetracker.ui.time

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.TimeEntry
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import com.example.expensetracker.data.viewmodel.TimeViewModel
import com.example.expensetracker.ui.navigation.Routes
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrackerScreen(
    navController: NavController,
    timeViewModel: TimeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val runningEntry by timeViewModel.runningEntry.collectAsState()
    val elapsedSeconds by timeViewModel.elapsedSeconds.collectAsState()
    val timeEntries by timeViewModel.timeEntries.collectAsState()
    val settings by settingsViewModel.appSettings.collectAsState(initial = AppSettings())
    val haptic = LocalHapticFeedback.current

    var isPomodoroMode by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var isBillable by remember { mutableStateOf(false) }
    var hourlyRateText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Time & Focus", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.TIME_LOGS) }) {
                        Icon(Icons.Default.History, contentDescription = "Logs")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = !isPomodoroMode,
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isPomodoroMode = false 
                        },
                        label = { Text("Stopwatch") },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Spacer(Modifier.width(Dimens.medium))
                    FilterChip(
                        selected = isPomodoroMode,
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isPomodoroMode = true 
                        },
                        label = { Text("Pomodoro (${settings.pomodoroDurationMinutes}m)") },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    TimerStopwatchCard(
                        runningEntry = runningEntry,
                        elapsedSeconds = elapsedSeconds,
                        isPomodoroMode = isPomodoroMode,
                        taskTitle = taskTitle,
                        onTitleChange = { taskTitle = it },
                        isBillable = isBillable,
                        onBillableChange = { isBillable = it },
                        hourlyRateText = hourlyRateText,
                        onHourlyRateChange = { hourlyRateText = it },
                        pomodoroDurationMinutes = settings.pomodoroDurationMinutes,
                        onStartTimer = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val rate = hourlyRateText.toDoubleOrNull()
                            val titleFinal = if (isPomodoroMode) taskTitle.ifBlank { "Focus Session" } else taskTitle
                            timeViewModel.startTimer(
                                title = titleFinal,
                                category = "Work",
                                isBillable = isBillable,
                                hourlyRate = rate,
                                isPomodoro = isPomodoroMode
                            )
                            taskTitle = ""
                        },
                        onStopTimer = { id ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            timeViewModel.stopTimer(id)
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { navController.navigate(Routes.TIME_LOGS) }) {
                        Text("View Logs")
                    }
                }
            }

            if (timeEntries.isEmpty()) {
                item {
                    EmptyTimeView()
                }
            } else {
                items(timeEntries.take(5), key = { it.id }) { entry ->
                    TimeEntryRowItem(
                        entry = entry,
                        settings = settings,
                        onDelete = { timeViewModel.deleteEntry(entry.id) },
                        onConvertToExpense = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            timeViewModel.convertToExpense(entry, settings.defaultCurrency) 
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun EmptyTimeView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Timer, 
            contentDescription = null, 
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text("No time tracked yet", style = MaterialTheme.typography.titleMedium)
        Text("Track your focus and work hours here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TimerStopwatchCard(
    runningEntry: TimeEntry?,
    elapsedSeconds: Long,
    isPomodoroMode: Boolean,
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    isBillable: Boolean,
    onBillableChange: (Boolean) -> Unit,
    hourlyRateText: String,
    onHourlyRateChange: (String) -> Unit,
    pomodoroDurationMinutes: Int,
    onStartTimer: () -> Unit,
    onStopTimer: (Long) -> Unit
) {
    val isTimerActive = runningEntry != null
    val pomodoroTargetSeconds = pomodoroDurationMinutes * 60L
    val displaySeconds = if (isPomodoroMode && isTimerActive) {
        (pomodoroTargetSeconds - elapsedSeconds).coerceAtLeast(0L)
    } else {
        elapsedSeconds
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isTimerActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isTimerActive) (if (isPomodoroMode) "FOCUS IN PROGRESS" else "SESSION ACTIVE") else "IDLE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(Dimens.medium))

            AnimatedContent(
                targetState = displaySeconds,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = ""
            ) { seconds ->
                Text(
                    text = formatDuration(seconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 52.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(Dimens.medium))

            if (!isTimerActive) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Task Description") },
                    placeholder = { Text("e.g., Coding, Meeting") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = Shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Billable Session", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isBillable, onCheckedChange = onBillableChange)
                }

                AnimatedVisibility(visible = isBillable) {
                    OutlinedTextField(
                        value = hourlyRateText,
                        onValueChange = onHourlyRateChange,
                        label = { Text("Hourly Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        shape = Shapes.medium
                    )
                }

                Button(
                    onClick = onStartTimer,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = Shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("START SESSION", fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Text(
                    text = "Working on: ${runningEntry.title}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(Dimens.large))
                Button(
                    onClick = { onStopTimer(runningEntry.id) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = Shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Stop, null)
                    Spacer(Modifier.width(8.dp))
                    Text("STOP & SAVE", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun TimeEntryRowItem(
    entry: TimeEntry,
    settings: AppSettings,
    onDelete: () -> Unit,
    onConvertToExpense: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                "${formatDuration(entry.durationSeconds)} • ${entry.dateString}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (entry.associatedExpenseId == null && entry.isBillable && entry.hourlyRate != null) {
            TextButton(onClick = onConvertToExpense) {
                Text("+ Income", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else if (entry.associatedExpenseId != null) {
            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp).padding(end = 8.dp))
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
    }
}

fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}
