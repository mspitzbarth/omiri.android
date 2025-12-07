package com.example.omiri.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing
import kotlinx.coroutines.launch

data class FilterChipOption(
    val label: String,
    val icon: ImageVector? = null
)

@Composable
fun FilterChipsRow(
    modifier: Modifier = Modifier,
    options: List<FilterChipOption>,
    initialSelected: String = options.firstOrNull()?.label.orEmpty(),
    onSelectedChange: (String) -> Unit = {},
    selectedBackgroundColor: Color = Color(0xFFFE6B36),
    selectedTextColor: Color = Color(0xFFFFFFFF),
    unselectedBackgroundColor: Color = Color.Transparent,
    unselectedTextColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var selected by remember { mutableStateOf(initialSelected) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val itemWidthPx = with(density) { 90.dp.toPx() } // Estimated width for text chips
    val centerOffsetPx = (screenWidthPx - itemWidthPx) / 2

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        itemsIndexed(options) { index, option ->
            val isSelected = option.label == selected
            FilterChip(
                selected = isSelected,
                onClick = {
                    selected = option.label
                    onSelectedChange(option.label)
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = index,
                            scrollOffset = -centerOffsetPx.toInt()
                        )
                    }
                },
                label = {
                    Text(
                        text = option.label,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                },
                leadingIcon = if (option.icon != null) {
                    {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedBackgroundColor,
                    selectedLabelColor = selectedTextColor,
                    containerColor = unselectedBackgroundColor,
                    labelColor = unselectedTextColor,
                    selectedLeadingIconColor = selectedTextColor,
                    iconColor = unselectedTextColor
                ),
                modifier = Modifier.height(36.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
        }
    }
}
      
@Composable
private fun Dp.toPx(): Float = with(androidx.compose.ui.platform.LocalDensity.current) { this@toPx.toPx() }
