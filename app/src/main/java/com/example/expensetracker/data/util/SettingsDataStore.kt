package com.example.expensetracker.data.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// 1) Define the DataStore on Context
val Context.dataStore by preferencesDataStore(name = "user_preferences")

// 2) Key for default currency
val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
