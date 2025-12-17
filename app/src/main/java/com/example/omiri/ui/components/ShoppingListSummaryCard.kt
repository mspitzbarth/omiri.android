package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

@Composable
fun ShoppingListSummaryCard(
    itemCount: Int,
    storeCount: Int,
    matchedDealsCount: Int,
    totalSavings: Double,
    bestTime: String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (totalSavings > 0) Color(0xFFF1F8E9) else AppColors.Surface
    val borderColor = if (totalSavings > 0) Color(0xFFC5E1A5) else AppColors.Neutral200

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top Row: counts and savings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Items Count
                Column {
                    Text(
                        text = "$itemCount items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Neutral900
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$storeCount stores • $matchedDealsCount matched deals",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Neutral600
                    )
                }
                
                // Right: Savings - Only show if > 0
                if (totalSavings > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "€${String.format("%.2f", totalSavings)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF388E3C) // Stronger Green
                        )
                        Text(
                            text = "Total savings",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Neutral600
                        )
                    }
                }
            }
        }
    }
}
