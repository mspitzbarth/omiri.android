package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

data class FilterOptions(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    val selectedStores: Set<String> = emptySet(),
    val selectedCategories: Set<String> = emptySet(),
    val onlineOnly: Boolean = false,
    val hasDiscount: Boolean = false,
    val sortBy: String? = null, // "price", "date"
    val sortOrder: String? = null // "asc", "desc"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onApply: (FilterOptions) -> Unit,
    initialFilters: FilterOptions = FilterOptions(),
    availableCategories: List<String> = emptyList(),
    availableStores: List<com.example.omiri.viewmodels.ProductViewModel.StoreFilterOption> = emptyList()
) {
    var priceRange by remember(isVisible) { mutableStateOf(initialFilters.priceRange) }
    var selectedStores by remember(isVisible) { mutableStateOf(initialFilters.selectedStores) }
    var selectedCategories by remember(isVisible) { mutableStateOf(initialFilters.selectedCategories) }
    var onlineOnly by remember(isVisible) { mutableStateOf(initialFilters.onlineOnly) }
    var hasDiscount by remember(isVisible) { mutableStateOf(initialFilters.hasDiscount) }
    var sortBy by remember(isVisible) { mutableStateOf(initialFilters.sortBy) }
    var sortOrder by remember(isVisible) { mutableStateOf(initialFilters.sortOrder) }

    val stores = availableStores
    val categories = availableCategories

    if (isVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
                
                // Sort By Dropdown
                var expandedSort by remember { mutableStateOf(false) }
                val sortOptions = listOf(
                    Triple("Price: Low to High", "price", "asc"),
                    Triple("Price: High to Low", "price", "desc")
                )
                
                // Helper to get display label
                val currentSortLabel = sortOptions.find { it.second == sortBy && it.third == sortOrder }?.first ?: "Select Sort Order"

                ExposedDropdownMenuBox(
                    expanded = expandedSort,
                    onExpandedChange = { expandedSort = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentSortLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sort By") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSort) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                         colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                            unfocusedBorderColor = com.example.omiri.ui.theme.AppColors.PastelGrey,
                            focusedLabelColor = com.example.omiri.ui.theme.AppColors.BrandOrange
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSort,
                        onDismissRequest = { expandedSort = false }
                    ) {
                        sortOptions.forEach { (label, key, order) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    sortBy = key
                                    sortOrder = order
                                    expandedSort = false
                                }
                            )
                        }
                        // Option to clear
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                sortBy = null
                                sortOrder = null
                                expandedSort = false
                            }
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.lg))

                // Price Range Section
                Text(
                    text = "Price Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "$${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.omiri.ui.theme.AppColors.BrandOrange
                )
                Spacer(Modifier.height(Spacing.xs))
                RangeSlider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    valueRange = 0f..1000f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                        activeTrackColor = com.example.omiri.ui.theme.AppColors.BrandOrange
                    )
                )

                Spacer(Modifier.height(Spacing.lg))

                // Stores Section
                Text(
                    text = "Stores",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(Spacing.sm))
                if (stores.isEmpty()) {
                    Text("No stores selected in Settings.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        stores.forEach { store ->
                            val isSelected = store.id in selectedStores
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedStores = if (isSelected) {
                                        selectedStores - store.id
                                    } else {
                                        selectedStores + store.id
                                    }
                                },
                                label = { Text(store.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = com.example.omiri.ui.theme.AppColors.PastelGrey,
                                    labelColor = com.example.omiri.ui.theme.AppColors.BrandInk
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.lg))

                // Categories Section
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(Spacing.sm))
                if (categories.isEmpty()) {
                    Text("Loading categories...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                     // Multi-select Dropdown
                    var expandedCategories by remember { mutableStateOf(false) }
                    val selectedCount = selectedCategories.size
                    val displayText = if (selectedCount > 0) {
                        if(selectedCount == 1) selectedCategories.first() else "$selectedCount Selected"
                    } else {
                        "Select Categories"
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedCategories,
                        onExpandedChange = { expandedCategories = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = displayText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categories") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategories) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                                unfocusedBorderColor = com.example.omiri.ui.theme.AppColors.PastelGrey,
                                focusedLabelColor = com.example.omiri.ui.theme.AppColors.BrandOrange
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategories,
                            onDismissRequest = { expandedCategories = false },
                            modifier = Modifier.heightIn(max = 300.dp) // Scrollable if many
                        ) {
                            categories.forEach { category ->
                                val isSelected = category in selectedCategories
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null, // Handled by item click
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(text = category)
                                        }
                                    },
                                    onClick = {
                                        selectedCategories = if (isSelected) {
                                            selectedCategories - category
                                        } else {
                                            selectedCategories + category
                                        }
                                        // Keep menu open for multi-select
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
                
                // Discount Only Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Discount Only",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Show only discounted items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasDiscount,
                        onCheckedChange = { hasDiscount = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = com.example.omiri.ui.theme.AppColors.BrandOrange
                        )
                    )
                }
                
                Spacer(Modifier.height(Spacing.md))

                // Online Only Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Online Only",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Show only online deals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = onlineOnly,
                        onCheckedChange = { onlineOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = com.example.omiri.ui.theme.AppColors.BrandOrange
                        )
                    )
                }

                Spacer(Modifier.height(Spacing.xl))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    OutlinedButton(
                        onClick = {
                            priceRange = 0f..1000f
                            selectedStores = emptySet()
                            selectedCategories = emptySet()
                            onlineOnly = false
                            hasDiscount = false
                            sortBy = null
                            sortOrder = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset")
                    }
                    Button(
                        onClick = {
                            onApply(
                                FilterOptions(
                                    priceRange = priceRange,
                                    selectedStores = selectedStores,
                                    selectedCategories = selectedCategories,
                                    onlineOnly = onlineOnly,
                                    hasDiscount = hasDiscount,
                                    sortBy = sortBy,
                                    sortOrder = sortOrder
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.omiri.ui.theme.AppColors.BrandOrange
                        )
                    ) {
                        Text("Apply Filters")
                    }
                }
                
                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }
}
