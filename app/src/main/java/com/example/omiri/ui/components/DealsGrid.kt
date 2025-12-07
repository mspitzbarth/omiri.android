package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.Spacing

@Composable
fun DealsGrid(
    deals: List<Deal>,
    modifier: Modifier = Modifier,
    onDealClick: (Deal) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = Spacing.xxxl),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        items(deals, key = { it.id }) { deal ->
            DealCard(
                deal = deal,
                onClick = onDealClick
            )
        }
    }
}
