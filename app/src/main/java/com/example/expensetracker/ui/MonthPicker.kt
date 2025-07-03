package com.example.expensetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.YearMonth

@Composable
fun MonthPicker(
    yearMonth: YearMonth,
    onYearMonthChange: (YearMonth) -> Unit,
    spacing: Dp = 8.dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(spacing), modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = {
            onYearMonthChange(yearMonth.minusMonths(1))
        }) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
        }

        Text(
            text = "%02d / %04d".format(yearMonth.monthValue, yearMonth.year),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        IconButton(onClick = {
            onYearMonthChange(yearMonth.plusMonths(1))
        }) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
        }
    }
}
