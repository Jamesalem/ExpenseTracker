package com.example.expensetracker.ui.security

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.expensetracker.data.util.BiometricPromptManager

@Composable
fun LockScreen(
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val activity = context as? FragmentActivity
    var visible by remember { mutableStateOf(false) }

    val darkBackgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
    }

    fun triggerAuth() {
        activity?.let { act ->
            val manager = BiometricPromptManager(act)
            manager.showBiometricPrompt(
                title = "Unlock Expense Tracker",
                subtitle = "Authenticate to continue",
                onSuccess = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUnlocked() 
                },
                onError = { /* Fail silently or show hint */ }
            )
        }
    }

    LaunchedEffect(Unit) {
        visible = true
        triggerAuth()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackgroundGradient)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                    slideInVertically(initialOffsetY = { 30 })
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Glowing Lock
                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = ""
                )

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color(0x1A6366F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Vault Locked",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Unlock to access your secure financial logs and private data.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        triggerAuth() 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "UNLOCK APPLICATION",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}
