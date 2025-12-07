package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.Spacing

@Composable
fun DealsCarousel(
    deals: List<Deal>,
    modifier: Modifier = Modifier,
    onDealClick: (String) -> Unit = {},
    onToggleShoppingList: (Deal, Boolean) -> Unit = { _, _ -> }
) {
    // Calculate card width to match the 2-column grid cards
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    // Match the grid card width: (screenWidth - 2×sidePadding - middleSpacing) / 2
    val cardWidth = (screenWidth - (Spacing.lg * 2) - Spacing.md) / 2

    // Vibrant colors for cards
    val cardColors = listOf(
         androidx.compose.ui.graphics.Color(0xFFEA580B), // Orange
         androidx.compose.ui.graphics.Color(0xFF3B82F6), // Blue
         androidx.compose.ui.graphics.Color(0xFFEF4444), // Red
         androidx.compose.ui.graphics.Color(0xFF10B981), // Emerald
         androidx.compose.ui.graphics.Color(0xFF8B5CF6), // Violet
         androidx.compose.ui.graphics.Color(0xFFF59E0B), // Amber
         androidx.compose.ui.graphics.Color(0xFFEC4899), // Pink
         androidx.compose.ui.graphics.Color(0xFF14B8A6), // Teal
         androidx.compose.ui.graphics.Color(0xFF6366F1)  // Indigo
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        contentPadding = PaddingValues(horizontal = Spacing.lg)
    ) {
        itemsIndexed(deals, key = { _, item -> item.id }) { index, deal ->
            DealCard(
                deal = deal,
                backgroundColor = cardColors[index % cardColors.size],
                onClick = { onDealClick(deal.id) },
                onFavoriteChange = onToggleShoppingList,
                modifier = Modifier.width(cardWidth)
            )
        }
    }
}
