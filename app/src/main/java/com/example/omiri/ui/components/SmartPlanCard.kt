package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

    Column {
        SmartPlanningHeader(modifier = Modifier.padding(bottom = Spacing.md))
        
        OmiriSummaryCard(
            title = "Weekly Summary",
            headerEndContent = {
                Text(
                    text = "Jan 15 - 21", // Dynamic date or generic
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            },
            modifier = Modifier.padding(horizontal = Spacing.lg),
            expandedContent = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    plan.steps.forEach { step ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Store Header + Savings
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Storefront, // Requires import? Or use Map/ShoppingBag
                                            contentDescription = null,
                                            tint = Color(0xFF1E40AF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = step.storeName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111827)
                                        )
                                    }
                                    
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Save €${String.format("%.2f", step.stepSavings)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF15803D), // Green
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF3F4F6))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Items List
                                step.items.forEach { itemName ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "•",
                                            color = Color(0xFF9CA3AF),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = itemName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF4B5563)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) {
        // Summary Stats (fake for now based on steps, or aggregate)
        val distinctStores = plan.steps.map { it.storeName }.distinct().count()
        val totalSavings = plan.steps.sumOf { it.stepSavings }
        val totalItems = plan.steps.sumOf { it.itemsCount }

        SummaryRow("Items to buy", totalItems.toString())
        SummaryRow("Stores to visit", distinctStores.toString())
        
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFFF3F4F6))
        Spacer(modifier = Modifier.height(12.dp))
        
        SummaryRow(
            label = "Estimated Savings", 
            value = "€${String.format("%.2f", totalSavings)}",
            valueColor = Color(0xFF16A34A), // Green
            isTotal = true
        )
        
        // Optional: List steps below or just keep it high level summary as per design?
        // Reference showed "Weekly Summary" -> Items, Money, Stores. Matches what I just did.
        // I will stick to this summary view. If detailed steps are needed, maybe an expand?
        // User asked "maybe make smartplan and smartalert like this", sharing the summary card.
        // So implementing the summary card logic is the correct interpretation.
    }
    }
}

// End of file
