package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun SmartPlanCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E7FF) // Light Blue bg
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0DBFE))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            // Header: Icon + Title
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF3B82F6), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb, // Fallback icon
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(Spacing.md))
                Column {
                    Text(
                        text = "Your Smart Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Optimized for fewer trips and maximum savings",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            // Steps
            PlanStep(color = Color(0xFF3B82F6), text = "Go to Lidl for 5 list items (save €9.10)")
            Spacer(Modifier.height(Spacing.sm))
            PlanStep(color = Color(0xFF3B82F6), text = "Then Aldi for 2 items (save €4.30)")
            Spacer(Modifier.height(Spacing.sm))
            PlanStep(color = Color(0xFF3B82F6), text = "Or one-stop option: Kaufland (save €10.20)")
        }
    }
}

@Composable
private fun PlanStep(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1F2937)
        )
    }
}
