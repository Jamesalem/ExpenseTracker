package com.example.expensetracker.ui.category

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Category
import com.example.expensetracker.data.viewmodel.CategoryViewModel
import com.example.expensetracker.ui.theme.Dimens
import com.example.expensetracker.ui.theme.Shapes
import com.example.expensetracker.ui.theme.generateCategoryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    onAddCategory: () -> Unit,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = (uiState as? CategoryViewModel.CategoryUiState.Success)?.categories.orEmpty()
    
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var toDelete by remember { mutableStateOf<Category?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    // Group categories by their type
    val incomeCategories = remember(categories) {
        categories.filter { it.type == Category.CategoryType.INCOME || it.type == Category.CategoryType.BOTH }
    }
    val expenseCategories = remember(categories) {
        categories.filter { it.type == Category.CategoryType.EXPENSE || it.type == Category.CategoryType.BOTH }
    }

    LaunchedEffect(viewModel.userMessage) {
        viewModel.userMessage.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddCategory() 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = Shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is CategoryViewModel.CategoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CategoryViewModel.CategoryUiState.Success -> {
                    if (categories.isEmpty()) {
                        EmptyCategoriesView(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            if (incomeCategories.isNotEmpty()) {
                                item {
                                    CategoryHeader("Income Categories")
                                }
                                items(incomeCategories, key = { "inc_${it.id}" }) { category ->
                                    if (category.isCustom) {
                                        SwipeToDeleteCategory(
                                            category = category,
                                            onDelete = { toDelete = category },
                                            onEdit = { editingCategory = it }
                                        )
                                    } else {
                                        CategoryItem(category = category, onEdit = { editingCategory = it })
                                    }
                                }
                            }
                            
                            if (expenseCategories.isNotEmpty()) {
                                item {
                                    Spacer(Modifier.height(16.dp))
                                    CategoryHeader("Expense Categories")
                                }
                                items(expenseCategories, key = { "exp_${it.id}" }) { category ->
                                    if (category.isCustom) {
                                        SwipeToDeleteCategory(
                                            category = category,
                                            onDelete = { toDelete = category },
                                            onEdit = { editingCategory = it }
                                        )
                                    } else {
                                        CategoryItem(category = category, onEdit = { editingCategory = it })
                                    }
                                }
                            }
                        }
                    }
                }
                is CategoryViewModel.CategoryUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        editingCategory?.let { category ->
            EditCategoryDialog(
                category = category,
                onDismiss = { editingCategory = null },
                onSave = { newName, newType ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.updateCategory(category.copy(name = newName, type = newType))
                    editingCategory = null
                }
            )
        }

        toDelete?.let { category ->
            DeleteCategoryDialog(
                onConfirm = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.deleteCategory(category)
                    toDelete = null
                },
                onDismiss = { toDelete = null }
            )
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteCategory(
    category: Category,
    onDelete: () -> Unit,
    onEdit: (Category) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                false
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) MaterialTheme.colorScheme.error else Color.Transparent, label = ""
            )
            Box(Modifier.fillMaxSize().background(color).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
            }
        },
        content = {
            CategoryItem(category = category, onEdit = onEdit)
        }
    )
}

@Composable
private fun CategoryItem(
    category: Category,
    onEdit: (Category) -> Unit
) {
    val color = remember(category.name) { generateCategoryColor(category.id) }
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category.name, style = MaterialTheme.typography.bodyLarge)
            if (!category.isCustom) {
                Text("Default", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
        IconButton(onClick = { onEdit(category) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun EmptyCategoriesView(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Text("No categories yet", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun DeleteCategoryDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Category") },
        text = { Text("Are you sure? This will remove the category. Transactions already assigned to this category will remain but their category name might be unlinked.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("DELETE", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun EditCategoryDialog(category: Category, onDismiss: () -> Unit, onSave: (String, Category.CategoryType) -> Unit) {
    var newName by remember { mutableStateOf(category.name) }
    var selectedType by remember { mutableStateOf(category.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = Shapes.medium
                )
                
                Text("Category Type", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // filter BOTH out for simple selection
                    listOf(Category.CategoryType.INCOME, Category.CategoryType.EXPENSE).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name, modifier = Modifier.padding(horizontal = 8.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(newName, selectedType) }, enabled = newName.isNotBlank()) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
