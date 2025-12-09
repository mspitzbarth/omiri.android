package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun SmartPlanCard(
    plan: com.example.omiri.data.api.models.ShoppingListOptimizeResponse? = null
) {
    if (plan == null) return

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(horizontal = Spacing.lg)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFE8357), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Weekly Summary",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Jan 15 - 21",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Summary Stats
            val distinctStores = plan.steps.map { it.storeName }.distinct().count()
            val totalSavings = plan.steps.sumOf { it.stepSavings }
            val totalItems = plan.steps.sumOf { it.itemsCount }

            SmartPlanSummaryRow("Items to buy", totalItems.toString())
            SmartPlanSummaryRow("Stores to visit", distinctStores.toString())
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFFED7AA))
            Spacer(Modifier.height(8.dp))
            SmartPlanSummaryRow(
                label = "Estimated Savings",
                value = "€${String.format("%.2f", totalSavings)}",
                valueColor = Color(0xFFFE8357),
                isTotal = true
            )

            Spacer(Modifier.height(16.dp))

            // Button
            Button(
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expanded) "Hide Plan" else "View Smart Plan")
            }

            // Expanded Content (Steps)
            if (expanded) {
                Spacer(Modifier.height(16.dp))
                RecommendedStoreRunCard(plan = plan)
            }
        }
    }
}

@Composable
fun RecommendedStoreRunCard(
    plan: com.example.omiri.data.api.models.ShoppingListOptimizeResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)), // Light Blue Box
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Map, null, tint = Color(0xFF2563EB)) // Blue Icon
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Recommended Store Run",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            // Store Rows
            plan.steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Store Icon
                    val storeColor = when {
                        step.storeName.contains("Lidl") || step.storeName.contains("Target") -> Color(0xFFEF4444) // Red
                        step.storeName.contains("Aldi") || step.storeName.contains("Walmart") -> Color(0xFF2563EB) // Blue
                        else -> Color(0xFF16A34A) // Green (Kaufland/Other)
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(storeColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Storefront,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Store Name
                    Text(
                        text = step.storeName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.weight(1f))

                    // Item Count
                    Text(
                        text = "${step.itemsCount} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    // Deals Badge
                    Surface(
                        color = Color(0xFFFF5722), // Orange pill
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${step.items.size} deals", // Using items as deals count
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (index < plan.steps.lastIndex) {
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                }
            }
        }
    }
}

@Composable
private fun SmartPlanSummaryRow(label: String, value: String, valueColor: Color = Color.Black, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
