// ui/components/pickers/FilePicker.kt
package com.example.expensetracker.ui.components.pickers

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun FilePicker(
    onFileSelected: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            ctx.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onFileSelected(uri)
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf("application/json", "text/plain"))
    }
}
