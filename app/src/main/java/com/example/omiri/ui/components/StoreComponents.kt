package com.example.omiri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.omiri.data.api.models.StoreListResponse
import com.example.omiri.ui.theme.Spacing

@Composable
fun PopularStoreItem(
    store: StoreListResponse,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(40.dp) // Reduced from 72dp
            .clickable(onClick = onClick)
    ) {
        // Icon Container
        Card(
            modifier = Modifier.size(32.dp), // Reduced from 64dp
            shape = RoundedCornerShape(8.dp), // Reduced from 16dp
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFFFFF7ED) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFFFE8357) else Color(0xFFE5E7EB)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Fallback Initials
                Text(
                    text = store.retailer.take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge, // Reduced from titleLarge
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFE8357)
                )
            }
        }
        
        Spacer(Modifier.height(2.dp)) // Reduced spacing
        
        // Name
        Text(
            text = store.retailer,
            style = MaterialTheme.typography.labelSmall, // Reduced from bodySmall
            fontSize = 9.sp, // Explicitly smaller text
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFFFE8357) else Color(0xFF4B5563),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
