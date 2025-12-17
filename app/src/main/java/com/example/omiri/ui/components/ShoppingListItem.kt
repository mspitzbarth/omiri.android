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
import androidx.compose.material.icons.outlined.Search

import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
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
import com.example.omiri.ui.theme.AppColors
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
    onFindDeals: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Local state for like animation/feedback (real state comes from item usually)
    // For now we assume clicking it toggles it visually so user sees feedback
    // Ideally this state is hoisted
    
    // Determine Store color key (Mock logic or use item data)
    val cardColor = if (isSelected) com.example.omiri.ui.theme.AppColors.InfoSoft else AppColors.Surface
    val borderColor = if (isSelected) com.example.omiri.ui.theme.AppColors.Info else AppColors.Neutral200 // Match other cards

    // Determine Store color key (Mock logic or use item data)
    val storeName = item.store ?: "Unknown"
    val storeColor = when(storeName) {
        "Target" -> Color(0xFFE53935)
        "Walmart" -> Color(0xFF1E88E5)
        "Costco" -> Color(0xFF8E24AA)
        else -> AppColors.Neutral400
    }
    
    val storeLetter = storeName.take(1)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), 
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    if (inSelectionMode) {
                        onToggleSelection()
                    } else {
                        // In the new design, the whole card doesn't necessarily toggle 'done', 
                        // but usually it does or opens details. We'll keep existing behavior for now.
                        // Ideally checking the box is specific, clicking card opens details.
                        // For now we will assume click = toggle done if not in selection mode, same as before?
                        // Or maybe we make only the checkbox toggle done.
                        // Let's stick to: Click -> Toggle Done (User habit), Long Click -> Selection
                        onToggleDone()
                    }
                },
                onLongClick = { onToggleSelection() }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 1. Checkbox
            Box(
                modifier = Modifier.padding(end = 12.dp, top = 2.dp)
            ) {
                // Custom Square Checkbox style if needed, or default
                androidx.compose.material3.Checkbox(
                    checked = item.isDone,
                    onCheckedChange = { 
                       if (inSelectionMode) onToggleSelection() else onToggleDone()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColors.BrandOrange,
                        uncheckedColor = AppColors.Neutral300, 
                        checkmarkColor = AppColors.Surface
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. Main Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Top Row: Title + Savings Amount (Right aligned)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium, // Larger font
                        fontWeight = FontWeight.Bold,
                        color = if (item.isDone) AppColors.Neutral400 else AppColors.Neutral900,
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    
                    // Savings (Green text on right)
                    if (item.discountPrice != null && item.price != null && item.discountPrice < item.price) {
                         val savings = item.price - item.discountPrice
                         Text(
                             text = "-€${String.format("%.2f", savings)}",
                             style = MaterialTheme.typography.bodyMedium,
                             fontWeight = FontWeight.Bold,
                             color = Color(0xFF2E7D32) // Green
                         )
                    } else if (item.isInDeals && !item.isDone) {
                        // Maybe show "No deal" or nothing?
                    } else {
                         Text(
                             text = "No deal",
                             style = MaterialTheme.typography.bodySmall,
                             color = AppColors.Neutral400
                         )
                    }

                }
                
                Spacer(Modifier.height(4.dp))
                
                // Row: Store Brand + Price + Original Price OR "Not matched" + Search
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (item.store != null) {
                        // Store Icon (Circle with Letter)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(20.dp)
                                .background(storeColor, CircleShape)
                        ) {
                            Text(
                                text = storeLetter,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        
                        Spacer(Modifier.width(6.dp))
                        
                        // Store Name
                        Text(
                            text = item.store ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Neutral700
                        )
                        
                        Spacer(Modifier.width(6.dp))
                        
                        // Bullet
                        Text(
                            text = "•",
                            color = AppColors.Neutral400,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(Modifier.width(6.dp))
                        
                        // Price Logic
                        if (item.discountPrice != null) {
                            // Discounted Price (Orange)
                            Text(
                                text = "€${String.format("%.2f", item.discountPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandOrange
                            )
                            
                            // Original Price (Strikethrough)
                            if (item.price != null && item.price > item.discountPrice) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "€${String.format("%.2f", item.price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = AppColors.Neutral400
                                )
                            }
                        } else if (item.price != null) {
                             // Regular Price (Black)
                             Text(
                                text = "€${String.format("%.2f", item.price)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Neutral900
                            )
                        }
                    } else {
                        // Not matched -> Stacked "Not matched" and "Find deals"
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Not matched",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Neutral400
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onFindDeals() }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = AppColors.BrandOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                   text = "Find deals",
                                   style = MaterialTheme.typography.bodyMedium,
                                   color = AppColors.BrandOrange,
                                   fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Badges Row
                Row(
                   horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Member Deal / Matched Tag
                    if (item.dealId != null) {
                        Surface(
                            color = AppColors.BrandOrangeSoft,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Deal matched",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }


                    // Percentage Off
                    if (item.discountPercentage != null && item.discountPercentage > 0) {
                         Surface(
                            color = AppColors.BrandOrangeSoft, 
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${item.discountPercentage}% OFF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    // Coupon
                    if (item.id.hashCode() % 4 == 0) {
                          Surface(
                            color = AppColors.Blue50, 
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Coupon",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Blue600,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
