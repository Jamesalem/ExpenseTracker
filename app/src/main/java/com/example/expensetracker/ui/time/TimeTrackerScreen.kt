package com.example.expensetracker.ui.time

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current

    var isPomodoroMode by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var isBillable by remember { mutableStateOf(false) }
    var hourlyRateText by remember { mutableStateOf("") }
    
    var entryToDelete by remember { mutableStateOf<TimeEntry?>(null) }

    // Permission Launcher for Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    fun startTimerWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
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
    }

    val yieldMetrics by timeViewModel.productivityYield.collectAsState(initial = null)

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
                        onStartTimer = { startTimerWithPermission() },
                        onStopTimer = { id ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            timeViewModel.stopTimer(id)
                        }
                    )
                }
            }

            if (yieldMetrics != null && yieldMetrics!!.totalTrackedHours > 0.0) {
                item {
                    ProductivityYieldCard(
                        yield = yieldMetrics!!,
                        currency = settings.defaultCurrency
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
                        onDelete = { entryToDelete = entry },
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

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Time Log") },
            text = { Text("Are you sure you want to permanently delete this focus session? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        timeViewModel.deleteEntry(entryToDelete!!.id)
                        entryToDelete = null
                    }
                ) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("CANCEL")
                }
            }
        )
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

@Composable
fun ProductivityYieldCard(
    yield: com.example.expensetracker.data.math.ProductivityYieldEngine.ProductivityYieldSummary,
    currency: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Productivity & Yield",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${String.format(Locale.getDefault(), "%.1f", yield.totalTrackedHours)} hrs logged",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Effective Hourly Yield (EHY)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Shapes.medium)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            "Effective Hourly Yield",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            CurrencyFormatter.format(yield.overallEffectiveHourlyRate, currency) + "/hr",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF10B981)
                        )
                    }
                }

                // Top Yielding Category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(Shapes.medium)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            "Top ROI Focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            yield.topYieldingCategory ?: "General",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
