# ExpenseTracker — Privacy-First Financial Intelligence & Time Management

**Built for the Community, Powered by Privacy.**

ExpenseTracker is a modern, high-performance Android application designed to help individuals master their finances and time without compromising their data. This application is provided **free of charge** as a 100% offline, privacy-first tool to optimize personal productivity and financial health.

---

## 🌟 Core Pillars
- **100% Offline & Private:** Zero internet permissions in AndroidManifest. Your financial data, accounts, and focus logs never leave your physical device.
- **On-Device Financial Intelligence:** 6 statistical and predictive engines running completely on-device (Holt-Winters cash flow forecasting, Safe-to-Spend velocity, Bayesian category predictions, and Financial Health scoring).
- **Security Hardened:** SHA-256 + 16-byte random salt PIN hashing and biometric vault encryption.
- **Automated Local Backups:** Periodic background WorkManager backups to isolated local storage with manual JSON/CSV export.

---

## 🚀 Key Features

### 💰 Financial Management & Intelligence
- **Multi-Wallet Accounts:** Track Cash, Bank Accounts, Credit Cards, Savings, and Investments with dedicated balances.
- **Dynamic Safe-Spend Pulse:** Real-time daily allowance meter and spending burn velocity gauge.
- **30-Day Cash Flow Forecasting:** Holt-Winters predictive balance projection with financial runway estimation.
- **Financial Health Hub:** 0–100 composite Financial Health Index ($FHI$) across Savings Rate, Budget Velocity, Emergency Runway, and Fixed Debt Burden.
- **Offline Bayesian NLP Categorization:** Instant 1-tap category suggestions appearing as you type.
- **Anomaly Detection:** Modified Z-Score / Median Absolute Deviation (MAD) spike alerts and recurring subscription price-creep detection.
- **Subscription Management:** Recurring bills tracking with automated due date alerts and payment status toggling.

### ⏱️ Productivity & Focus
- **Productivity & Yield Analyzer:** Correlates focus time sessions with earned income to compute true Effective Hourly Yield ($EHY$).
- **Pomodoro & Stopwatch Modes:** Fluid timer controls with tactile haptic feedback and exact alarm notifications.
- **1-Tap Billable Conversion:** Convert billable client focus sessions directly into income records.

### 🔒 Security & Vault
- **Encrypted Local Vault:** Protect sensitive transactions with biometric authentication (fingerprint/face) or salted PIN hashing.
- **Local Data Export & Restore:** Full JSON/CSV backup and restore capabilities via Android Storage Access Framework (SAF).

---

## 🛠 Tech Stack
- **UI:** 100% Jetpack Compose (Material 3) with dynamic color palettes and fluid animations.
- **Persistence:** Room Database v15 with indexed queries and automated migrations.
- **Architecture:** MVVM + Clean Architecture + Kotlin Coroutines & Flows.
- **Dependency Injection:** Hilt (Dagger).
- **Background Tasks:** AndroidX WorkManager for automated rolling backups.
- **Mathematical Engines:** Pure on-device Kotlin algorithms ($O(1)$ to $O(N)$ runtime).

---

## 📱 Requirements
- **Android Version:** Android 8.0 (API 26) or higher.
- **Target SDK:** 35 (Android 15).
- **Permissions:** 0 Network Permissions (`android.permission.INTERNET` removed).

---

## 📜 Legal & Compliance
- [Privacy Policy](file:///c:/Projects/ExpenseTracker/PRIVACY_POLICY.md)
- [Terms of Service](file:///c:/Projects/ExpenseTracker/TERMS_OF_SERVICE.md)
- **License:** Distributed under the **MIT License**. Created by Jamesalem Digital and Tech Foundation [JDTF] for the global community.

---
*Optimizing personal finance and productivity, 100% privately on your device.*
