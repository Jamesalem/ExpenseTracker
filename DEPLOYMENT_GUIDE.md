# Master Deployment Guide: Google Play Console & GitHub Releases

This comprehensive guide covers everything needed to generate signed production builds (**APK for GitHub** and **AAB for Google Play**), set up your **Google Play Console Developer Account**, configure mandatory store policy declarations, and publish the application.

---

## Table of Contents
1. [Part 1: Generating the Production Keystore (`.jks`)](#part-1-generating-the-production-keystore-jks)
2. [Part 2: Building Signed Production Artifacts](#part-2-building-signed-production-artifacts)
   - [A. Building `.aab` for Google Play](#a-building-aab-for-google-play)
   - [B. Building `.apk` for GitHub Releases](#b-building-apk-for-github-releases)
3. [Part 3: Google Play Console Developer Account Registration](#part-3-google-play-console-developer-account-registration)
4. [Part 4: Play Console App Setup & Mandatory Policy Declarations](#part-4-play-console-app-setup--mandatory-policy-declarations)
   - [Data Safety Form (Exact Answers)](#data-safety-form-exact-answers)
   - [Financial Features Declaration](#financial-features-declaration)
   - [Privacy Policy URL](#privacy-policy-url)
5. [Part 5: Store Listing & Graphic Assets](#part-5-store-listing--graphic-assets)
6. [Part 6: Publishing to GitHub Releases](#part-6-publishing-to-github-releases)
7. [Part 7: Google Play Testing Tracks & Production Rollout](#part-7-google-play-testing-tracks--production-rollout)

---

## Part 1: Generating the Production Keystore (`.jks`)

A digital keystore cryptographically signs your app to prove your identity as the developer. Google Play requires all updates to be signed with the same key.

### Option A: Using Command Line (`keytool`)
Open PowerShell / Terminal in your project root and run:

```powershell
keytool -genkey -v -keystore release-keystore.jks -alias expensetracker-key -keyalg RSA -keysize 2048 -validity 10000
```

You will be prompted for:
1. **Keystore Password**: Choose a strong password and save it securely.
2. **First and Last Name**: e.g., `James Alem` or `Jamesalem Foundation`
3. **Organization Unit / Name**: e.g., `JDTF`
4. **City / State / Country Code**: e.g., `UK` / `US` / `NG`

### Option B: Using Android Studio GUI
1. In Android Studio, go to **Build** $\rightarrow$ **Generate Signed Bundle / APK...**
2. Choose **Android App Bundle** or **APK** $\rightarrow$ Click **Next**.
3. Under *Key store path*, click **Create new...**
4. Set path to `C:\Projects\ExpenseTracker\release-keystore.jks`.
5. Enter password, alias name (`expensetracker-key`), validity (25+ years), and certificate details. Click **OK**.

> [!CAUTION]
> **CRITICAL SECURITY RULE:**
> - Never commit `release-keystore.jks` or passwords to your public GitHub repository.
> - Back up your keystore file in multiple secure offline locations (e.g., encrypted USB drive, password manager). If you lose this key, you cannot update existing installs on Google Play.

---

## Part 2: Building Signed Production Artifacts

### A. Building `.aab` for Google Play
Run:
```powershell
.\gradlew bundleRelease
```
* **Output File Location**:
  `app/build/outputs/bundle/release/app-release.aab`
* **Purpose**: Upload this `.aab` file directly to Google Play Console. Google uses it to generate dynamic, optimized APKs for each user's specific screen density and CPU architecture.

### B. Building `.apk` for GitHub Releases
Run:
```powershell
.\gradlew assembleRelease
```
* **Output File Location**:
  `app/build/outputs/apk/release/app-release.apk`
* **Purpose**: Upload this standalone `.apk` to your GitHub Release page for direct side-loading.

---

## Part 3: Google Play Console Developer Account Registration

To publish apps on the Google Play Store, you must register for a Google Play Developer Account.

### Step 1: Requirements Checklist
Before beginning, make sure you have:
* A clean **Google Account** (preferably dedicated to your developer activities).
* **Credit or Debit Card** (Visa, MasterCard, Amex) to pay the **$25 USD one-time registration fee** (no recurring annual charge).
* **Government-Issued Photo ID** (Passport, Driver's License, or National ID Card) for identity verification.
* **Valid Phone Number & Physical Address**.

### Step 2: Account Type Selection
1. Navigate to: [https://play.google.com/console/signup](https://play.google.com/console/signup)
2. Sign in with your Google Account.
3. Select your **Account Type**:
   * **Personal Account (Individual):** Recommended if you are an independent creator. Requires identity verification and 14-day closed testing with 20 testers before production release.
   * **Organization Account:** Requires a **D-U-N-S Number** (Dun & Bradstreet registration) and official corporate registration documents.
4. Fill in:
   * **Developer Name** (Publicly shown on Play Store, e.g., `Jamesalem Foundation` or `James Alem`).
   * **Contact Email** & **Contact Phone Number** (verified via SMS/Call code).
   * **Website / GitHub Profile URL**.
5. Read and accept the **Google Play Developer Distribution Agreement**.
6. Pay the **$25 USD** registration fee.
7. Complete the **Identity Verification** step by uploading a clear photo of your ID when prompted. Verification usually takes 24–48 hours.

---

## Part 4: Play Console App Setup & Mandatory Policy Declarations

Once your developer account is approved:

### 1. Create App
1. Go to the Play Console dashboard $\rightarrow$ Click **Create App** (top right).
2. **App Name**: `ExpenseTracker - Offline Finance & Focus` (or `ExpenseTracker`)
3. **Default Language**: `English (United States)` or `English (United Kingdom)`
4. **App or Game**: `App`
5. **Free or Paid**: `Free`
6. Accept Declarations $\rightarrow$ Click **Create App**.

---

### 2. Mandatory Policy Declarations (Step-by-Step)

Navigate to **Policy and Programs** $\rightarrow$ **App Content** in the left menu:

#### A. Privacy Policy URL
* **URL to enter**:
  `https://raw.githubusercontent.com/Jamesalem/ExpenseTracker/main/PRIVACY_POLICY.md`  
  *(or your GitHub repository markdown URL)*

#### B. Data Safety Form (Exact Answers for 100% Offline App)
Because ExpenseTracker has **zero internet permissions**, answer as follows:
* **"Does your app collect or share any of the required user data types?"** $\rightarrow$ Select **NO**.
* **Result**: Play Store will display:
  > ✅ *"No data collected — The developer says this app doesn't collect user data"*
  > ✅ *"No data shared with third parties"*

#### C. Financial Features Declaration
* **Declaration**: Select **Personal Finance Management (PFM) / Budgeting Tool**.
* **Financial Services License**: Select **"My app does not provide loans, banking accounts, money transmission, or crypto trading."**

#### D. Target Audience and Content Rating
* **Target Age**: Select **18 and over** (or 13+).
* Complete the **IARC Content Rating Questionnaire**:
  - App Category: `Utility / Productivity / Finance`
  - Violence, Sexual Content, Offensive Language: `No` to all.
  - Shares user physical location: `No`.
  - Enables purchasing digital goods: `No`.
  - Rating Awarded: **Everyone (PEGI 3 / ESRB Everyone)**.

#### E. App Access
* Select **"All functionality is available without special access restrictions"** (since the app has no backend login).

#### F. Advertising ID
* **"Does your app use Advertising ID?"** $\rightarrow$ Select **NO**.

---

## Part 5: Store Listing & Graphic Assets

Navigate to **Grow** $\rightarrow$ **Store Presence** $\rightarrow$ **Main Store Listing**:

### 1. Text Details
* **App Name**: `ExpenseTracker: Offline Budget & Time`
* **Short Description** (up to 80 chars):
  `100% offline personal expense tracker, cash flow forecaster & focus timer.`
* **Full Description** (up to 4000 chars):
  ```text
  Take full control of your personal finances and productivity with 100% privacy and zero cloud dependence.

  ExpenseTracker is a high-performance, offline personal budgeting and time-tracking tool powered by on-device mathematical models. Your data never leaves your physical device.

  KEY FEATURES:
  • 100% Offline & Private: Zero internet permissions required. No tracking, no servers, zero data collection.
  • Dynamic Safe-to-Spend: Live daily allowance meter and burn velocity pace gauge.
  • 30-Day Cash Flow Forecasting: Predictive runway estimation powered by Holt-Winters exponential smoothing.
  • Financial Health Hub: Comprehensive 0–100 Financial Health Index across Savings, Budget, Runway, and Fixed Burden.
  • Multi-Wallet Accounts: Manage Cash, Bank Accounts, Credit Cards, and Savings.
  • Offline Bayesian Categorization: Real-time 1-tap category prediction chips as you type.
  • Statistical Anomaly Alerts: Spot sudden spending spikes and recurring subscription price creep.
  • Productivity & Focus: Integrated Pomodoro and stopwatch timer calculating your true Effective Hourly Yield (EHY).
  • Encrypted Local Vault: Protect your records with salted SHA-256 PIN hashing and biometric authentication.
  • Automated Local Backups: Scheduled rolling backups and manual JSON/CSV export.
  ```

### 2. Graphic Asset Specifications

| Asset | Dimensions | Format | Notes |
| :--- | :--- | :--- | :--- |
| **App Icon** | `512 x 512 px` | 32-bit PNG with alpha | Max 1 MB |
| **Feature Graphic** | `1024 x 500 px` | JPEG or 24-bit PNG | Main banner at top of store page |
| **Phone Screenshots** | Min `320 px`, Max `3840 px` | JPEG or 24-bit PNG | Minimum 2 screenshots (1080x1920 or 1080x2400 recommended) |

---

## Part 6: Publishing to GitHub Releases

To provide direct standalone APK downloads on your GitHub repository:

### Step 1: Build the Release APK
```powershell
.\gradlew assembleRelease
```
The file will be at: `app/build/outputs/apk/release/app-release.apk`

### Step 2: Create a Release on GitHub
1. Open your browser to: [https://github.com/Jamesalem/ExpenseTracker/releases](https://github.com/Jamesalem/ExpenseTracker/releases)
2. Click **Draft a new release** (or **Create a new release**).
3. **Choose a tag**: Type `v1.0.0` and click *Create new tag*.
4. **Target**: `main`
5. **Release title**: `ExpenseTracker v1.0.0 — Production Release`
6. **Description**:
   ```markdown
   ## What's New in v1.0.0
   - 100% Offline Architecture (Zero internet permissions).
   - Dynamic Safe-to-Spend Allowance & Burn Velocity Meter.
   - Holt-Winters 30-Day Cashflow Forecasting & Runway Projection.
   - Anomaly Detection (MAD Modified Z-Score) & Subscription Price Creep.
   - Offline Bayesian Text Classifier for instant 1-tap categorization.
   - Financial Health Hub (0-100 score with 4 resilience pillars).
   - Productivity Yield Engine (Effective Hourly Yield & Focus Tracking).
   - Multi-Account Wallets & Custom Tagging.
   - Salted SHA-256 PIN Vault & Biometric Authentication.
   - Automated Local Rolling Backups.

   ### Downloads
   - `ExpenseTracker-v1.0.0.apk` — Standalone signed APK for direct installation.
   ```
7. Drag and drop `app-release.apk` into the **Attach binaries** box (you can rename it to `ExpenseTracker-v1.0.0.apk`).
8. Click **Publish release**.

---

## Part 7: Google Play Testing Tracks & Production Rollout

### 1. Understanding Google Play Testing Requirements (Personal Accounts)
If you registered as a **Personal (Individual) Developer Account**, Google requires:
* **Closed Testing Track**: You must run a closed test with at least **20 opted-in testers for at least 14 consecutive days** before requesting production access.
* (Organization accounts are exempt from this 20-tester rule).

### 2. Uploading the Release Bundle (`.aab`)
1. In Google Play Console, go to **Release** $\rightarrow$ **Testing** $\rightarrow$ **Closed testing** (or **Production** if an organization).
2. Click **Create new release**.
3. Under *App bundles*, drag and drop `app-release.aab`.
4. Enter **Release name**: `1.0.0 (1)`.
5. Enter **Release notes**: Brief highlights of features.
6. Click **Next** $\rightarrow$ **Review release** $\rightarrow$ **Start rollout**.

### 3. Review & Approval
* Google’s automated and human review takes approximately **1 to 3 business days**.
* Once approved, your application will be live on the Google Play Store.
