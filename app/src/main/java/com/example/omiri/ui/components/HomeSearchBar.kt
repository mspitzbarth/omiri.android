package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun HomeSearchBar(
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onSearchClick),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF3F4F6) // Light gray background like input field
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(Modifier.width(Spacing.sm))
            
            Text(
                text = "Search products, stores, categories...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Search",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
