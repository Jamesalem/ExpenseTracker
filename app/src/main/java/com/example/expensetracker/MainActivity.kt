package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.expensetracker.ui.navigation.AppNavHost
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Enable drawing behind system bars for full edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Optional: for splash / status bar coloring if supported
        enableEdgeToEdge()

        setContent {
            ExpenseTrackerTheme {
                val view = LocalView.current

                // ✅ Apply insets manually so Compose content isn't clipped
                ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.updatePadding(
                        left = systemBars.left,
                        top = systemBars.top,
                        right = systemBars.right,
                        bottom = systemBars.bottom
                    )
                    insets
                }

                AppNavHost(Modifier.fillMaxSize())
            }
        }
    }
}
