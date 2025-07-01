package com.example.expensetracker.data.local

import android.content.Context
import androidx.room.*
import com.example.expensetracker.data.model.Expense

@Database(
    entities = [Expense::class],
    version = 2,      // bump to 2 because of the new currencyCode field
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                )
                    // simple fallback for dev—wipe & rebuild on schema change,
                    // dropping *all* tables (recommended true)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
