package com.example.expensetracker.ui.security

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.R
import com.example.expensetracker.data.util.BiometricPromptManager
import com.example.expensetracker.data.util.SecurityUtil
import com.example.expensetracker.data.viewmodel.SettingsViewModel

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by settingsViewModel.appSettings.collectAsState(initial = null)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val activity = context as? FragmentActivity
    
    var visible by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val darkBackgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
    }

    fun triggerBiometricAuth() {
        activity?.let { act ->
            val manager = BiometricPromptManager(act)
            manager.showBiometricPrompt(
                title = "Unlock Expense Tracker",
                subtitle = "Authenticate to continue",
                onSuccess = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUnlocked() 
                },
                onError = { /* Fail silently */ }
            )
        }
    }

    fun handlePinKey(key: String) {
        errorMessage = null
        if (key == "⌫") {
            if (pinInput.isNotEmpty()) {
                pinInput = pinInput.dropLast(1)
            }
            return
        }

        if (pinInput.length < 4) {
            val newPin = pinInput + key
            pinInput = newPin

            if (newPin.length == 4) {
                val targetPin = settingsState?.appLockPin
                if (SecurityUtil.verifyPin(newPin, targetPin)) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUnlocked()
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    errorMessage = "Incorrect PIN. Try again."
                    pinInput = ""
                }
            }
        }
    }

    LaunchedEffect(settingsState) {
        visible = true
        if (settingsState?.useBiometrics == true) {
            triggerBiometricAuth()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackgroundGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                    slideInVertically(initialOffsetY = { 30 })
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Animated Glowing Lock
                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = ""
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color(0x1A6366F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Vault Locked",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "Enter 4-digit PIN to access application",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(20.dp))

                // 4-Dot visual PIN indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) Color(0xFF6366F1)
                                    else Color(0xFF475569)
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        errorMessage!!,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                // 3x4 Numeric Keypad
                LockPinPad(
                    onKeyPressed = { handlePinKey(it) },
                    showBiometricButton = settingsState?.useBiometrics == true,
                    onBiometricClick = { triggerBiometricAuth() }
                )
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

@Composable
private fun LockPinPad(
    onKeyPressed: (String) -> Unit,
    showBiometricButton: Boolean,
    onBiometricClick: () -> Unit
) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "BIO", "0", "⌫")
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        keys.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    if (key == "BIO") {
                        if (showBiometricButton) {
                            IconButton(
                                onClick = onBiometricClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x336366F1))
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = Color.White)
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155))
                                .clickable { onKeyPressed(key) }
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = Color.White
                                )
                            } else {
                                Text(
                                    key,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

