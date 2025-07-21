// ui/category/CategoryListScreen.kt
package com.example.expensetracker.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Category
import com.example.expensetracker.data.viewmodel.CategoryViewModel
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.generateCategoryColor
import com.example.expensetracker.ui.theme.secondaryButtonColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    onAddCategory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = (uiState as? CategoryViewModel.CategoryUiState.Success)?.categories.orEmpty()
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is CategoryViewModel.CategoryUiState.Error) {
            snackbarHostState.showSnackbar((uiState as CategoryViewModel.CategoryUiState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.manage_categories)) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCategory,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_category),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState) {
            is CategoryViewModel.CategoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CategoryViewModel.CategoryUiState.Success -> {
                if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_categories_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(Dimens.medium),
                        verticalArrangement = Arrangement.spacedBy(Dimens.small)
                    ) {
                        items(categories) { category ->
                            CategoryItem(
                                category = category,
                                onEdit  = { editingCategory = it },
                                onDelete= { viewModel.deleteCategory(it) }
                            )
                        }
                    }
                }
            }
            is CategoryViewModel.CategoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_categories_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        editingCategory?.let { category ->
            EditCategoryDialog(
                category = category,
                onDismiss = { editingCategory = null },
                onSave    = { newName ->
                    viewModel.updateCategory(category.copy(name = newName))
                    editingCategory = null
                }
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    val color = generateCategoryColor(category.id)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(Dimens.iconS)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(Dimens.medium))
            Text(
                category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = { onEdit(category) }) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_category))
            }
            IconButton(onClick = { onDelete(category) }) {
                Icon(Icons.Filled.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = stringResource(R.string.delete_category))
            }
        }
    }
}

@Composable
fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newName by remember { mutableStateOf(category.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(stringResource(R.string.edit_category)) },
        text    = {
            OutlinedTextField(
                value       = newName,
                onValueChange = { newName = it },
                label       = { Text(stringResource(R.string.category_name)) },
                modifier    = Modifier.fillMaxWidth(),
                singleLine  = true,
                shape       = MaterialTheme.shapes.medium
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(newName) },
                enabled = newName.isNotBlank() && newName != category.name
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = MaterialTheme.secondaryButtonColors()
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
