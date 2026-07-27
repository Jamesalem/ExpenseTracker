package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.dao.SubscriptionDao
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.Subscription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    val subscriptions: StateFlow<List<Subscription>> = subscriptionDao.observeActiveSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMonthlyCost: StateFlow<Double?> = subscriptionDao.observeTotalMonthlySubscriptionsCost()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addSubscription(
        title: String,
        amount: Double,
        category: String,
        billingCycle: String,
        nextDueDateString: String,
        note: String?
    ) {
        viewModelScope.launch {
            val sub = Subscription(
                title = title.ifBlank { "Subscription" },
                amount = amount,
                category = category.ifBlank { "Subscriptions" },
                billingCycle = billingCycle,
                nextDueDateString = nextDueDateString.ifBlank {
                    LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                },
                note = note
            )
            subscriptionDao.insert(sub)
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            subscriptionDao.deleteById(id)
        }
    }

    fun payAndRecordExpense(subscription: Subscription, defaultCurrency: String) {
        viewModelScope.launch {
            val expense = Expense(
                title = "Subscription: ${subscription.title}",
                amount = subscription.amount,
                date = LocalDate.now(),
                category = subscription.category,
                note = "Recurring ${subscription.billingCycle.lowercase()} payment",
                type = Expense.ExpenseType.EXPENSE,
                currencyCode = defaultCurrency
            )
            expenseDao.insert(expense)

            // Calculate next due date (e.g. +1 month)
            val currentDue = try {
                LocalDate.parse(subscription.nextDueDateString)
            } catch (e: Exception) {
                LocalDate.now()
            }
            val nextDue = if (subscription.billingCycle == "YEARLY") {
                currentDue.plusYears(1)
            } else {
                currentDue.plusMonths(1)
            }

            val updatedSub = subscription.copy(
                nextDueDateString = nextDue.format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
            subscriptionDao.update(updatedSub)
        }
    }
}
