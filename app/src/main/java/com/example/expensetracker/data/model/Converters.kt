// Converters.kt
package com.example.expensetracker.data.model

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Register this class in your @Database annotation:
 *   @TypeConverters(Converters::class)
 */
class Converters {
    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter fun fromLocalDate(date: LocalDate): String = isoDate.format(date)
    @TypeConverter fun toLocalDate(value: String): LocalDate = LocalDate.parse(value, isoDate)

    @TypeConverter fun fromDayOfWeek(day: DayOfWeek): String = day.name
    @TypeConverter fun toDayOfWeek(name: String): DayOfWeek = DayOfWeek.valueOf(name)

    @TypeConverter fun fromStringList(list: List<String>?): String? = list?.joinToString(";")
    @TypeConverter fun toStringList(data: String?): List<String> =
        data?.split(";")?.filter { it.isNotEmpty() } ?: emptyList()

    @TypeConverter fun fromBackupFrequency(freq: BackupFrequency): String = freq.name
    @TypeConverter fun toBackupFrequency(name: String): BackupFrequency =
        BackupFrequency.valueOf(name)

    @TypeConverter fun fromThemeMode(mode: ThemeMode): String = mode.name
    @TypeConverter fun toThemeMode(name: String): ThemeMode =
        ThemeMode.valueOf(name)

    @TypeConverter fun fromBudgetPeriod(period: BudgetPeriod): String = period.name
    @TypeConverter fun toBudgetPeriod(name: String): BudgetPeriod =
        BudgetPeriod.valueOf(name)

    @TypeConverter fun fromExpenseType(type: Expense.ExpenseType): String = type.name
    @TypeConverter fun toExpenseType(name: String): Expense.ExpenseType =
        Expense.ExpenseType.valueOf(name)
}
