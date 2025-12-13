package com.example.omiri.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.models.PersonalizationCategory
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InterestsContent(
    categories: List<PersonalizationCategory>,
    selectedOptions: Map<String, Set<String>>,
    onOptionToggle: (String, String, Boolean) -> Unit,
    showHeader: Boolean = false // Default to false for Settings
) {
    val shoppingGoals = categories.find { it.key == "shopping_goals" }
    val shoppingMode = categories.find { it.key == "shopping_mode" }
    val dietary = categories.find { it.key == "dietary_style" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (showHeader) {
            // Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), 
                border = BorderStroke(1.dp, Color(0xFFFE8357))
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFE8357),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Personalized for you",
                            color = Color(0xFFFE8357),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(Spacing.xl))
                    Text(
                        text = "Let's find your best deals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Pick what matters most — we'll tailor prices, stores, and alerts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4B5563)
                    )
                }
            }
            Spacer(Modifier.height(Spacing.xl))
        }

        // Shopping Goals
        shoppingGoals?.let { category ->
            SettingsGroup(title = "What are you shopping for?") {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                     if (showHeader) {
                         Text("Choose up to 3 -- we'll optimize your feed.", style = MaterialTheme.typography.bodyMedium, color = AppColors.MutedText)
                         Spacer(Modifier.height(Spacing.xl))
                     }
                     FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        category.options.forEach { option ->
                            val isSelected = selectedOptions[category.key]?.contains(option) == true
                            FilterChip(
                                selected = isSelected,
                                onClick = { onOptionToggle(category.key, option, !isSelected) },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFE8357),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                 }
            }
        }
        
        Spacer(Modifier.height(Spacing.xl))
        
        // Shopping Mode
        shoppingMode?.let { category ->
            SettingsGroup(title = "How do you shop?") {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                     if (showHeader) {
                         Text("So we can show the right deals at the right time.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                         Spacer(Modifier.height(Spacing.xl))
                     }
                     
                     // Primary Modes
                    val mainModes = listOf("In-store", "Pickup", "Delivery")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        mainModes.forEach { mode ->
                             val isSelected = selectedOptions[category.key]?.contains(mode) == true
                             val isAny = selectedOptions[category.key]?.contains("Any") == true
                             val visuallySelected = isSelected || (mode == "In-store" && isAny)
                             
                             Box(modifier = Modifier.weight(1f)) {
                                 FilterChip(
                                    selected = visuallySelected,
                                    onClick = { onOptionToggle(category.key, mode, !visuallySelected) },
                                    label = { Text(mode, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFE8357),
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                             }
                        }
                    }
                    
                    if (selectedOptions[category.key]?.contains("Any") == true) {
                         // Logic handled in display
                    }

                    Spacer(Modifier.height(Spacing.xl))
                    
                    FilterChip(
                        selected = selectedOptions[category.key]?.contains("Any") == true,
                        onClick = { onOptionToggle(category.key, "Any", !(selectedOptions[category.key]?.contains("Any") ?: false)) },
                        label = { Text("Any", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        shape = RoundedCornerShape(16.dp),
                         colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFE8357),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                 }

                 HorizontalDivider(color = Color(0xFFE5E7EB))

                 // Toggles for specialized options using SettingsItem style
                 val switchOptions = listOf(
                     Triple("Willing to switch stores", Icons.Outlined.Storefront, "For better deals"), 
                     Triple("Prefer one main store", Icons.Outlined.Star, "Prioritize convenience")
                 )
                 
                 switchOptions.forEachIndexed { index, (opt, icon, subtitle) ->
                     val isChecked = selectedOptions[category.key]?.contains(opt) == true
                     com.example.omiri.ui.components.SettingsItem(
                         icon = icon,
                         iconColor = if(index == 0) Color(0xFFE0E7FF) else Color(0xFFFEF3C7),
                         iconTint = if(index == 0) Color(0xFF4338CA) else Color(0xFFD97706),
                         title = opt,
                         subtitle = subtitle, // Add subtitle for context
                         trailingContent = {
                             Switch(
                                 checked = isChecked,
                                 onCheckedChange = { onOptionToggle(category.key, opt, it) },
                                 colors = SwitchDefaults.colors(
                                     checkedThumbColor = Color.White,
                                     checkedTrackColor = Color(0xFFFE8357),
                                     uncheckedThumbColor = Color.White,
                                     uncheckedTrackColor = Color(0xFFD1D5DB)
                                 )
                             )
                         }
                     )
                     if (index < switchOptions.lastIndex) {
                         HorizontalDivider(color = Color(0xFFE5E7EB))
                     }
                 }
            }
        }
        
        Spacer(Modifier.height(Spacing.xl))
        
        // Dietary Preferences
        dietary?.let { category ->
            SettingsGroup(title = "Food preferences (optional)") {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                     if (showHeader) {
                         Text("Helps recipes + grocery matches.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                         Spacer(Modifier.height(Spacing.xl))
                     }
                     FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        category.options.forEach { option ->
                            val isSelected = selectedOptions[category.key]?.contains(option) == true
                            FilterChip(
                                selected = isSelected,
                                onClick = { onOptionToggle(category.key, option, !isSelected) },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFE8357),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                 }
            }
        }

        Spacer(Modifier.height(Spacing.xxl))
    }
}
