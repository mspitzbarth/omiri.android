package com.example.omiri.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.Spacing
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DealsCarousel(
    deals: List<Deal>,
    modifier: Modifier = Modifier,
    enableSnapping: Boolean = true,
    isLoading: Boolean = false,
    onDealClick: (Deal) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val screenWidth = maxWidth
        val density = LocalDensity.current
        
        // Dynamic Item Width Calculation
        // Goal: N full items + 0.5 item visible
        // Formula: Width = (AvailableWidth - (N * Gap)) / (N + 0.5)
        // We iterate to find an N that results in a card width closest to our ideal target (e.g., 160.dp)
        
        val targetWidth = 160.dp
        val horizontalPadding = Spacing.lg * 2
        val gap = Spacing.lg // Increased to match padding so previous item is fully cleared
        
        val availableWidth = screenWidth - horizontalPadding
        
        val itemWidth = remember(screenWidth, density) {
            var bestWidth = targetWidth
            var minDiff = Float.MAX_VALUE
            
            // Try matching for N = 1 to 4 items
            for (n in 1..4) {
                // Width * (n + 0.5) + gap * n = availableWidth
                // Width * (n + 0.5) = availableWidth - gap * n
                val w = (availableWidth - (gap * n)) / (n + 0.5f)
                
                if (w > 120.dp) { // Enforce a minimum practical width
                    val diff = kotlin.math.abs(w.value - targetWidth.value)
                    if (diff < minDiff) {
                        minDiff = diff
                        bestWidth = w
                    }
                }
            }
            bestWidth
        }

        val listState = rememberLazyListState()
        val flingBehavior = if (enableSnapping) {
            rememberSnapFlingBehavior(
                lazyListState = listState,
                snapPosition = androidx.compose.foundation.gestures.snapping.SnapPosition.Start
            )
        } else {
            androidx.compose.foundation.gestures.ScrollableDefaults.flingBehavior()
        }

        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            if (isLoading) {
                items(5) { // Show 5 skeletons
                    DealCardSkeleton(
                        modifier = Modifier.width(itemWidth)
                    )
                }
            } else {
                items(deals) { deal ->
                    DealCard(
                        deal = deal,
                        onClick = onDealClick,
                        modifier = Modifier.width(itemWidth)
                    )
                }
            }
        }
    }
}
