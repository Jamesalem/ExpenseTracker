// ui/components/pickers/MonthPicker.kt
package com.example.expensetracker.ui.components.pickers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.Dimens
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthPicker(
    yearMonth: YearMonth,
    onYearMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val fmt = DateTimeFormatter.ofPattern("MMMM yyyy")

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { onYearMonthChange(yearMonth.minusMonths(1)) }) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = stringResource(R.string.previous_month),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = yearMonth.format(fmt),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.medium),
            textAlign = TextAlign.Center
        )

        IconButton(onClick = { onYearMonthChange(yearMonth.plusMonths(1)) }) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.next_month),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
