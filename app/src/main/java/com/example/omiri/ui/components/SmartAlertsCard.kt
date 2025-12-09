package com.example.omiri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.ChevronRight
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
fun SmartAlertsCard(
    alerts: List<com.example.omiri.data.api.models.SmartAlert> = emptyList()
) {
    if (alerts.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
    ) {
        alerts.take(3).forEachIndexed { index, alert ->
            OmiriTipCard(
                title = "Smart Alert", // Or generic title, or alert.type if we have mapping
                body = alert.title,
                icon = getIconForName(alert.iconName),
                onClick = { /* Handle click */ }
            )
            
            if (index < alerts.take(3).size - 1) {
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }
    }
}

private fun getIconForName(name: String): ImageVector {
    return when(name) {
        "PERCENT" -> Icons.Default.Percent
        "HOME" -> Icons.Default.Store
        "CHECK_CIRCLE" -> Icons.Filled.CheckCircle
        "CLOCK" -> Icons.Default.AccessTime 
        else -> Icons.Default.Info
    }
}
