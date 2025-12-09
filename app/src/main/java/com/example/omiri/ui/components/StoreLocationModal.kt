package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.LocationDisabled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.data.api.models.StoreResponse
import com.example.omiri.ui.theme.Spacing

import com.example.omiri.ui.components.simpleVerticalScrollbar
import androidx.compose.foundation.lazy.rememberLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreLocationModal(
    storeName: String,
    locations: List<StoreResponse>,
    selectedZipcodes: Set<String>,
    isLoading: Boolean,
    onLocationToggle: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onVoiceSearch: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter locations based on search
    val filteredLocations = remember(locations, searchQuery, selectedZipcodes) {
        val filtered = if (searchQuery.isBlank()) {
            locations
        } else {
            locations.filter { location ->
                location.storeName?.contains(searchQuery, ignoreCase = true) == true ||
                location.address?.contains(searchQuery, ignoreCase = true) == true ||
                location.city?.contains(searchQuery, ignoreCase = true) == true ||
                location.zipcode?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        // Sort to show selected locations first
        filtered.sortedByDescending { location ->
            selectedZipcodes.contains(location.zipcode ?: "")
        }
    }
    
    val selectedCount = selectedZipcodes.size
    val canSave = selectedCount in 0..5  // Allow 0-5 zipcodes per store
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = Spacing.lg)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Locations",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "$selectedCount/5 selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedCount > 5) Color(0xFFEF4444) else Color(0xFF6B7280)
                    )
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF6B7280)
                    )
                }
            }
            
            Spacer(Modifier.height(Spacing.md))
            
            // Search bar with voice
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search locations...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF6B7280)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onVoiceSearch) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Voice search",
                            tint = Color(0xFFFE8357)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFE8357),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color(0xFFFAFBFC),
                    unfocusedContainerColor = Color(0xFFFAFBFC)
                ),
                singleLine = true
            )
            
            Spacer(Modifier.height(Spacing.md))
            
            // Locations list
            val listState = rememberLazyListState()
            
            // Loading state
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFE8357)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .simpleVerticalScrollbar(listState),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(filteredLocations) { location ->
                        LocationItem(
                            location = location,
                            isSelected = location.zipcode?.let { selectedZipcodes.contains(it) } ?: false,
                            onToggle = {
                                location.zipcode?.let { onLocationToggle(it) }
                            }
                        )
                    }
                    
                    if (filteredLocations.isEmpty()) {
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
                                        imageVector = Icons.Outlined.LocationDisabled,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFF9CA3AF)
                                    )
                                    Text(
                                        text = "No locations found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(Spacing.md))
            
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = canSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFE8357),
                        disabledContainerColor = Color(0xFFE5E7EB)
                    )
                ) {
                    Text(
                        text = if (selectedCount > 5) "Max 5 per store" else "Save ($selectedCount/5)",
                        color = if (canSave) Color.White else Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationItem(
    location: StoreResponse,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFF7ED) else Color.White
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFE8357))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                Text(
                    text = location.storeName ?: location.retailer,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
                
                location.address?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    location.city?.let { city ->
                        Text(
                            text = city,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    
                    location.zipcode?.let { zipcode ->
                        Text(
                            text = "• $zipcode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFFE8357),
                    uncheckedColor = Color(0xFFD1D5DB)
                )
            )
        }
    }
}
