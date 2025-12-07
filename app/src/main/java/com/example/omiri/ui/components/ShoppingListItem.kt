package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.ShoppingItem
import com.example.omiri.ui.theme.Spacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium, // Restoring shape
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color(0xFFF2F3F5),
                shape = MaterialTheme.shapes.medium
            )
            .clickable { onToggleDone() } // Make entire card clickable
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Checkbox
                Checkbox(
                    checked = item.isDone,
                    onCheckedChange = { onToggleDone() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFEA580B),
                        uncheckedColor = Color(0xFF9CA3AF)
                    )
                )

                // Item details
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.isDone) Color(0xFF9CA3AF) else Color(0xFF111827),
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Badge
                        val category = item.category
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = category.color.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = category.getName(),
                                style = MaterialTheme.typography.labelSmall,
                                color = category.color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Deal status badge
                    if (item.isInDeals) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFA12AF9)
                        ) {
                            Text(
                                text = "In deals",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
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
                    
                    // Close the Row containing badges
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete item",
                    tint = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
