package com.example.expensetracker.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.dao.SubscriptionDao
import com.example.expensetracker.data.math.AnomalyDetector
import com.example.expensetracker.data.math.CashflowForecaster
import com.example.expensetracker.data.math.FinancialHealthEngine
import com.example.expensetracker.data.math.SafeSpendEngine
import com.example.expensetracker.data.model.AppSettings
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val budgetRepo: BudgetRepository,
    private val settingsRepo: SettingsRepository,
    private val subscriptionDao: SubscriptionDao
) : ViewModel() {

    sealed class DashboardUiState {
        data object Loading : DashboardUiState()
        data class Success(
            val expenses: List<Expense>,
            val budgets: List<Budget>,
            val settings: AppSettings,
            val selectedMonth: YearMonth,
            val totalIncome: Double,
            val expenseTotal: Double,
            val billsTotal: Double,
            val totalSpent: Double,
            val spentByCategory: List<Pair<String, Double>>,
            val safeSpendResult: SafeSpendEngine.SafeSpendResult,
            val cashflowForecast: CashflowForecaster.CashflowForecastResult,
            val anomalies: List<AnomalyDetector.AnomalyResult>,
            val healthScore: FinancialHealthEngine.HealthScoreBreakdown
        ) : DashboardUiState()
        data class Error(val message: String) : DashboardUiState()
    }

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        loadDashboardData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadDashboardData() {
        viewModelScope.launch {
            _selectedMonth.flatMapLatest { month ->
                val start = month.atDay(1)
                val end = month.atEndOfMonth()
                
                combine(
                    expenseRepo.observeExpensesBetweenDates(start, end),
                    expenseRepo.observeAllExpenses(),
                    budgetRepo.observeAllBudgets(),
                    settingsRepo.appSettings,
                    subscriptionDao.observeActiveSubscriptions()
                ) { monthExpenses, allExpenses, budgets, settings, subscriptions ->
                    val totalIncome = monthExpenses
                        .filter { it.type == Expense.ExpenseType.INCOME }
                        .sumOf { it.amount }

                    val expenseTotal = monthExpenses
                        .filter { it.type == Expense.ExpenseType.EXPENSE }
                        .sumOf { it.amount }

                    val billsTotal = subscriptions.sumOf { it.amount }
                    val totalSpent = expenseTotal + billsTotal

                    val spentByCategory = monthExpenses
                        .filter { it.type == Expense.ExpenseType.EXPENSE }
                        .groupBy { it.category }
                        .mapValues { it.value.sumOf(Expense::amount) }
                        .toList()
                        .sortedByDescending { it.second }

                    val periodKey = "${month.year}-${month.monthValue}"
                    val currentBudget = budgets.firstOrNull { it.periodKey == periodKey }?.amount
                        ?: settings.budgetAmount

                    // 1. Daily spend history for variance estimation
                    val dailySpendMap = monthExpenses
                        .filter { it.type == Expense.ExpenseType.EXPENSE }
                        .groupBy { it.date }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }

                    val dailySpendHistory = (1..month.lengthOfMonth()).map { day ->
                        val date = month.atDay(day)
                        dailySpendMap[date] ?: 0.0
                    }

                    // 2. SafeSpendEngine
                    val safeSpendResult = SafeSpendEngine.calculateSafeSpend(
                        totalBudget = currentBudget,
                        currentSpent = totalSpent,
                        pendingFixedBills = billsTotal,
                        dailySpendHistory = dailySpendHistory,
                        currentDate = if (month == YearMonth.now()) LocalDate.now() else start
                    )

                    // 3. Cashflow Forecasting (All-time daily net flows)
                    val allDailyNetFlows = allExpenses
                        .groupBy { it.date }
                        .toSortedMap()
                        .map { entry ->
                            val inc = entry.value.filter { it.type == Expense.ExpenseType.INCOME }.sumOf { it.amount }
                            val exp = entry.value.filter { it.type == Expense.ExpenseType.EXPENSE }.sumOf { it.amount }
                            inc - exp
                        }

                    val totalLiquid = (allExpenses.filter { it.type == Expense.ExpenseType.INCOME }.sumOf { it.amount } -
                            allExpenses.filter { it.type == Expense.ExpenseType.EXPENSE }.sumOf { it.amount }).coerceAtLeast(0.0)

                    val cashflowForecast = CashflowForecaster.forecastCashflow(
                        currentBalance = totalLiquid,
                        historicalDailyNetFlows = allDailyNetFlows,
                        startDate = LocalDate.now(),
                        forecastHorizonDays = 30
                    )

                    // 4. Anomaly Detection
                    val anomalies = AnomalyDetector.detectExpenseAnomalies(monthExpenses)

                    // 5. Financial Health Scoring
                    val healthScore = FinancialHealthEngine.computeHealthScore(
                        monthlyIncome = totalIncome,
                        monthlyExpense = totalSpent,
                        monthlyBudget = currentBudget,
                        monthlyRecurringBills = billsTotal,
                        liquidSavings = totalLiquid
                    )

                    DashboardUiState.Success(
                        expenses = monthExpenses,
                        budgets = budgets,
                        settings = settings,
                        selectedMonth = month,
                        totalIncome = totalIncome,
                        expenseTotal = expenseTotal,
                        billsTotal = billsTotal,
                        totalSpent = totalSpent,
                        spentByCategory = spentByCategory,
                        safeSpendResult = safeSpendResult,
                        cashflowForecast = cashflowForecast,
                        anomalies = anomalies,
                        healthScore = healthScore
                    )
                }
            }
            .catch { e ->
                _uiState.value = DashboardUiState.Error("Error loading dashboard data: ${e.message}")
            }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateSelectedMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun saveBudget(periodKey: String, amount: Double) {
        viewModelScope.launch {
            try {
                budgetRepo.upsertBudget(Budget(periodKey = periodKey, amount = amount))
                _userMessage.emit("Budget saved successfully!")
            } catch (e: Exception) {
                _userMessage.emit("Failed to save budget: ${e.message}")
            }
        }
    }
}
