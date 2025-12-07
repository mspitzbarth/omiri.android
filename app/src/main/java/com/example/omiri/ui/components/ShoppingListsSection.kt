package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun ShoppingListsSection(
    onViewAll: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
    ) {
        SectionHeader(
            title = "Shopping Lists",
            actionText = "View All",
            onActionClick = onViewAll
        )
        Spacer(Modifier.height(Spacing.sm))

        ShoppingListItemRow(
            title = "Weekly Groceries",
            subtitle = "9 items",
            pillText = "3 matched deals",
            pillColor = Color(0xFFFFF7ED), // Orange bg
            pillTextColor = Color(0xFFC2410C) // Dark Orange text
        )
        Spacer(Modifier.height(Spacing.sm))
        ShoppingListItemRow(
            title = "Meal Prep",
            subtitle = "6 items",
            pillText = "1 matched deal",
            pillColor = Color(0xFFECFDF5), // Green bg
            pillTextColor = Color(0xFF047857) // Dark Green text
        )
    }
}

@Composable
private fun ShoppingListItemRow(
    title: String,
    subtitle: String,
    pillText: String,
    pillColor: Color,
    pillTextColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = pillColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = pillText,
                            style = MaterialTheme.typography.labelSmall,
                            color = pillTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF)
            )
        }
    }
}
