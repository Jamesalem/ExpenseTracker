# Privacy Policy for ExpenseTracker

**Effective Date:** August 20, 2026  
**Last Updated:** August 20, 2026

Jamesalem Digital and Tech Foundation [JDTF] ("we," "us," or "our") built **ExpenseTracker** as a privacy-first, 100% offline personal finance and productivity management application. We believe your financial data belongs strictly to you.

This Privacy Policy explains how ExpenseTracker operates, how your data is handled, and why your privacy is guaranteed.

---

## 1. 100% Offline & Zero-Collection Guarantee

ExpenseTracker is architected from the ground up to operate **completely offline without internet connectivity**.

* **No Remote Servers:** We do not own, operate, or maintain any backend servers, databases, or cloud processing infrastructure for this application.
* **No Network Permission:** The `android.permission.INTERNET` permission is completely absent from the application manifest. The app is physically incapable of making outbound network requests or transmitting data over the internet.
* **Zero Telemetry or Analytics:** We do not embed third-party tracking libraries, advertising SDKs, behavioral analytics, or crash telemetry tools.
* **No Account Required:** You do not need to register, log in, or provide an email address, phone number, or name to use any feature of ExpenseTracker.

---

## 2. Information You Enter & Local Storage

All data entered into ExpenseTracker—including transactions, income, expenses, categories, tags, multi-wallet accounts, budgets, subscriptions, and focus time logs—is stored **locally and exclusively on your physical device** in a private SQLite/Room database (`AppDatabase` v15).

* **On-Device Mathematical Processing:** All financial algorithms, including Cash Flow Forecasting (Holt-Winters), Safe-to-Spend velocity calculations, Anomaly Detection (MAD / Modified Z-Score), Bayesian category predictions, and Financial Health scoring, execute locally on your device's CPU with zero cloud dependencies.
* **PIN & Vault Security:** If you choose to enable the App Lock/Vault feature, your PIN is never stored in plaintext. It is cryptographically hashed on-device using SHA-256 with a unique 16-byte cryptographically random salt.
* **Biometric Authentication:** If enabled, biometric authentication (fingerprint/face unlock) utilizes Android's official `androidx.biometric` APIs. Biometric data is processed directly by your device's secure hardware enclave (TEE/Keystore) and is never accessible to the app or developer.

---

## 3. Device Permissions & Purpose

ExpenseTracker requests only the minimal set of operating system permissions strictly necessary to deliver local features:

| Permission | Purpose |
| :--- | :--- |
| **`USE_BIOMETRIC` / `USE_FINGERPRINT`** | Authenticates your identity locally on-device to unlock the app vault if enabled. |
| **`POST_NOTIFICATIONS`** | Delivers scheduled budget thresholds, subscription renewal alerts, and timer completions locally on Android 13+. |
| **`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`** | Triggers exact Pomodoro focus session alarms and timer notifications. |
| **`VIBRATE`** | Provides tactile haptic feedback during stopwatch and timer interactions. |
| **Storage Access (SAF / Document Picker)** | Used solely when you explicitly tap **Export Backup** or **Restore Backup** via the Android Storage Access Framework. |

---

## 4. Backups & Data Ownership

You maintain 100% ownership and control over your financial records:

1. **Automated Local Backups:** The app features a background `AutoBackupWorker` that periodically dumps an encrypted-ready JSON backup to your app's isolated internal storage.
2. **Manual Export & Restore:** You can manually export your entire transaction history and configuration to JSON/CSV format at any time and share or store it in your chosen location.
3. **Data Deletion:** You can permanently erase all application data at any time by clearing the app data in Android Settings or uninstalling the application.

---

## 5. Third-Party Sharing

Because we do not collect, transmit, or store your data on external infrastructure, **we do not sell, rent, monetize, trade, or share any user data with third parties, advertisers, or data brokers.**

---

## 6. Children’s Privacy

ExpenseTracker does not collect personal data from anyone, including children under the age of 13. The application is completely safe for all audiences.

---

## 7. Global Privacy Compliance (GDPR, CCPA/CPRA, UK DPA 2018)

Because ExpenseTracker does not act as a data controller or data processor of user data on remote servers:
* **GDPR (EU & UK):** Complies with privacy-by-design and data minimization principles. All processing occurs under the personal/household exemption directly on user-owned hardware.
* **CCPA / CPRA:** No personal information is collected or sold.
* **Google Play Data Safety:** Fully compliant with Google Play Store Declarations ("No data collected", "No data shared with third parties").

---

## 8. Changes to This Policy

We may update this Privacy Policy to reflect future on-device features or regulatory standards. Updates will be published in our public repository and reflected within the app's **About** section.

---

## 9. Contact & Inquiries

For questions, feedback, or verification of our open-source codebase, visit our official repository:

* **GitHub Repository:** [https://github.com/Jamesalem/ExpenseTracker](https://github.com/Jamesalem/ExpenseTracker)
* **Organization:** Jamesalem Digital and Tech Foundation [JDTF]
