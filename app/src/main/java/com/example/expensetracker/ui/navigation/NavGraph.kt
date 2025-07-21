// ui/navigation/NavGraph.kt
package com.example.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.ui.category.AddCategoryScreen
import com.example.expensetracker.ui.category.CategoryListScreen
import com.example.expensetracker.ui.dashboard.DashboardScreen
import com.example.expensetracker.ui.expense.ExpenseDetailScreen
import com.example.expensetracker.ui.expense.ExpenseEditScreen
import com.example.expensetracker.ui.expense.ExpenseFormScreen
import com.example.expensetracker.ui.expense.ExpenseListScreen
import com.example.expensetracker.ui.home.HomeScreen
import com.example.expensetracker.ui.setting.SettingsScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

object Routes {
    const val HOME           = "home"
    const val EXPENSE_LIST   = "expenses"
    const val EXPENSE_FORM   = "expense_form"
    const val SETTINGS       = "settings"
    const val EXPENSE_DETAIL = "expense_detail"
    const val EXPENSE_EDIT   = "expense_edit"
    const val DASHBOARD      = "dashboard"
    const val CATEGORIES     = "categories"
    const val ADD_CATEGORY   = "add_category"

    const val ARG_EXPENSE_ID = "expenseId"

    fun detailRoute(id: Long): String =
        "$EXPENSE_DETAIL/{$ARG_EXPENSE_ID}".replace("{$ARG_EXPENSE_ID}", id.toString())

    fun editRoute(id: Long): String =
        "$EXPENSE_EDIT/{$ARG_EXPENSE_ID}".replace("{$ARG_EXPENSE_ID}", id.toString())
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    ExpenseTrackerTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = modifier
        ) {
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
        }
    }
}
