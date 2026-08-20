package com.example.expensetracker.data.math

import com.example.expensetracker.data.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NaiveBayesCategorizerTest {

    @Test
    fun testNaiveBayes_predictsCorrectCategory() {
        val categorizer = NaiveBayesCategorizer()
        val training = listOf(
            Expense(title = "Starbucks latte coffee", amount = 5.0, date = LocalDate.now(), category = "Food & Drink", currencyCode = "USD"),
            Expense(title = "McDonalds burger fries meal", amount = 10.0, date = LocalDate.now(), category = "Food & Drink", currencyCode = "USD"),
            Expense(title = "Shell Gas Station Petrol", amount = 45.0, date = LocalDate.now(), category = "Transportation", currencyCode = "USD"),
            Expense(title = "Uber Ride downtown", amount = 22.0, date = LocalDate.now(), category = "Transportation", currencyCode = "USD"),
            Expense(title = "Netflix monthly subscription", amount = 15.0, date = LocalDate.now(), category = "Entertainment", currencyCode = "USD"),
            Expense(title = "Spotify music premium", amount = 10.0, date = LocalDate.now(), category = "Entertainment", currencyCode = "USD")
        )

        categorizer.train(training)

        val foodPrediction = categorizer.predict("Coffee latte at downtown shop")
        assertTrue(foodPrediction.isNotEmpty())
        assertEquals("Food & Drink", foodPrediction.first().first)

        val transportPrediction = categorizer.predict("Uber ride to airport")
        assertTrue(transportPrediction.isNotEmpty())
        assertEquals("Transportation", transportPrediction.first().first)

        val subPrediction = categorizer.predict("Spotify subscription")
        assertTrue(subPrediction.isNotEmpty())
        assertEquals("Entertainment", subPrediction.first().first)
    }

    @Test
    fun testNaiveBayes_untrainedReturnsEmpty() {
        val categorizer = NaiveBayesCategorizer()
        val predictions = categorizer.predict("Unknown test string")
        assertTrue(predictions.isEmpty())
    }
}
