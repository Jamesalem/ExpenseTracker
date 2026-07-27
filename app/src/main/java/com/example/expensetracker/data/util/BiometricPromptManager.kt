package com.example.expensetracker.data.util

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricPromptManager(
    private val activity: FragmentActivity
) {

    fun showBiometricPrompt(
        title: String = "Unlock Expense Tracker",
        subtitle: String = "Authenticate to access your private financial data",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Authentication failed. Please try again.")
                    }
                }
            )

            val allowedAuthenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            } else {
                BIOMETRIC_STRONG
            }

            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(allowedAuthenticators)

            // Do not call setNegativeButtonText if DEVICE_CREDENTIAL is set to avoid IllegalArgumentException
            if ((allowedAuthenticators and DEVICE_CREDENTIAL) == 0) {
                promptInfoBuilder.setNegativeButtonText("Cancel")
            }

            prompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Biometric authentication error")
        }
    }

    fun canAuthenticate(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val allowedAuthenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            } else {
                BIOMETRIC_STRONG
            }
            biometricManager.canAuthenticate(allowedAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }
}
