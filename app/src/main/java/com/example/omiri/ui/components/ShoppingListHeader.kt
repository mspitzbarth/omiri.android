package com.example.omiri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

@Composable
fun ShoppingListHeader(
    listName: String,
    onClick: () -> Unit,
    isCheckingDeals: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.Surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title Section (Clickable to switch lists)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Neutral900
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Select List",
                        tint = AppColors.BrandOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Refresh / Loading Section
                if (isCheckingDeals) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AppColors.BrandOrange,
                        strokeWidth = 2.dp
                    )
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = AppColors.Neutral200
            )
        }
    }
}
