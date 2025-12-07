package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun SmartBundlesGrid(
    onViewAll: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
    ) {
        SectionHeader(
            title = "Smart Bundles",
            actionText = "View All",
            onActionClick = onViewAll
        )
        Spacer(Modifier.height(Spacing.md))

        // Grid of 2x2 manually for simplicity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            BundleCard(
                title = "Breakfast under €10",
                productCount = "5 products",
                icon = Icons.Default.Coffee,
                iconBgColor = Color(0xFFFEF3C7), // Yellow
                iconTint = Color(0xFFD97706),
                modifier = Modifier.weight(1f)
            )
            BundleCard(
                title = "High-protein week",
                productCount = "8 products",
                icon = Icons.Default.FitnessCenter,
                iconBgColor = Color(0xFFD1FAE5), // Green
                iconTint = Color(0xFF059669),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            BundleCard(
                title = "Budget skincare picks",
                productCount = "4 products",
                icon = Icons.Default.LocalFlorist,
                iconBgColor = Color(0xFFFCE7F3), // Pink
                iconTint = Color(0xFFDB2777),
                modifier = Modifier.weight(1f)
            )
            BundleCard(
                title = "Office lunch kit",
                productCount = "6 products",
                icon = Icons.Default.BusinessCenter,
                iconBgColor = Color(0xFFDBEAFE), // Blue
                iconTint = Color(0xFF2563EB),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BundleCard(
    title: String,
    productCount: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp), // Fixed height for uniformity
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    maxLines = 2
                )
                Text(
                    text = productCount,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}
