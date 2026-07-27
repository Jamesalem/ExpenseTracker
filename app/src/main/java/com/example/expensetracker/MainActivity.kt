package com.example.expensetracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.example.expensetracker.ui.navigation.AppNavHost
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Enable drawing behind system bars for full edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(window, false)

        enableEdgeToEdge()

        setContent {
            ExpenseTrackerTheme {
                AppNavHost(Modifier.fillMaxSize())
            }
        }
    }
}
