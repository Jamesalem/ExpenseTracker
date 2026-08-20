package com.example.expensetracker.data.math

import com.example.expensetracker.data.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AnomalyDetectorTest {

    @Test
    fun testDetectExpenseAnomalies_findsSpike() {
        val date = LocalDate.now()
        val normalExpenses = (1..10).map {
            Expense(
                id = it.toLong(),
                title = "Lunch $it",
                amount = 15.0 + (it % 3),
                date = date,
                category = "Food",
                currencyCode = "USD"
            )
        }
        val spikeExpense = Expense(
            id = 99L,
            title = "Luxury Dinner",
            amount = 350.0,
            date = date,
            category = "Food",
            currencyCode = "USD"
        )

        val allExpenses = normalExpenses + spikeExpense
        val anomalies = AnomalyDetector.detectExpenseAnomalies(allExpenses)

        assertTrue(anomalies.isNotEmpty())
        assertEquals(99L, anomalies.first().expenseId)
        assertEquals(350.0, anomalies.first().amount, 0.001)
    }

    @Test
    fun testDetectExpenseAnomalies_noAnomaliesOnUniformData() {
        val date = LocalDate.now()
        val uniformExpenses = (1..10).map {
            Expense(
                id = it.toLong(),
                title = "Coffee $it",
                amount = 5.0,
                date = date,
                category = "Coffee",
                currencyCode = "USD"
            )
        }

        val anomalies = AnomalyDetector.detectExpenseAnomalies(uniformExpenses)
        assertTrue(anomalies.isEmpty())
    }
}
