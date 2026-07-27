package com.example.expensetracker.ui.setting.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R

@Composable
fun PinSetupDialog(
    onPinSetupComplete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Enter PIN, 2: Confirm PIN
    var firstPin by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun handleKeyPress(key: String) {
        error = null
        if (key == "⌫") {
            if (currentInput.isNotEmpty()) {
                currentInput = currentInput.dropLast(1)
            }
            return
        }

        if (currentInput.length < 4) {
            val newPin = currentInput + key
            currentInput = newPin

            if (newPin.length == 4) {
                if (step == 1) {
                    firstPin = newPin
                    currentInput = ""
                    step = 2
                } else {
                    if (newPin == firstPin) {
                        onPinSetupComplete(newPin)
                        onDismiss()
                    } else {
                        error = "PIN mismatch! Please try confirming again."
                        currentInput = ""
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (step == 1) "Create Security PIN" else "Confirm Security PIN",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (step == 1) "Enter a 4-digit PIN for App Lock" else "Re-enter your 4-digit PIN to confirm",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(20.dp))

                // 4-Dot visual PIN indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < currentInput.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(24.dp))

                // 3x4 Numeric PinPad
                PinPad(onKeyPressed = { handleKeyPress(it) })
            }
        },
        confirmButton = {
            if (step == 2 && currentInput.length == 4) {
                Button(onClick = {
                    if (currentInput == firstPin) {
                        onPinSetupComplete(currentInput)
                        onDismiss()
                    } else {
                        error = "PIN mismatch!"
                        currentInput = ""
                    }
                }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun PinPad(onKeyPressed: (String) -> Unit) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onKeyPressed(key) }
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = stringResource(R.string.backspace),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    key,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
