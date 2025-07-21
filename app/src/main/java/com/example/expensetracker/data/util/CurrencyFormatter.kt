package com.example.expensetracker.data.util

import java.text.NumberFormat
import java.util.Currency

object CurrencyFormatter { // Changed to object for singleton pattern
    private const val DEFAULT_DECIMAL_PLACES = 2
    private const val DEFAULT_USE_GROUPING = true

    fun format(amount: Double, currencyCode: String): String {
        return format(amount, currencyCode, DEFAULT_DECIMAL_PLACES, DEFAULT_USE_GROUPING)
    }

    fun format(amount: Double, currencyCode: String, decimalPlaces: Int, useGrouping: Boolean): String {
        val currency = try {
            Currency.getInstance(currencyCode)
        } catch (e: Exception) {
            Currency.getInstance("USD")
        }

        val format = NumberFormat.getCurrencyInstance().apply {
            this.currency = currency
            maximumFractionDigits = decimalPlaces
            minimumFractionDigits = decimalPlaces
            isGroupingUsed = useGrouping
        }
        return format.format(amount)
    }

    fun getSymbol(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            currencyCode
        }
    }

    fun getDisplayName(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).displayName
        } catch (e: Exception) {
            currencyCode
        }
    }
}