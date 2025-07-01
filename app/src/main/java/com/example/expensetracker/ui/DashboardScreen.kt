package com.example.expensetracker.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    vm: ExpenseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val expenses by vm.expenses.collectAsState()

    // Filter to current month
    val thisMonth = remember(expenses) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        expenses.filter { exp ->
            cal.time = Date(exp.date)
            cal.get(Calendar.MONTH) == currentMonth &&
                    cal.get(Calendar.YEAR) == currentYear
        }
    }

    // Group by category & sum
    val entries = remember(thisMonth) {
        thisMonth
            .groupBy { it.category }
            .map { (category, list) ->
                PieEntry(list.sumOf { it.amount }.toFloat(), category)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            factory = { createPieChart(context, entries) }
        )
    }
}

// Helper to configure the PieChart view
private fun createPieChart(context: Context, entries: List<PieEntry>): PieChart {
    return PieChart(context).apply {
        data = PieData(PieDataSet(entries, "").apply {
            sliceSpace = 2f
            valueTextSize = 12f
        })
        description.isEnabled = false
        isRotationEnabled = true
        setUsePercentValues(false)
        legend.isWordWrapEnabled = true
        invalidate()
    }
}
