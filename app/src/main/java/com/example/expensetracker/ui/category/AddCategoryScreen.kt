// ui/category/AddCategoryScreen.kt
package com.example.expensetracker.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Category
import com.example.expensetracker.data.viewmodel.CategoryViewModel
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(Category.CategoryType.EXPENSE) }
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(viewModel.userMessage) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
            if (message.contains("successfully")) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Category", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.large),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium)
        ) {
            Text(
                "Create a custom category to better organize your transactions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                placeholder = { Text("e.g., Subscriptions, Hobby") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is CategoryViewModel.CategoryUiState.Loading,
                shape = Shapes.medium
            )

            Text("Category Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Category.CategoryType.values().filter { it != Category.CategoryType.BOTH }.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.name, modifier = Modifier.padding(horizontal = 8.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(Dimens.medium))

            Button(
                onClick = { 
                    if (categoryName.isNotBlank()) {
                        viewModel.addCategory(categoryName, selectedType)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = categoryName.isNotBlank() && uiState !is CategoryViewModel.CategoryUiState.Loading,
                shape = Shapes.medium
            ) {
                if (uiState is CategoryViewModel.CategoryUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("CREATE CATEGORY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
