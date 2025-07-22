package com.example.expensetracker.ui.setting

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import com.example.expensetracker.ui.setting.components.SettingsSectionHeader
import com.example.expensetracker.ui.setting.dialogs.BackupSettingDialog
import com.example.expensetracker.ui.setting.dialogs.BudgetSettingDialog
import com.example.expensetracker.ui.setting.dialogs.CategoryManagementDialog
import com.example.expensetracker.ui.setting.dialogs.CurrencyPickerDialog
import com.example.expensetracker.ui.setting.dialogs.DecimalPlacesDialog
import com.example.expensetracker.ui.setting.dialogs.NotificationSettingsDialog
import com.example.expensetracker.ui.setting.dialogs.PinSetupDialog
import com.example.expensetracker.ui.setting.dialogs.SecuritySettingsDialog
import com.example.expensetracker.ui.setting.dialogs.ThemeSettingDialog
import com.example.expensetracker.ui.setting.sections.AppearanceSetting
import com.example.expensetracker.ui.setting.sections.BudgetSetting
import com.example.expensetracker.ui.setting.sections.CategorySetting
import com.example.expensetracker.ui.setting.sections.CurrencySetting
import com.example.expensetracker.ui.setting.sections.DataManagementSetting
import com.example.expensetracker.ui.setting.sections.NotificationSetting
import com.example.expensetracker.ui.setting.sections.SecuritySetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val appSettings = (uiState as? SettingsViewModel.SettingsUiState.Success)?.settings ?: AppSettings()
    val context = LocalContext.current

    // Dialog states
    var showCurrencyPicker     by remember { mutableStateOf(false) }
    var showBudgetDialog       by remember { mutableStateOf(false) }
    var showCategoryDialog     by remember { mutableStateOf(false) }
    var showThemeDialog        by remember { mutableStateOf(false) }
    var showBackupDialog       by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showSecurityDialog     by remember { mutableStateOf(false) }
    var showPinSetupDialog     by remember { mutableStateOf(false) }
    var showRestorePicker      by remember { mutableStateOf(false) }
    var showDecimalPlacesDialog by remember { mutableStateOf(false) }

    val backupResult  by viewModel.backupResult.collectAsState()
    val restoreResult by viewModel.restoreResult.collectAsState()

    // File picker for restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.performRestore(it) }
        showRestorePicker = false
    }

    // Handle backup result (share or toast)
    LaunchedEffect(backupResult) {
        backupResult?.let { (success, uri) ->
            if (success && uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        context.getString(R.string.share_backup)
                    )
                )
                Toast.makeText(context, R.string.backup_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.backup_failed, Toast.LENGTH_LONG).show()
            }
            viewModel.clearBackupResult()
        }
    }

    // Handle restore result (toast)
    LaunchedEffect(restoreResult) {
        restoreResult?.let { (success, message) ->
            if (success) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.restore_failed, message),
                    Toast.LENGTH_LONG
                ).show()
            }
            viewModel.clearRestoreResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is SettingsViewModel.SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SettingsViewModel.SettingsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as SettingsViewModel.SettingsUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is SettingsViewModel.SettingsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Currency Settings
                    item { SettingsSectionHeader(stringResource(R.string.currency_settings)) }
                    item {
                        CurrencySetting(
                            appSettings = appSettings,
                            onCurrencyClick = { showCurrencyPicker = true },
                            onDecimalClick = { showDecimalPlacesDialog = true },
                            onGroupingSeparatorChange = { useGrouping ->
                                viewModel.setGroupingSeparator(useGrouping)
                            }
                        )
                    }

                    // Budget Management
                    item { SettingsSectionHeader(stringResource(R.string.budget_management)) }
                    item {
                        BudgetSetting(
                            appSettings = appSettings,
                            onBudgetClick = { showBudgetDialog = true }
                        )
                    }

                    // Expense Categories
                    item { SettingsSectionHeader(stringResource(R.string.expense_categories)) }
                    item {
                        CategorySetting(
                            appSettings = appSettings,
                            onCategoryClick = { showCategoryDialog = true }
                        )
                    }

                    // Appearance
                    item { SettingsSectionHeader(stringResource(R.string.appearance)) }
                    item {
                        AppearanceSetting(
                            appSettings = appSettings,
                            onThemeClick = { showThemeDialog = true }
                        )
                    }

                    // Notifications
                    item { SettingsSectionHeader(stringResource(R.string.notifications)) }
                    item {
                        NotificationSetting(
                            appSettings = appSettings,
                            onNotificationToggle = { newValue ->
                                viewModel.setNotifications(enabled = newValue)
                            },
                            onNotificationSettingsClick = { showNotificationsDialog = true }
                        )
                    }

                    // Security
                    item { SettingsSectionHeader(stringResource(R.string.security)) }
                    item {
                        SecuritySetting(
                            appSettings = appSettings,
                            onAppLockToggle = { useLock ->
                                viewModel.setSecurity(
                                    useLock = useLock,
                                    pin = if (useLock) appSettings.appLockPin else null,
                                    useBiometrics = appSettings.useBiometrics
                                )
                            },
                            onBiometricsToggle = { useBio ->
                                viewModel.setSecurity(
                                    useLock = appSettings.useAppLock,
                                    pin = appSettings.appLockPin,
                                    useBiometrics = useBio
                                )
                            },
                            onSecuritySettingsClick = { showSecurityDialog = true }
                        )
                    }

                    // Data Management
                    item { SettingsSectionHeader(stringResource(R.string.data_management)) }
                    item {
                        DataManagementSetting(
                            appSettings = appSettings,
                            onBackupClick = { showBackupDialog = true },
                            onBackupNow = { viewModel.performBackup() },
                            onRestore = { showRestorePicker = true }
                        )
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
                onDismiss = { showCurrencyPicker = false }
            )
        }
        if (showBudgetDialog) {
            BudgetSettingDialog(
                currentAmount = appSettings.budgetAmount,
                currentPeriod = appSettings.budgetPeriod,
                onConfirm = { amt, period ->
                    viewModel.setBudget(amt, period)
                    showBudgetDialog = false
                },
                onDismiss = { showBudgetDialog = false }
            )
        }
        if (showCategoryDialog) {
            CategoryManagementDialog(
                categories = appSettings.customCategories,
                onConfirm = { cats ->
                    viewModel.updateCustomCategories(cats)
                    showCategoryDialog = false
                },
                onDismiss = { showCategoryDialog = false }
            )
        }
        if (showThemeDialog) {
            ThemeSettingDialog(
                currentTheme = appSettings.themeMode,
                onThemeSelected = { theme ->
                    viewModel.setTheme(theme)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }
        if (showBackupDialog) {
            BackupSettingDialog(
                enabled = appSettings.autoBackupEnabled,
                frequency = appSettings.autoBackupFrequency,
                onConfirm = { enabled, freq ->
                    viewModel.setAutoBackup(enabled, freq)
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
                onConfirm = { enabled, time, day ->
                    viewModel.setNotifications(enabled, time, day)
                    showNotificationsDialog = false
                },
                onDismiss = { showNotificationsDialog = false }
            )
        }
        if (showSecurityDialog) {
            SecuritySettingsDialog(
                useAppLock = appSettings.useAppLock,
                useBiometrics = appSettings.useBiometrics,
                onConfirm = { useLock, useBio ->
                    viewModel.setSecurity(useLock, appSettings.appLockPin, useBio)
                    showSecurityDialog = false
                },
                onDismiss = { showSecurityDialog = false },
                onSetupPin = {
                    showSecurityDialog = false
                    showPinSetupDialog = true
                }
            )
        }
        if (showPinSetupDialog) {
            PinSetupDialog(
                onPinSetupComplete = { pin ->
                    viewModel.setSecurity(appSettings.useAppLock, pin, appSettings.useBiometrics)
                    showPinSetupDialog = false
                },
                onDismiss = { showPinSetupDialog = false }
            )
        }
        // Trigger file picker outside LazyColumn
        if (showRestorePicker) {
            LaunchedEffect(Unit) {
                filePickerLauncher.launch("application/json")
            }
        }
        // NEW: DecimalPlacesDialog integration
        if (showDecimalPlacesDialog) {
            DecimalPlacesDialog(
                currentDecimalPlaces = appSettings.decimalPlaces,
                onConfirm = { newDecimalPlaces ->
                    viewModel.setDecimalPlaces(newDecimalPlaces)
                    showDecimalPlacesDialog = false
                },
                onDismiss = { showDecimalPlacesDialog = false }
            )
        }
    }
}