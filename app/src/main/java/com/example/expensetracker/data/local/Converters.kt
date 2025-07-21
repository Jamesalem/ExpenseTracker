// data/local/Converters.kt
package com.example.expensetracker.data.local

import androidx.room.TypeConverter
import com.example.expensetracker.data.model.BackupFrequency
import com.example.expensetracker.data.model.BudgetPeriod
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.ThemeMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class Converters {

    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE
    private val ymFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    // LocalDate ↔ String
    @TypeConverter fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, isoDate) }

    @TypeConverter fun fromLocalDate(date: LocalDate?): String? =
        date?.format(isoDate)

    // YearMonth ↔ String
    @TypeConverter fun toYearMonth(value: String?): YearMonth? =
        value?.let { YearMonth.parse(it, ymFormatter) }

    @TypeConverter fun fromYearMonth(yearMonth: YearMonth?): String? =
        yearMonth?.format(ymFormatter)

    // Enum converters
    @TypeConverter fun toBudgetPeriod(value: String?): BudgetPeriod? =
        value?.let { BudgetPeriod.valueOf(it) }

    @TypeConverter fun fromBudgetPeriod(period: BudgetPeriod?): String? =
        period?.name

    @TypeConverter fun toThemeMode(value: String?): ThemeMode? =
        value?.let { ThemeMode.valueOf(it) }

    @TypeConverter fun fromThemeMode(mode: ThemeMode?): String? =
        mode?.name

    @TypeConverter fun toBackupFrequency(value: String?): BackupFrequency? =
        value?.let { BackupFrequency.valueOf(it) }

    @TypeConverter fun fromBackupFrequency(frequency: BackupFrequency?): String? =
        frequency?.name

    @TypeConverter fun toDayOfWeek(value: String?): DayOfWeek? =
        value?.let { DayOfWeek.valueOf(it) }

    @TypeConverter fun fromDayOfWeek(day: DayOfWeek?): String? =
        day?.name

    // ExpenseType converter
    @TypeConverter fun toExpenseType(value: String?): Expense.ExpenseType? =
        value?.let { Expense.ExpenseType.valueOf(it) }

    @TypeConverter fun fromExpenseType(type: Expense.ExpenseType?): String? =
        type?.name

    // List<String> ↔ comma‑separated
    @TypeConverter fun fromStringList(list: List<String>?): String? =
        list?.joinToString(",")

    @TypeConverter fun toStringList(data: String?): List<String>? =
        data?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
}
