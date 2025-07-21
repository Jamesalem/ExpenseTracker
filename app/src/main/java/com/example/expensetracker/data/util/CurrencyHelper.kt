package com.example.expensetracker.data.util

import java.util.Currency
import java.util.Locale

object CurrencyHelper {
    data class CurrencyInfo(
        val code: String,
        val name: String,
        val symbol: String,
        val fractionDigits: Int
    )

    val allCurrencies: List<CurrencyInfo> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Currency.getAvailableCurrencies()
            .mapNotNull { currency ->
                try {
                    val code = currency.currencyCode
                    CurrencyInfo(
                        code = code,
                        name = currency.getDisplayName(Locale.getDefault()) ?: code,
                        symbol = currency.getSymbol(Locale.getDefault()) ?: code,
                        fractionDigits = currency.defaultFractionDigits
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.code }
    }

    val popularCurrencies: List<CurrencyInfo> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        CurrencyLists.popularCodes.mapNotNull { code ->
            allCurrencies.firstOrNull { it.code == code }
        }
    }
}