package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
    val selectedStoresInitial by viewModel.selectedStores.collectAsState() 
    val storeLocationsInitial by viewModel.storeLocations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Location modal state
    val locationModalStore by viewModel.locationModalStore.collectAsState()
    val availableLocations by viewModel.availableLocations.collectAsState()
    val isLoadingLocations by viewModel.isLoadingLocations.collectAsState()
    
    // Show country picker dialog
    var showCountryPicker by remember { mutableStateOf(false) }

    // Local buffers for edits
    // We use a key to reset buffer if initial changes (e.g. first load)
    // But we want to persist local edits.
    // Initialize buffers once loaded
    var selectedStoresBuffer by remember { mutableStateOf<Set<String>>(emptySet()) }
    var storeLocationsBuffer by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Sync with VM initial state only once
    LaunchedEffect(selectedStoresInitial, storeLocationsInitial) {
        if (!isInitialized && selectedStoresInitial.isNotEmpty()) {
             selectedStoresBuffer = selectedStoresInitial
             storeLocationsBuffer = storeLocationsInitial
             isInitialized = true
        } else if (!isInitialized && selectedStoresInitial.isEmpty() && !isLoading) {
             // also init if empty but loaded
             isInitialized = true
        }
    }
    
    // Load stores when country changes
    LaunchedEffect(selectedCountry) {
        viewModel.loadStores()
    }
    
    /* 
       Zipcode handling for Modal: 
       We use a temp state 'selectedZipcodes' for the modal. 
       When modal opens, init it from buffer.
       When modal saves, update buffer.
    */
    var locationModalZipcodes by remember { mutableStateOf(setOf<String>()) }
    
    // Update selected zipcodes when modal opens
    LaunchedEffect(locationModalStore) {
        locationModalStore?.let { store ->
            locationModalZipcodes = storeLocationsBuffer[store.id] ?: emptySet()
        }
    }
    
    val availableCountries by viewModel.availableCountries.collectAsState()
    
    Scaffold(
        topBar = {
             ScreenHeader(
                title = "My Stores",
                onBackClick = onBackClick,
                action = {
                    TextButton(
                        onClick = {
                            viewModel.saveChanges(selectedStoresBuffer, storeLocationsBuffer)
                            onBackClick()
                        },
                        enabled = selectedStoresBuffer.isNotEmpty(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFE8357),
                            disabledContentColor = Color(0xFFFED7AA)
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .simpleVerticalScrollbar(listState),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Country Selector (Inline)
            item {
                CountrySelectionRow(
                    availableCountries = availableCountries,
                    selectedCountry = selectedCountry,
                    onCountrySelected = { viewModel.selectCountry(it) }
                )
            }
            
            // Selected Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedStoresBuffer.size} stores selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (selectedStoresBuffer.isNotEmpty()) {
                        Text(
                            text = "Clear all",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFE8357),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { 
                                selectedStoresBuffer = emptySet()
                                storeLocationsBuffer = emptyMap()
                            }
                        )
                    }
                }
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
                            color = Color(0xFFFE8357)
                        )
                    }
                }
            }
            
            // Store list
            items(availableStores) { store ->
                val isSelected = selectedStoresBuffer.contains(store.id)
                StoreItem(
                    store = store,
                    isSelected = isSelected,
                    selectedLocationCount = storeLocationsBuffer[store.id]?.size ?: 0,
                    onToggle = {
                        if (store.hasMultipleLocations) {
                            viewModel.openLocationModal(store)
                        } else {
                            // Toggle local buffer
                            if (isSelected) {
                                selectedStoresBuffer = selectedStoresBuffer - store.id
                                storeLocationsBuffer = storeLocationsBuffer - store.id
                            } else {
                                selectedStoresBuffer = selectedStoresBuffer + store.id
                            }
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
    
    // Location Modal
    locationModalStore?.let { store ->
        StoreLocationModal(
            storeName = store.retailer,
            locations = availableLocations,
            selectedZipcodes = locationModalZipcodes,
            isLoading = isLoadingLocations,
            onLocationToggle = { zipcode ->
                locationModalZipcodes = if (locationModalZipcodes.contains(zipcode)) {
                    locationModalZipcodes - zipcode
                } else {
                    if (locationModalZipcodes.size < 5) {
                        locationModalZipcodes + zipcode
                    } else {
                        locationModalZipcodes
                    }
                }
            },
            onSave = {
                // Update buffers
                if (locationModalZipcodes.isEmpty()) {
                    // If no location selected, deselect store
                    selectedStoresBuffer = selectedStoresBuffer - store.id
                    storeLocationsBuffer = storeLocationsBuffer - store.id
                } else {
                    selectedStoresBuffer = selectedStoresBuffer + store.id
                    storeLocationsBuffer = storeLocationsBuffer + (store.id to locationModalZipcodes)
                }
                viewModel.closeLocationModal()
            },
            onDismiss = {
                viewModel.closeLocationModal()
            }
        )
    }
}

@Composable
fun CountrySelectionRow(
// ... (rest same as before)
    availableCountries: List<String>,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    if (availableCountries.isEmpty()) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = "COUNTRY / REGION",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Bold
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(availableCountries) { code ->
                val isSelected = code == selectedCountry
                FilterChip(
                    selected = isSelected,
                    onClick = { onCountrySelected(code) },
                    label = { 
                        Text(
                            text = "${getCountryFlag(code)} $code", // Flag + Code (e.g. 🇺🇸 US)
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFE8357),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color(0xFF111827)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFFFE8357) else Color(0xFFE5E7EB),
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
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
        shape = RoundedCornerShape(12.dp),
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
                    .size(24.dp)
                    .background(
                        color = Color(0xFFDC2626),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF991B1B),
                fontWeight = FontWeight.Medium,
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
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
