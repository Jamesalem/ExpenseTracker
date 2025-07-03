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

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            // Home
            composable("home") {
                HomeScreen(navController)
            }

            // List
            composable("list") {
                ExpenseListScreen(navController)
            }

            // Add
            composable("add") {
                ExpenseFormScreen(navController)
            }

            // Settings
            composable("settings") {
                SettingsScreen(navController)
            }

            // Detail
            composable(
                "detail/{expenseId}",
                arguments = listOf(navArgument("expenseId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                val id = backStackEntry.arguments!!.getLong("expenseId")
                ExpenseDetailScreen(
                    navController = navController,
                    expenseId = id
                )
            }

            // Edit
            composable(
                "edit/{expenseId}",
                arguments = listOf(navArgument("expenseId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                val id = backStackEntry.arguments!!.getLong("expenseId")
                ExpenseEditScreen(
                    navController = navController,
                    expenseId = id
                )
            }

            // Dashboard
            composable("dashboard") {
                DashboardScreen(navController)
            }
        }
    }
}
