// ui/setting/SettingsScreen.kt
package com.example.expensetracker.ui.setting

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.util.BiometricPromptManager
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import com.example.expensetracker.ui.setting.components.SettingsSectionHeader
import com.example.expensetracker.ui.setting.dialogs.BackupSettingDialog
import com.example.expensetracker.ui.setting.dialogs.BudgetSettingDialog
import com.example.expensetracker.ui.setting.dialogs.CategoryManagementDialog
import com.example.expensetracker.ui.setting.dialogs.CurrencyPickerDialog
import com.example.expensetracker.ui.setting.dialogs.DecimalPlacesDialog
import com.example.expensetracker.ui.setting.dialogs.LoggingReminderDialog
import com.example.expensetracker.ui.setting.dialogs.NotificationSettingsDialog
import com.example.expensetracker.ui.setting.dialogs.PinSetupDialog
import com.example.expensetracker.ui.setting.dialogs.PomodoroDurationDialog
import com.example.expensetracker.ui.setting.dialogs.SecuritySettingsDialog
import com.example.expensetracker.ui.setting.dialogs.ThemeSettingDialog
import com.example.expensetracker.ui.setting.sections.AboutSetting
import com.example.expensetracker.ui.setting.sections.AppearanceSetting
import com.example.expensetracker.ui.setting.sections.BudgetSetting
import com.example.expensetracker.ui.setting.sections.CategorySetting
import com.example.expensetracker.ui.setting.sections.CurrencySetting
import com.example.expensetracker.ui.setting.sections.DataManagementSetting
import com.example.expensetracker.ui.setting.sections.NotificationSetting
import com.example.expensetracker.ui.setting.sections.SecuritySetting
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val appSettings = (uiState as? SettingsViewModel.SettingsUiState.Success)?.settings ?: AppSettings()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricManager = remember(activity) { activity?.let { BiometricPromptManager(it) } }

    // Dialog states
    var showCurrencyPicker     by remember { mutableStateOf(false) }
    var showBudgetDialog       by remember { mutableStateOf(false) }
    var showCategoryDialog     by remember { mutableStateOf(false) }
    var showThemeDialog        by remember { mutableStateOf(false) }
    var showBackupDialog       by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLoggingReminderDialog by remember { mutableStateOf(false) }
    var showPomodoroDialog     by remember { mutableStateOf(false) }
    var showSecurityDialog     by remember { mutableStateOf(false) }
    var showPinSetupDialog     by remember { mutableStateOf(false) }
    var showDecimalPlacesDialog by remember { mutableStateOf(false) }

    val backupResult  by viewModel.backupResult.collectAsState()
    val restoreResult by viewModel.restoreResult.collectAsState()

    // Sound Picker Launcher
    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            viewModel.setTimerSound(uri?.toString())
        }
    }

    // SAF Document Launchers
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackupToUri(it) }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.performRestore(it) }
    }

    // Handle results
    LaunchedEffect(backupResult) {
        backupResult?.let { (success, uri) ->
            if (success && uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_backup)))
                Toast.makeText(context, R.string.backup_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.backup_failed, Toast.LENGTH_LONG).show()
            }
            viewModel.clearBackupResult()
        }
    }

    LaunchedEffect(restoreResult) {
        restoreResult?.let { (success, message) ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearRestoreResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is SettingsViewModel.SettingsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is SettingsViewModel.SettingsUiState.Error -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(text = (uiState as SettingsViewModel.SettingsUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is SettingsViewModel.SettingsUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { SettingsSectionHeader("General") }
                        item { CurrencySetting(appSettings, { showCurrencyPicker = true }, { showDecimalPlacesDialog = true }, { viewModel.setGroupingSeparator(it) }) }
                        item { SettingsSectionHeader("Budget") }
                        item { BudgetSetting(appSettings, { showBudgetDialog = true }) }
                        item { SettingsSectionHeader(stringResource(R.string.categories)) }
                        item { CategorySetting(appSettings, { showCategoryDialog = true }) }
                        item { SettingsSectionHeader(stringResource(R.string.appearance)) }
                        item { AppearanceSetting(appSettings, { showThemeDialog = true }) }
                        item { SettingsSectionHeader(stringResource(R.string.notifications)) }
                        item {
                            NotificationSetting(
                                appSettings = appSettings,
                                onNotificationToggle = { viewModel.setNotifications(enabled = it) },
                                onNotificationSettingsClick = { showNotificationsDialog = true },
                                onTestNotificationClick = { viewModel.sendTestNotification() },
                                onSoundPickerClick = {
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Timer Sound")
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, appSettings.timerSoundUri?.let { Uri.parse(it) })
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                    }
                                    soundPickerLauncher.launch(intent)
                                },
                                onLoggingReminderToggle = { viewModel.setLoggingReminder(enabled = it) },
                                onLoggingReminderSettingsClick = { showLoggingReminderDialog = true },
                                onPomodoroSettingsClick = { showPomodoroDialog = true }
                            )
                        }
                        item { SettingsSectionHeader(stringResource(R.string.security)) }
                        item {
                            SecuritySetting(
                                appSettings = appSettings,
                                onAppLockToggle = { useLock ->
                                    if (useLock && activity != null) {
                                        biometricManager?.showBiometricPrompt(
                                            title = "Confirm Identity",
                                            subtitle = "Authenticate to enable App Lock",
                                            onSuccess = {
                                                viewModel.setSecurity(useLock = true, pin = appSettings.appLockPin, useBiometrics = appSettings.useBiometrics)
                                            },
                                            onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                        )
                                    } else {
                                        viewModel.setSecurity(useLock = false, pin = null, useBiometrics = false)
                                    }
                                },
                                onBiometricsToggle = { useBio ->
                                    if (useBio && activity != null) {
                                        biometricManager?.showBiometricPrompt(
                                            title = "Confirm Identity",
                                            subtitle = "Authenticate to enable Biometrics",
                                            onSuccess = {
                                                viewModel.setSecurity(useLock = appSettings.useAppLock, pin = appSettings.appLockPin, useBiometrics = true)
                                            },
                                            onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                        )
                                    } else {
                                        viewModel.setSecurity(useLock = appSettings.useAppLock, pin = appSettings.appLockPin, useBiometrics = false)
                                    }
                                },
                                onSecuritySettingsClick = { showSecurityDialog = true }
                            )
                        }
                        item { SettingsSectionHeader(stringResource(R.string.data_management)) }
                        item { DataManagementSetting(appSettings, { showBackupDialog = true }, { createDocumentLauncher.launch("ExpenseTracker_Backup_${LocalDate.now()}.json") }, { openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }) }
                        
                        item { SettingsSectionHeader("About") }
                        item { 
                            AboutSetting(
                                onPrivacyPolicyClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Jamesalem/ExpenseTracker/blob/main/PRIVACY_POLICY.md"))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showCurrencyPicker) {
            CurrencyPickerDialog(
                currentCurrency = appSettings.defaultCurrency,
                onCurrencySelected = { newCurrency ->
                    viewModel.setCurrency(newCurrency)
                    showCurrencyPicker = false
                },
                onConvertHistorical = { newCurrency, rate ->
                    viewModel.convertHistoricalCurrency(newCurrency, rate)
                    showCurrencyPicker = false
                },
                onDismiss = { showCurrencyPicker = false }
            )
        }
        if (showBudgetDialog) {
            BudgetSettingDialog(
                currentAmount = appSettings.budgetAmount,
                currentPeriod = appSettings.budgetPeriod,
                onConfirm = { amount, period ->
                    viewModel.setBudget(amount, period)
                    showBudgetDialog = false
                },
                onDismiss = { showBudgetDialog = false }
            )
        }
        if (showCategoryDialog) {
            CategoryManagementDialog(
                categories = appSettings.customCategories,
                onConfirm = { newCategories ->
                    viewModel.updateCustomCategories(newCategories)
                    showCategoryDialog = false
                },
                onDismiss = { showCategoryDialog = false }
            )
        }
        if (showThemeDialog) {
            ThemeSettingDialog(
                currentTheme = appSettings.themeMode,
                onThemeSelected = { mode ->
                    viewModel.setTheme(mode)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }
        if (showDecimalPlacesDialog) {
            DecimalPlacesDialog(
                currentDecimalPlaces = appSettings.decimalPlaces,
                onConfirm = { places ->
                    viewModel.setDecimalPlaces(places)
                    showDecimalPlacesDialog = false
                },
                onDismiss = { showDecimalPlacesDialog = false }
            )
        }
        if (showBackupDialog) {
            BackupSettingDialog(
                enabled = appSettings.autoBackupEnabled,
                frequency = appSettings.autoBackupFrequency,
                onConfirm = { enabled, frequency ->
                    viewModel.setAutoBackup(enabled, frequency)
                    showBackupDialog = false
                },
                onDismiss = { showBackupDialog = false }
            )
        }
        if (showNotificationsDialog) {
            NotificationSettingsDialog(
                enableNotifications = appSettings.enableNotifications,
                notificationTime = appSettings.notificationTime,
                weeklyReminderDay = appSettings.weeklyReminderDay,
                onConfirm = { enabled, time, weeklyDay ->
                    viewModel.setNotifications(enabled = enabled, time = time, weeklyDay = weeklyDay)
                    showNotificationsDialog = false
                },
                onDismiss = { showNotificationsDialog = false }
            )
        }
        if (showLoggingReminderDialog) {
            LoggingReminderDialog(
                enabled = appSettings.loggingReminderEnabled,
                reminderTime = appSettings.loggingReminderTime,
                onConfirm = { enabled, time ->
                    viewModel.setLoggingReminder(enabled, time)
                    showLoggingReminderDialog = false
                },
                onDismiss = { showLoggingReminderDialog = false }
            )
        }
        if (showPomodoroDialog) {
            PomodoroDurationDialog(
                currentDuration = appSettings.pomodoroDurationMinutes,
                onConfirm = { mins ->
                    viewModel.setPomodoroDuration(mins)
                    showPomodoroDialog = false
                },
                onDismiss = { showPomodoroDialog = false }
            )
        }
        if (showSecurityDialog) {
            SecuritySettingsDialog(
                useAppLock = appSettings.useAppLock,
                useBiometrics = appSettings.useBiometrics,
                onConfirm = { lockEnabled, bioEnabled ->
                    viewModel.setSecurity(useLock = lockEnabled, pin = if (lockEnabled) appSettings.appLockPin else null, useBiometrics = bioEnabled)
                },
                onSetupPin = {
                    showSecurityDialog = false
                    showPinSetupDialog = true
                },
                onDismiss = { showSecurityDialog = false }
            )
        }
        if (showPinSetupDialog) {
            PinSetupDialog(
                onPinSetupComplete = { pin ->
                    viewModel.setSecurity(useLock = true, pin = pin, useBiometrics = appSettings.useBiometrics)
                    showPinSetupDialog = false
                },
                onDismiss = { showPinSetupDialog = false }
            )
        }
    }
}
