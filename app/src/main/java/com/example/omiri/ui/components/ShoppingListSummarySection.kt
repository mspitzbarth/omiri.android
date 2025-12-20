package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors

@Composable
fun ShoppingListSummarySection(
    total: Double,
    potentialSavings: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Column {
            HorizontalDivider(thickness = 1.dp, color = AppColors.Neutral200)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Neutral500
                    )
                    Text(
                        text = "€${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Neutral900
                    )
                }

                // Potential Savings
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Potential Savings",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Neutral500
                    )
                    Text(
                        text = "€${String.format("%.2f", potentialSavings)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
            // Spacer for navigation bar if needed, but usually handled by scaffold padding
            Spacer(Modifier.height(8.dp))
        }
    }
}
