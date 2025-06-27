package com.example.expensetracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun AppNavHost() {
    ExpenseTrackerTheme {
        val navController = rememberNavController()
        NavHost(navController, startDestination = "list") {

            composable("list") {
                ExpenseListScreen(navController)
            }

            composable("add") {
                ExpenseFormScreen(navController)
            }

            composable(
                "detail/{expenseId}",
                arguments = listOf(navArgument("expenseId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                ExpenseDetailScreen(
                    navController,
                    expenseId = backStackEntry.arguments!!.getLong("expenseId")
                )
            }

            composable(
                "edit/{expenseId}",
                arguments = listOf(navArgument("expenseId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                ExpenseEditScreen(
                    navController,
                    expenseId = backStackEntry.arguments!!.getLong("expenseId")
                )
            }
        }
    }
}
