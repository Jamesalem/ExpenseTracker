// ui/components/cards/BudgetCard.kt
package com.example.expensetracker.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.progressColor

@Composable
fun BudgetCard(
    budget: Double?,
    totalSpent: Double,
    currencyCode: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationS)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.monthly_budget),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_budget),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (budget != null && budget > 0) {
                val progress = (totalSpent / budget).toFloat().coerceIn(0f, 1f)
                val remaining = budget - totalSpent

                Text(
                    text = stringResource(
                        R.string.budget_usage,
                        CurrencyFormatter.format(totalSpent, currencyCode),
                        CurrencyFormatter.format(budget, currencyCode)
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = progressColor(progress),
                    trackColor = MaterialTheme.colorScheme.surface,
                    strokeCap = StrokeCap.Round,
                )

                Text(
                    text = stringResource(
                        R.string.budget_remaining,
                        CurrencyFormatter.format(remaining, currencyCode)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (remaining >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.extraSmall)) {
                    Text(
                        text = stringResource(R.string.no_budget_set),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.set_budget_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
