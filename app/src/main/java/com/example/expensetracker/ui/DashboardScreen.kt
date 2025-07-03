package com.example.expensetracker.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.time.YearMonth
import java.util.Calendar
import java.util.Date

@Composable
fun DashboardScreen(
    navController: NavController,
    vm: ExpenseViewModel = hiltViewModel()
) {
    val expenses by vm.expenses.collectAsState()
    val budgets by vm.budgets.collectAsState()

    // 1) Month selector state
    var selectedYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val periodKey = remember(selectedYearMonth) {
        "%04d-%02d".format(selectedYearMonth.year, selectedYearMonth.monthValue)
    }

    // 2) Filter this month's expenses
    val monthExpenses = remember(expenses, selectedYearMonth) {
        expenses.filter { exp ->
            val cal = Calendar.getInstance().apply { time = Date(exp.date) }
            cal.get(Calendar.YEAR) == selectedYearMonth.year &&
                    cal.get(Calendar.MONTH) + 1    == selectedYearMonth.monthValue
        }
    }

    // 3) Current budget (or 0.0 if none) + editable text state
    val currentBudget = budgets.find { it.periodKey == periodKey }?.amount ?: 0.0
    var budgetInput by remember(currentBudget) { mutableStateOf(currentBudget.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ─── Month selector ─────────────────────────────────────────────
        MonthPicker(
            yearMonth = selectedYearMonth,
            onYearMonthChange = { selectedYearMonth = it }
        )

        // ─── Budget entry ──────────────────────────────────────────────
        OutlinedTextField(
            value = budgetInput,
            onValueChange = { budgetInput = it },
            label = { Text("Budget for $periodKey") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                IconButton(onClick = {
                    val amt = budgetInput.toDoubleOrNull() ?: 0.0
                    vm.upsertBudget(periodKey, amt)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Save Budget"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ─── Navigate to full list ──────────────────────────────────────
        OutlinedButton(
            onClick = { navController.navigate("list") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "View All"
            )
            Spacer(Modifier.width(8.dp))
            Text("View All Expenses")
        }

        // ─── Combined PieChart ────────────────────────────────────────
        val spentByCategory = remember(monthExpenses) {
            monthExpenses
                .groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amount }.toFloat() }
        }

        val entries = remember(spentByCategory, currentBudget) {
            val spentTotal = spentByCategory.values.sum()
            val remaining = (currentBudget - spentTotal).toFloat().coerceAtLeast(0f)
            buildList {
                add(PieEntry(remaining, "Remaining"))
                spentByCategory.forEach { (cat, amt) ->
                    add(PieEntry(amt, cat))
                }
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { ctx ->
                createPieChart(ctx, entries)
            },
            update = { chart ->
                // 1) re‑set the data
                chart.data = PieData(
                    PieDataSet(entries, "").apply {
                        sliceSpace = 2f
                        valueTextSize = 12f
                    }
                )
                // 2) notify & redraw
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        )
    }
}

/**
 * Helper to create and configure a PieChart with initial entries.
 * Returns the view for AndroidView.
 */
private fun createPieChart(context: Context, entries: List<PieEntry>): PieChart {
    return PieChart(context).apply {
        data = PieData(
            PieDataSet(entries, "").apply {
                sliceSpace = 2f
                valueTextSize = 12f
            }
        )
        description.isEnabled = false
        isRotationEnabled = true
        setUsePercentValues(false)
        legend.isWordWrapEnabled = true
        invalidate()
    }
}
