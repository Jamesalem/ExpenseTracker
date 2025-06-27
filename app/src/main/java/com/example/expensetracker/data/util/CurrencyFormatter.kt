package com.example.expensetracker.data.util

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {

    /**
     * Find a Locale whose default Currency matches the code,
     * or fall back to Locale.getDefault() if none found.
     */
    private fun localeFor(currencyCode: String): Locale {
        return Locale.getAvailableLocales()
            .firstOrNull {
                runCatching {
                    Currency.getInstance(it).currencyCode == currencyCode
                }.getOrDefault(false)
            }
            ?: Locale.getDefault()
    }

    /** All available ISO 4217 codes, sorted alphabetically */
    val currencyCodes: List<String> = Currency
        .getAvailableCurrencies()
        .map(Currency::getCurrencyCode)
        .sorted()

    /** Format an amount according to the code’s locale rules */
    fun format(amount: Double, currencyCode: String): String {
        val currency = runCatching { Currency.getInstance(currencyCode) }
            .getOrNull()
            ?: Currency.getInstance("USD")

        val locale = localeFor(currency.currencyCode)
        val nf = NumberFormat.getCurrencyInstance(locale)
        nf.currency = currency
        return nf.format(amount)
    }
}
