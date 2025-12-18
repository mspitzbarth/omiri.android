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
                colors = CardDefaults.cardColors(containerColor = AppColors.BrandOrangeSoft), 
                border = BorderStroke(1.dp, AppColors.BrandOrange)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = AppColors.BrandOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Personalized for you",
                            color = AppColors.BrandOrange,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(Spacing.xl))
                    Text(
                        text = "Let's find your best deals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Neutral900
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Pick what matters most — we'll tailor prices, stores, and alerts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Neutral600
                    )
                }
            }
            Spacer(Modifier.height(Spacing.xl))
        }

        // Shopping Goals
        shoppingGoals?.let { category ->
            SettingsGroup(title = "SHOPPING GOALS") {
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
                                    selectedContainerColor = AppColors.BrandOrange,
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
            SettingsGroup(title = "SHOPPING PREFERENCES") {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                     if (showHeader) {
                         Text("So we can show the right deals at the right time.", style = MaterialTheme.typography.bodyMedium, color = AppColors.MutedText)
                         Spacer(Modifier.height(Spacing.xl))
                     }
                     
                     // Primary Modes
                    val mainModes = listOf("In-store", "Pickup", "Delivery")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        mainModes.forEach { mode ->
                             val isSelected = selectedOptions[category.key]?.contains(mode) == true
                             val isAny = selectedOptions[category.key]?.contains("Any") == true
                             val visuallySelected = isSelected || isAny
                             
                             Box(modifier = Modifier.weight(1f)) {
                                 FilterChip(
                                    selected = visuallySelected,
                                    onClick = { onOptionToggle(category.key, mode, !visuallySelected) },
                                    label = { Text(mode, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppColors.BrandOrange,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                             }
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))
                    
                    FilterChip(
                        selected = selectedOptions[category.key]?.contains("Any") == true,
                        onClick = { onOptionToggle(category.key, "Any", !(selectedOptions[category.key]?.contains("Any") ?: false)) },
                        label = { Text("Search all (In-store, Pickup, Delivery)", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        shape = RoundedCornerShape(16.dp),
                         colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.BrandOrange,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                 }

                 HorizontalDivider(color = AppColors.Neutral100)

                 // Toggles for specialized options using SettingsItem style
                 val switchOptions = listOf(
                     Triple("Willing to switch stores", Icons.Outlined.Storefront, "For better deals"), 
                     Triple("Prefer one main store", Icons.Outlined.Star, "Prioritize convenience")
                 )
                 
                 switchOptions.forEachIndexed { index, (opt, icon, subtitle) ->
                     val isChecked = selectedOptions[category.key]?.contains(opt) == true
                     com.example.omiri.ui.components.SettingsItem(
                         icon = icon,
                         iconColor = if(index == 0) AppColors.PastelIndigo else AppColors.PastelYellow,
                         iconTint = if(index == 0) AppColors.PastelIndigo else AppColors.PastelYellowText,
                         title = opt,
                         subtitle = subtitle,
                         trailingContent = {
                             Switch(
                                 checked = isChecked,
                                 onCheckedChange = { onOptionToggle(category.key, opt, it) },
                                 colors = SwitchDefaults.colors(
                                     checkedThumbColor = Color.White,
                                     checkedTrackColor = AppColors.BrandOrange,
                                     uncheckedThumbColor = Color.White,
                                     uncheckedTrackColor = AppColors.Neutral300
                                 )
                             )
                         }
                     )
                     if (index < switchOptions.lastIndex) {
                         HorizontalDivider(color = AppColors.Neutral100)
                     }
                 }
            }
        }
        
        Spacer(Modifier.height(Spacing.xl))
        
        // Dietary Preferences
        dietary?.let { category ->
            SettingsGroup(title = "DIETARY PREFERENCES") {
                 Column(modifier = Modifier.padding(Spacing.md)) {
                     if (showHeader) {
                         Text("Helps recipes + grocery matches.", style = MaterialTheme.typography.bodyMedium, color = AppColors.MutedText)
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
                                    selectedContainerColor = AppColors.BrandOrange,
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
