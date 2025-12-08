package com.example.omiri.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun SavingsDashboard(
    potentialSavings: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDFA) // Very light teal/green
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCCFBF1))
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.lg)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF0D9488),
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, CircleShape)
                    .padding(6.dp)
            )
            
            Spacer(Modifier.height(Spacing.sm))
            
            Text(
                text = if (potentialSavings > 0) "Shopping List Savings" else "No Savings Yet",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF134E4A),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = if (potentialSavings > 0) "€${String.format("%.2f", potentialSavings)}" else "--",
                style = MaterialTheme.typography.displaySmall, // Bigger, hero style
                fontWeight = FontWeight.Bold,
                color = if (potentialSavings > 0) Color(0xFF059669) else Color(0xFF64748B) // Green 600 or Slate 500
            )
            
            Spacer(Modifier.height(Spacing.sm))
            
            Text(
                text = if (potentialSavings > 0) "Potential savings from your list" else "Find deals to start saving",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF0F766E)
            )
        }
    }
}
