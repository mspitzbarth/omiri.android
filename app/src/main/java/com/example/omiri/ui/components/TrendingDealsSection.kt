package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.Spacing

@Composable
fun TrendingDealsSection(
    deals: List<Deal>,
    onDealClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (deals.isEmpty()) return

    Column(modifier = modifier) {
        // Section Header with visual flair
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFDC2626), // Red
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Trending Now",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            // Removed "Live" badge per user request
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(deals) { deal ->
                TrendingDealCard(
                    deal = deal,
                    onClick = { onDealClick(deal.id) }
                )
            }
        }
    }
}

@Composable
fun TrendingDealCard(
    deal: Deal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(deal.heroColor ?: MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!deal.imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = deal.imageUrl,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = com.example.omiri.util.EmojiHelper.getProductEmoji(deal.title, deal.category).ifEmpty { "🔥" },
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = deal.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Text(
                        text = deal.price,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626) // Highlight price
                    )
                    if (deal.discountPercentage > 0) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "-${deal.discountPercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            }
        }
    }
}
