package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

data class RouteStepUi(
    val index: Int,
    val storeName: String,
    val itemCount: Int,
    val savings: Double,
    val items: List<String>,
    val color: Color
)

@Composable
fun RecommendedRouteCard(
    steps: List<RouteStepUi>,
    onViewMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Neutral200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Timeline, // Or similar route icon
                        contentDescription = null,
                        tint = AppColors.BrandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Recommended Route",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Neutral900
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Steps
            Column {
                steps.forEachIndexed { index, step ->
                    RouteStepItem(
                        step = step,
                        isLast = index == steps.lastIndex
                    )
                }
            }
            
    }
}
}

@Composable
fun RouteStepItem(
    step: RouteStepUi,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min) // Important for line drawing
    ) {
        // Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Circle Number
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .background(step.color, CircleShape)
            ) {
                Text(
                    text = "${step.index}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Vertical Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(AppColors.Neutral200)
                        .padding(vertical = 4.dp)
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Content
        Column(
            modifier = Modifier.padding(bottom = if (isLast) 0.dp else 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = step.storeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Neutral900
                )
            }
            
            Text(
                text = "${step.itemCount} items • Save €${String.format("%.2f", step.savings)}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Neutral700,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            // Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                step.items.take(3).forEach { item ->
                    Surface(
                        color = AppColors.Neutral100,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.Neutral900,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (step.items.size > 3) {
                     Surface(
                        color = AppColors.Neutral100,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "+${step.items.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.Neutral900,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
