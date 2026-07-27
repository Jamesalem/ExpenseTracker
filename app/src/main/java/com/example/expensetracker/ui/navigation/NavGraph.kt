package com.example.expensetracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import com.example.expensetracker.ui.category.AddCategoryScreen
import com.example.expensetracker.ui.category.CategoryListScreen
import com.example.expensetracker.ui.dashboard.DashboardScreen
import com.example.expensetracker.ui.expense.ExpenseDetailScreen
import com.example.expensetracker.ui.expense.ExpenseEditScreen
import com.example.expensetracker.ui.expense.ExpenseFormScreen
import com.example.expensetracker.ui.expense.ExpenseListScreen
import com.example.expensetracker.ui.home.HomeScreen
import com.example.expensetracker.ui.onboarding.OnboardingScreen
import com.example.expensetracker.ui.security.LockScreen
import com.example.expensetracker.ui.setting.SettingsScreen
import com.example.expensetracker.ui.subscription.SubscriptionScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.time.TimeLogListScreen
import com.example.expensetracker.ui.time.TimeTrackerScreen

object Routes {
    const val HOME           = "home"
    const val ONBOARDING     = "onboarding"
    const val LOCK           = "lock"
    const val EXPENSE_LIST   = "expenses"
    const val EXPENSE_FORM   = "expense_form"
    const val SETTINGS       = "settings"
    const val EXPENSE_DETAIL = "expense_detail"
    const val EXPENSE_EDIT   = "expense_edit"
    const val DASHBOARD      = "dashboard"
    const val CATEGORIES     = "categories"
    const val ADD_CATEGORY   = "add_category"
    const val TIME_TRACKER   = "time_tracker"
    const val TIME_LOGS      = "time_logs"
    const val SUBSCRIPTIONS  = "subscriptions"

    const val ARG_EXPENSE_ID = "expenseId"

    fun detailRoute(id: Long): String =
        "$EXPENSE_DETAIL/{$ARG_EXPENSE_ID}".replace("{$ARG_EXPENSE_ID}", id.toString())

    fun editRoute(id: Long): String =
        "$EXPENSE_EDIT/{$ARG_EXPENSE_ID}".replace("{$ARG_EXPENSE_ID}", id.toString())
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by settingsViewModel.appSettings.collectAsState(initial = null)
    val settings = settingsState

    if (settings == null) {
        ExpenseTrackerTheme {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    var isAuthenticated by remember {
        mutableStateOf(!(settings.useAppLock || settings.useBiometrics))
    }

    val startRoute = when {
        !settings.hasCompletedOnboarding -> Routes.ONBOARDING
        (settings.useAppLock || settings.useBiometrics) && !isAuthenticated -> Routes.LOCK
        else -> Routes.HOME
    }

    ExpenseTrackerTheme(
        darkTheme = when (settings.themeMode) {
            com.example.expensetracker.data.model.ThemeMode.DARK -> true
            com.example.expensetracker.data.model.ThemeMode.LIGHT -> false
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        }
    ) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = modifier
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOCK) {
                LockScreen(
                    onUnlocked = {
                        isAuthenticated = true
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOCK) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(navController)
            }

            composable(Routes.EXPENSE_LIST) {
                ExpenseListScreen(navController)
            }

            composable(Routes.EXPENSE_FORM) {
                ExpenseFormScreen(navController)
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(navController)
            }

            composable(
                route = "${Routes.EXPENSE_DETAIL}/{${Routes.ARG_EXPENSE_ID}}",
                arguments = listOf(navArgument(Routes.ARG_EXPENSE_ID) {
                    type = NavType.LongType
                })
            ) { backStack ->
                backStack.arguments
                    ?.getLong(Routes.ARG_EXPENSE_ID)
                    ?.takeIf { it >= 0L }
                    ?.let { id ->
                        ExpenseDetailScreen(navController, expenseId = id)
                    }
            }

            composable(
                route = "${Routes.EXPENSE_EDIT}/{${Routes.ARG_EXPENSE_ID}}",
                arguments = listOf(navArgument(Routes.ARG_EXPENSE_ID) {
                    type = NavType.LongType
                })
            ) { backStack ->
                backStack.arguments
                    ?.getLong(Routes.ARG_EXPENSE_ID)
                    ?.takeIf { it >= 0L }
                    ?.let { id ->
                        ExpenseEditScreen(navController, expenseId = id)
                    }
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(navController)
            }

            composable(Routes.CATEGORIES) {
                CategoryListScreen(onAddCategory = { navController.navigate(Routes.ADD_CATEGORY) })
            }

            composable(Routes.ADD_CATEGORY) {
                AddCategoryScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.TIME_TRACKER) {
                TimeTrackerScreen(navController)
            }

            composable(Routes.TIME_LOGS) {
                TimeLogListScreen(navController)
            }

            composable(Routes.SUBSCRIPTIONS) {
                SubscriptionScreen(navController)
            }
        }
    }
}
