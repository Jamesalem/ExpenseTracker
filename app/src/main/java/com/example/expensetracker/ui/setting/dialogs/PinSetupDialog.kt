// ui/setting/dialogs/PinSetupDialog.kt
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R

@Composable
fun PinSetupDialog(
    onPinSetupComplete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Pre‑fetch strings
    val enterPinText    = stringResource(R.string.enter_pin)
    val confirmPinText  = stringResource(R.string.confirm_pin)
    val enter4PinPrompt = stringResource(R.string.enter_4_digit_pin)
    val reenterPrompt   = stringResource(R.string.reenter_pin_confirmation)
    val pinMismatchMsg  = stringResource(R.string.pin_mismatch)
    val cancelText      = stringResource(R.string.cancel)
    val confirmText     = stringResource(R.string.confirm)

    var pin     by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var step    by remember { mutableIntStateOf(1) }
    var error   by remember { mutableStateOf<String?>(null) }

    fun processInput(input: String) {
        val newVal = input.take(4).filter(Char::isDigit)
        if (step == 1) pin = newVal else confirm = newVal
        error = null
        if (newVal.length == 4) {
            if (step == 1) {
                step = 2
            } else if (pin == confirm) {
                onPinSetupComplete(pin)
                onDismiss()
            } else {
                error = pinMismatchMsg
                confirm = ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = {
            Text(if (step == 1) enterPinText else confirmPinText)
        },
        text    = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (step == 1) enter4PinPrompt else reenterPrompt,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value                = if (step == 1) pin else confirm,
                    onValueChange        = { processInput(it) },
                    isError              = error != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    modifier             = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(16.dp))
                PinPad(onDigitPressed = { processInput(it) })
            }
        },
        confirmButton = {
            if (step == 2) {
                Button(
                    onClick = {
                        if (pin == confirm) {
                            onPinSetupComplete(pin)
                            onDismiss()
                        } else {
                            error = pinMismatchMsg
                            confirm = ""
                        }
                    },
                    enabled = confirm.length == 4
                ) {
                    Text(confirmText)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText)
            }
        }
    )
}

@Composable
private fun PinPad(onDigitPressed: (String) -> Unit) {
    val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier               = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(Modifier.size(64.dp))
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onDigitPressed(key) }
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = stringResource(R.string.backspace),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(key, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
