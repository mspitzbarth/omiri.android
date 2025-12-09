package com.example.omiri.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.Deal
import androidx.compose.foundation.layout.PaddingValues
import com.example.omiri.ui.theme.Spacing
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.lazy.rememberLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DealsCarousel(
    deals: List<Deal>,
    modifier: Modifier = Modifier,
    enableSnapping: Boolean = false,
    onDealClick: (Deal) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val flingBehavior = if (enableSnapping) {
        rememberSnapFlingBehavior(lazyListState = listState)
    } else {
        ScrollableDefaults.flingBehavior()
    }

    LazyRow(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        items(deals) { deal ->
            DealCard(
                deal = deal,
                onClick = onDealClick,
                modifier = Modifier.width(160.dp)
            )
        }
    }
}
