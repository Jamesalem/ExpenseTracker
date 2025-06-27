# ExpenseTracker

A simple Android expense-tracking app built with:

- **Jetpack Compose** for UI
- **Room** for local persistence
- **Hilt** for dependency injection
- **Kotlin Coroutines** + **Flow** for async data
- **MPAndroidChart** for a pie-chart dashboard
- **Coil** for image loading (optional receipts)

## Features

- List all expenses with swipe-to-delete
- Add new expenses (amount, date, category, note, currency)
- View a dashboard of this month’s spending by category
- Tap an expense to see details & edit
- Choose from all ISO-4217 currency codes
- Local database migrations (destructive by default)

## Requirements

- Android Studio Flamingo (or later)
- Android SDK 34 (compile/target)
- Java 17+ (for AGP 8.x + Hilt)
- Gradle 8.11+

## Getting Started

1. **Clone** this repo:
   ```bash
   git clone https://github.com/Jamesalem/ExpenseTracker.git
   cd ExpenseTracker


## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
