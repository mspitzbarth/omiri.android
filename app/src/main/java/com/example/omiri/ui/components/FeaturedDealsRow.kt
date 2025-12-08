package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.Spacing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@Composable
fun FeaturedDealsRow(
    deals: List<Deal>,
    isLoading: Boolean = false,
    emptyMessage: String = "No deals found",
    onViewAll: () -> Unit = {},
    onDealClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Padding(padding = PaddingValues(horizontal = Spacing.lg)) {
             SectionHeader(
                title = "Featured Deals for You",
                actionText = "View All",
                onActionClick = onViewAll
            )
        }
        
        Spacer(Modifier.height(Spacing.md))
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Height of a card roughly
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFEA580B))
            }
        } else if (deals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                 deals.forEach { deal ->
                      DealCard(
                        deal = deal,
                        onClick = { onDealClick(deal.id) },
                        modifier = Modifier.width(160.dp)
                    )
                 }
            }
        }
    }
}

@Composable
private fun Padding(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(padding)) {
        content()
    }
}

// Temporary mock card for visualization if no data is passed
@Composable
fun MockDealCard(
    title: String,
    store: String,
    price: String,
    oldPrice: String,
    badgeText: String,
    badgeColor: Color
) {
    // Reusing the structure of DealCard visually but hardcoded for the mock view
    androidx.compose.material3.Card(
        modifier = Modifier.width(160.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.Gray) // Placeholder image
            ) {
                // Badge
                androidx.compose.material3.Surface(
                    color = badgeColor,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = badgeText,
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                androidx.compose.material3.Text(
                    text = store,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                androidx.compose.material3.Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                     fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    text = price,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = Color(0xFFEA580B),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                androidx.compose.material3.Text(
                    text = oldPrice,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
            }
        }
    }
}
