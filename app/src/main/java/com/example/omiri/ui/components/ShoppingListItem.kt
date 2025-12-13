package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.BreakfastDining
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.ShoppingItem
import com.example.omiri.ui.theme.Spacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onToggleDone: () -> Unit,
    onToggleSelection: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) com.example.omiri.ui.theme.AppColors.InfoSoft else Color.White
    val borderColor = if (isSelected) com.example.omiri.ui.theme.AppColors.Info else Color(0xFFE5E7EB)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), // Light gray border
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    if (inSelectionMode) {
                        onToggleSelection()
                    } else {
                        onToggleDone() 
                    }
                },
                onLongClick = { onToggleSelection() }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) 
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Checkbox (Left)
            // If in selection mode, maybe we want to hide checkbox or keep it? 
            // Keeping it is fine, but usually selection replaces check. 
            // For now, keeping it simple as per request to just "remove delete icon... select multiple items".
            Box(
                modifier = Modifier.padding(end = 12.dp)
            ) {
                androidx.compose.material3.Checkbox(
                    checked = item.isDone,
                    onCheckedChange = { 
                        if (inSelectionMode) onToggleSelection() else onToggleDone() 
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = com.example.omiri.ui.theme.AppColors.BrandOrange, // Orange Branding
                        uncheckedColor = Color(0xFFD1D5DB), // Gray outline
                        checkmarkColor = Color.White
                    )
                )
            }

            // 2. Info (Middle)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (item.isDone) Color(0xFF9CA3AF) else Color(0xFF111827),
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else null
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Category
                Text(
                    text = item.category.getName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Badges Row (Under Category)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    
                    // Price / Deal Badge
                    if (item.discountPrice != null || item.price != null) {
                         if (item.discountPrice != null && (item.price != null && item.discountPrice < item.price)) {
                             // Discounted State
                             Surface(
                                 color = Color(0xFFFEF2F2), // Red 50
                                 shape = RoundedCornerShape(4.dp),
                                 border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                             ) {
                                 Row(
                                     modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                     verticalAlignment = Alignment.CenterVertically,
                                     horizontalArrangement = Arrangement.spacedBy(4.dp)
                                 ) {
                                         Surface(
                                             color = Color(0xFFFFE5DB), // Light Orange
                                             shape = CircleShape
                                         ) {
                                             Text(
                                                 text = "${item.discountPercentage}%",
                                                 style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                 color = Color(0xFFFE8357),
                                                 modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                             )
                                         }
                                     Text(
                                         text = "€${String.format("%.2f", item.discountPrice)}",
                                         style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                         color = Color(0xFFDC2626)
                                     )
                                 }
                             }
                         } else {
                             // Regular Price Badge
                             item.price?.let {
                                 Surface(
                                     color = Color(0xFFF3F4F6),
                                     shape = RoundedCornerShape(4.dp)
                                 ) {
                                     Text(
                                         text = "€${String.format("%.2f", it)}",
                                         style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                         color = Color(0xFF374151),
                                         modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                     )
                                 }
                             }
                         }
                    } else if (item.isInDeals) {
                        Surface(
                            color = Color(0xFFF3E8FF), // Light Purple
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Matched Deal",
                                color = Color(0xFFA12AF9), // Purple
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.isRecurring) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Recurring",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            // Removed delete button
        }
    }
}
