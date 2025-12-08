package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.data.api.models.StoreListResponse
import com.example.omiri.ui.components.StoreLocationModal
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.MyStoresViewModel
import com.example.omiri.ui.components.*
import com.example.omiri.ui.components.ScreenHeader

import com.example.omiri.ui.components.simpleVerticalScrollbar
import androidx.compose.foundation.lazy.rememberLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoresScreen(
    onBackClick: () -> Unit = {},
    viewModel: MyStoresViewModel = viewModel()
) {
    // Collect state from ViewModel
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val availableStores by viewModel.availableStores.collectAsState()
    val selectedStores by viewModel.selectedStores.collectAsState()  // Observe for reactive updates
    val storeLocations by viewModel.storeLocations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Location modal state
    val locationModalStore by viewModel.locationModalStore.collectAsState()
    val availableLocations by viewModel.availableLocations.collectAsState()
    val isLoadingLocations by viewModel.isLoadingLocations.collectAsState()
    
    // Show country picker dialog
    var showCountryPicker by remember { mutableStateOf(false) }
    var selectedZipcodes by remember { mutableStateOf(setOf<String>()) }
    
    // Load stores when country changes
    LaunchedEffect(selectedCountry) {
        viewModel.loadStores()
    }
    
    // Update selected zipcodes when modal opens
    LaunchedEffect(locationModalStore) {
        locationModalStore?.let { store ->
            selectedZipcodes = storeLocations[store.id] ?: emptySet()
        }
    }
    
    val totalSelectedCount = viewModel.getTotalSelectedCount()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFBFC))
    ) {
        // Header
        ScreenHeader(
            title = "My Stores",
            onBackClick = onBackClick
        )
        
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .simpleVerticalScrollbar(listState),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Country Selector
            item {
                CountrySelector(
                    selectedCountry = selectedCountry,
                    onClick = { showCountryPicker = true }
                )
            }
            
            // Filter by Location Button
            item {
                FilterByLocationButton(
                    onClick = { /* TODO: Implement location filter */ }
                )
            }
            
            // Info Banner
            item {
                InfoBanner(
                    selectedCount = totalSelectedCount,
                    maxCount = 5
                )
            }
            
            // Popular Categories
            item {
                Text(
                    text = "Popular Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
            
            item {
                PopularCategories()
            }
            
            // Available Stores Header
            item {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "Available Stores",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
            
            // Error message
            error?.let { errorMsg ->
                item {
                    ErrorBanner(
                        message = errorMsg,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
            
            // Loading state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFEA580B)
                        )
                    }
                }
            }
            
            // Store list
            items(availableStores) { store ->
                StoreItem(
                    store = store,
                    isSelected = selectedStores.contains(store.id),  // Use state directly for reactivity
                    selectedLocationCount = storeLocations[store.id]?.size ?: 0,
                    onToggle = {
                        if (store.hasMultipleLocations) {
                            viewModel.openLocationModal(store)
                        } else {
                            viewModel.toggleStore(store.id, store.hasMultipleLocations)
                        }
                    }
                )
            }
            
            // Empty state
            if (!isLoading && availableStores.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Store,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "No stores available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Country Picker Dialog
    if (showCountryPicker) {
        CountryPickerDialog(
            selectedCountry = selectedCountry,
            onCountrySelected = { country ->
                viewModel.selectCountry(country)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }
    
    // Location Modal
    locationModalStore?.let { store ->
        StoreLocationModal(
            storeName = store.retailer,
            locations = availableLocations,
            selectedZipcodes = selectedZipcodes,
            isLoading = isLoadingLocations,
            onLocationToggle = { zipcode ->
                selectedZipcodes = if (selectedZipcodes.contains(zipcode)) {
                    selectedZipcodes - zipcode
                } else {
                    // Allow up to 5 zipcodes per store
                    if (selectedZipcodes.size < 5) {
                        selectedZipcodes + zipcode
                    } else {
                        selectedZipcodes  // Don't add if already at 5 for this store
                    }
                }
            },
            onSave = {
                viewModel.saveStoreLocations(store.id, selectedZipcodes)
            },
            onDismiss = {
                viewModel.closeLocationModal()
            }
        )
    }
}

@Composable
private fun PopularCategories() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        CategoryChip(
            icon = Icons.Outlined.Computer,
            label = "Electronics",
            modifier = Modifier.weight(1f)
        )
        CategoryChip(
            icon = Icons.Outlined.ShoppingBag,
            label = "Fashion",
            modifier = Modifier.weight(1f)
        )
        CategoryChip(
            icon = Icons.Outlined.Restaurant,
            label = "Food",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CategoryChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEE2E2)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular icon background
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color(0xFFDC2626),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF991B1B),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF991B1B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
