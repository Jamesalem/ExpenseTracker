package com.example.expensetracker.ui.subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Subscription
import com.example.expensetracker.data.util.CurrencyFormatter
import com.example.expensetracker.data.viewmodel.SettingsViewModel
import com.example.expensetracker.data.viewmodel.SubscriptionViewModel
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navController: NavController,
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val subscriptions by subscriptionViewModel.subscriptions.collectAsState()
    val totalMonthlyCost by subscriptionViewModel.totalMonthlyCost.collectAsState()
    val settings by settingsViewModel.appSettings.collectAsState(initial = AppSettings())
    val haptic = LocalHapticFeedback.current

    var showAddCard by remember { mutableStateOf(false) }
    var subTitle by remember { mutableStateOf("") }
    var subAmountText by remember { mutableStateOf("") }
    var subBillingCycle by remember { mutableStateOf("MONTHLY") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Subscriptions & Bills", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddCard = !showAddCard 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = Shapes.large
            ) {
                Icon(
                    imageVector = if (showAddCard) Icons.Default.Close else Icons.Default.Add, 
                    contentDescription = "Toggle Add"
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            item {
                TotalSubscriptionCard(
                    totalMonthlyCost = totalMonthlyCost ?: 0.0,
                    settings = settings
                )
            }

            item {
                AnimatedVisibility(
                    visible = showAddCard,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    NewSubscriptionCard(
                        title = subTitle,
                        onTitleChange = { subTitle = it },
                        amount = subAmountText,
                        onAmountChange = { subAmountText = it },
                        cycle = subBillingCycle,
                        onCycleChange = { subBillingCycle = it },
                        onSave = {
                            val amt = subAmountText.toDoubleOrNull() ?: 0.0
                            if (subTitle.isNotBlank() && amt > 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                subscriptionViewModel.addSubscription(
                                    title = subTitle,
                                    amount = amt,
                                    category = "Subscriptions",
                                    billingCycle = subBillingCycle,
                                    nextDueDateString = "",
                                    note = null
                                )
                                subTitle = ""
                                subAmountText = ""
                                showAddCard = false
                            }
                        }
                    )
                }
            }

            if (subscriptions.isEmpty() && !showAddCard) {
                item {
                    EmptySubscriptionsView(modifier = Modifier.fillParentMaxHeight(0.6f))
                }
            } else if (subscriptions.isNotEmpty()) {
                item {
                    Text(
                        "Active Subscriptions (${subscriptions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                items(subscriptions, key = { it.id }) { sub ->
                    SubscriptionRowItem(
                        subscription = sub,
                        settings = settings,
                        onDelete = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            subscriptionViewModel.deleteSubscription(sub.id) 
                        },
                        onPay = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            subscriptionViewModel.payAndRecordExpense(sub, settings.defaultCurrency) 
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun NewSubscriptionCard(
    title: String,
    onTitleChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    cycle: String,
    onCycleChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Dimens.medium), verticalArrangement = Arrangement.spacedBy(Dimens.small)) {
            Text("Add Recurring Expense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Service Name") },
                placeholder = { Text("e.g., Netflix, Rent") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = Shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = Shapes.medium
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Billing Cycle", style = MaterialTheme.typography.labelSmall)
                    Row {
                        FilterChip(
                            selected = cycle == "MONTHLY",
                            onClick = { onCycleChange("MONTHLY") },
                            label = { Text("Monthly") }
                        )
                        Spacer(Modifier.width(4.dp))
                        FilterChip(
                            selected = cycle == "YEARLY",
                            onClick = { onCycleChange("YEARLY") },
                            label = { Text("Yearly") }
                        )
                    }
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = Shapes.medium
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("SAVE SUBSCRIPTION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptySubscriptionsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Refresh, 
            contentDescription = null, 
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text("No active subscriptions", style = MaterialTheme.typography.titleMedium)
        Text(
            "Track your recurring bills and payments here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TotalSubscriptionCard(totalMonthlyCost: Double, settings: AppSettings) {
    val emeraldGradient = remember {
        androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(Color(0xFF10B981), Color(0xFF059669))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(Shapes.large)
            .background(emeraldGradient)
            .padding(Dimens.large)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Wallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(Dimens.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Monthly Commitment",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    CurrencyFormatter.format(totalMonthlyCost, settings.defaultCurrency),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SubscriptionRowItem(
    subscription: Subscription,
    settings: AppSettings,
    onDelete: () -> Unit,
    onPay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    subscription.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        subscription.billingCycle.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(
                    if (subscription.nextDueDateString.isNotBlank()) "Next: ${subscription.nextDueDateString}" else "Recurring",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                CurrencyFormatter.format(subscription.amount, settings.defaultCurrency),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = onPay, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Pay",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
