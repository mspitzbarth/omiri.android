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
import androidx.compose.ui.draw.clip
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
            containerColor = Color(0xFFF3F4F6) // Light gray
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Text(
                text = "COUNTRY / REGION",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Bold
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
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.SemiBold
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
fun StoreSearchBar(
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = if (query.isEmpty()) "Search stores..." else query,
            style = MaterialTheme.typography.bodyLarge,
            color = if (query.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF111827)
        )
    }
}

// Deprecated: Use StoreSearchBar instead
@Composable
fun FilterByLocationButton(onClick: () -> Unit) {
    StoreSearchBar(onClick = onClick)
}

@Composable
fun InfoBanner(selectedCount: Int, maxCount: Int) {
    // Replaced by the header row in the main screen, but keeping for compatibility if needed
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
            Text(
                text = "You can select up to $maxCount stores",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF92400E)
            )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) // Always gray border, no active orange border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 12.dp), // SettingsItem padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox (Left)
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFFE8357), // Orange (Agentic Orange)
                    uncheckedColor = Color(0xFFD1D5DB),
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Store Icon (SettingsItem style: 40dp, rounded 10dp)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getStoreIconColor(store.retailer).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val firstLetter = store.retailer.firstOrNull()?.uppercase() ?: "S"
                Text(
                    text = firstLetter,
                    style = MaterialTheme.typography.titleMedium, // Adjusted for smaller box
                    color = getStoreIconColor(store.retailer),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = store.retailer,
                    style = MaterialTheme.typography.bodyLarge, // SettingsItem style
                    fontWeight = FontWeight.Medium, // SettingsItem style
                    color = Color(0xFF111827)
                )
                
                // Location Stats (Only for multiple locations)
                if (store.hasMultipleLocations) {
                    // No Spacer needed if we want tight packing, or small spacer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        // Total Locations Icon + Text
                        Icon(
                            imageVector = Icons.Outlined.Store,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${store.storeCount} locations",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )

                        // Selected Count
                        if (selectedLocationCount > 0) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$selectedLocationCount selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFE8357),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Chevron (Only if multiple locations to drill down)
            if (store.hasMultipleLocations) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }
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
                                tint = Color(0xFFFE8357)
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
