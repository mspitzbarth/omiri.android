package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.data.api.models.StoreListResponse
import com.example.omiri.ui.theme.Spacing

@Composable
fun CountrySelector(
    selectedCountry: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7ED)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Text(
                text = "Country",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(Spacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getCountryFlag(selectedCountry),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = getCountryName(selectedCountry),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Select country",
                    tint = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun FilterByLocationButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFEA580B),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Filter by Location",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Search by zipcode, city, or store name",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Filter",
                tint = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun InfoBanner(selectedCount: Int, maxCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBEB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "You can select up to $maxCount stores for deal notifications",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "Selected Stores",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF92400E),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = Color(0xFFEA580B)
            ) {
                Text(
                    text = "$selectedCount/$maxCount",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StoreItem(
    store: StoreListResponse,
    isSelected: Boolean,
    selectedLocationCount: Int,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEA580B))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Store icon placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = getStoreIconColor(store.retailer),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Store,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Text(
                        text = store.retailer,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (store.hasMultipleLocations) {
                        Text(
                            text = "${store.storeCount} locations available",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    if (selectedLocationCount > 0) {
                        Text(
                            text = "$selectedLocationCount location${if (selectedLocationCount > 1) "s" else ""} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEA580B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFEA580B),
                    uncheckedColor = Color(0xFFD1D5DB)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerDialog(
    selectedCountry: String,
    onCountrySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val countries = listOf(
        "US" to "United States",
        "DE" to "Germany",
        "GB" to "United Kingdom",
        "FR" to "France",
        "CA" to "Canada"
    )
    
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg)
            ) {
                Text(
                    text = "Select Country",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                
                Spacer(Modifier.height(Spacing.md))
                
                countries.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCountrySelected(code) }
                            .padding(vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getCountryFlag(code),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF111827),
                            modifier = Modifier.weight(1f)
                        )
                        if (code == selectedCountry) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFFEA580B)
                            )
                        }
                    }
                    if (code != countries.last().first) {
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                    }
                }
            }
        }
    }
}

fun getCountryFlag(countryCode: String): String {
    return when (countryCode) {
        "US" -> "🇺🇸"
        "DE" -> "🇩🇪"
        "GB" -> "🇬🇧"
        "FR" -> "🇫🇷"
        "CA" -> "🇨🇦"
        else -> "🌍"
    }
}

fun getCountryName(countryCode: String): String {
    return when (countryCode) {
        "US" -> "United States"
        "DE" -> "Germany"
        "GB" -> "United Kingdom"
        "FR" -> "France"
        "CA" -> "Canada"
        else -> countryCode
    }
}

fun getStoreIconColor(retailer: String): Color {
    val colors = listOf(
        Color(0xFF60A5FA), // Blue
        Color(0xFF34D399), // Green
        Color(0xFFF59E0B), // Orange
        Color(0xFFEC4899), // Pink
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEF4444)  // Red
    )
    return colors[retailer.hashCode().mod(colors.size)]
}
