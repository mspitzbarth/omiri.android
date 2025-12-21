package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Search


import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
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
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) AppColors.Neutral100 else Color.White
    val borderColor = if (isSelected) AppColors.BrandOrange else AppColors.Neutral200

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    if (inSelectionMode) onToggleSelection() else onToggleDone()
                },
                onLongClick = { onToggleSelection() }
            )
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                OmiriSpinner()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag Handle
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Reorder",
                    tint = AppColors.Neutral300,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(Modifier.width(12.dp))

                // Checkbox
                OmiriCheckbox(
                    checked = item.isDone,
                    onCheckedChange = { if (inSelectionMode) onToggleSelection() else onToggleDone() }
                )

                Spacer(Modifier.width(12.dp))

                // Main Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isDone) AppColors.Neutral400 else AppColors.Neutral900,
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else null
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = com.example.omiri.util.CategoryHelper.getCategoryName(item.categoryId),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.SubtleText
                        )
                        if (item.store != null) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.Neutral300
                            )
                            Text(
                                text = item.store,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.SubtleText
                            )
                        }
                    }
                }

                // Price & Discount
                Column(horizontalAlignment = Alignment.End) {
                    val displayPrice = item.discountPrice ?: item.price
                    if (displayPrice != null) {
                        Text(
                            text = "€${String.format("%.2f", displayPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.discountPrice != null) AppColors.BrandOrange else AppColors.Neutral900
                        )
                    }
                    if (item.discountPercentage != null && item.discountPercentage > 0) {
                        Spacer(Modifier.height(4.dp))
                        OmiriBadge(
                            text = "-${item.discountPercentage}%",
                            color = AppColors.Success,
                            softBackground = true,
                            leadingIcon = Icons.Default.LocalOffer
                        )
                    }
                }
            }
        }
    }
}
