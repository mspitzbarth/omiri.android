package com.example.omiri.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun OmiriSummaryCard(
    title: String,
    headerEndContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    expandedContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) // Gray 200
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge, // Matches Shopping List Item
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827) // Gray 900
                )
                
                headerEndContent?.invoke()
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Content
            content()
            
            // Expandable Section
            if (expandedContent != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Divider and Chevron
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                    Column {
                        expandedContent()
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF111827),
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium, // Reduced to match 'Matched Deals' text
            color = Color(0xFF4B5563) // Gray 600
        )
        
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium, // Reduced from HeadlineSmall
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
