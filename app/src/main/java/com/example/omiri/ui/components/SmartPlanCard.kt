package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Lightbulb
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
            // .animateContentSize(), // Requires animation import or usage
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
                        .background(Color(0xFFEA580B), RoundedCornerShape(8.dp)),
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

            SummaryRow("Items to buy", totalItems.toString())
            SummaryRow("Stores to visit", distinctStores.toString())
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFFED7AA))
            Spacer(Modifier.height(8.dp))
            SummaryRow(
                label = "Estimated Savings",
                value = "€${String.format("%.2f", totalSavings)}",
                valueColor = Color(0xFFEA580B),
                isTotal = true
            )

            Spacer(Modifier.height(16.dp))

            // Button
            Button(
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expanded) "Hide Plan" else "View Smart Plan")
            }

            // Expanded Content (Steps)
            if (expanded) {
                Spacer(Modifier.height(16.dp))

                // Recommended Store Run Card
                Card(
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
                                    .background(Color(0xFF2563EB), RoundedCornerShape(12.dp)), // Blue Icon Box
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Map, null, tint = Color.White)
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(Modifier.weight(1f))

                                // Item Count
                                Text(
                                    text = "${step.itemsCount} items",
                                    style = MaterialTheme.typography.bodyMedium,
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
                                        style = MaterialTheme.typography.labelMedium,
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
        }
    }
}

// End of file
