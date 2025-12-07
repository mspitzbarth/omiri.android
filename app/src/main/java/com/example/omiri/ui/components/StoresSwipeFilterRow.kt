package com.example.omiri.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

data class StoreFilterOption(
    val id: String,
    val name: String,
    val shortName: String? = null, // optional UI-friendly label
    val emoji: String? = null      // optional brand vibe
)

@Composable
fun StoresSwipeFilterRow(
    stores: List<StoreFilterOption>,
    selectedStoreIds: Collection<String>, // ✅ instead of Set<String>
    onStoreToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(end = Spacing.lg),
    selectedBackgroundColor: Color = Color(0xFF111827),
    selectedTextColor: Color = Color.White,
    unselectedBackgroundColor: Color = Color(0xFFF3F4F6),
    unselectedTextColor: Color = Color(0xFF111827)
) {
    if (stores.isEmpty()) return

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val itemWidthPx = with(density) { 60.dp.toPx() } // Estimated width
    val centerOffsetPx = (screenWidthPx - itemWidthPx) / 2

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        contentPadding = contentPadding
    ) {
        itemsIndexed(stores) { index, store ->
            val isSelected = selectedStoreIds.contains(store.id)

            FilterChip(
                selected = isSelected,
                onClick = { 
                    onStoreToggle(store.id)
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = index, 
                            scrollOffset = -centerOffsetPx.toInt() // Negative offset pushes item to the right (center)
                        )
                    }
                },
                label = {
                    Text(
                        text = store.emoji ?: store.name.take(1),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                },
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedBackgroundColor,
                    selectedLabelColor = selectedTextColor,
                    containerColor = unselectedBackgroundColor,
                    labelColor = unselectedTextColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

