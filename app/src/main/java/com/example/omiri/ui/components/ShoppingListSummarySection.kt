package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

@Composable
fun ShoppingListSummarySection(
    totalItems: Int,
    checkedItems: Int,
    estimatedTotal: Double,
    potentialSavings: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ShoppingSummaryRow(label = "Total Items", value = totalItems.toString())
        Spacer(Modifier.height(8.dp))
        ShoppingSummaryRow(label = "Checked", value = checkedItems.toString())
        Spacer(Modifier.height(8.dp))
        ShoppingSummaryRow(
            label = "Estimated Total", 
            value = "€${String.format("%.2f", estimatedTotal)}",
            isBold = true
        )
        
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = AppColors.Neutral100)
        Spacer(Modifier.height(16.dp))
        
        ShoppingSummaryRow(
            label = "Potential Savings", 
            value = "€${String.format("%.2f", potentialSavings)}",
            valueColor = AppColors.Success,
            isBold = true
        )
    }
}

@Composable
private fun ShoppingSummaryRow(
    label: String,
    value: String,
    valueColor: Color = AppColors.Neutral900,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.SubtleText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
