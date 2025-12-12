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
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing
import kotlinx.coroutines.launch

data class MixedFilterOption(
    val label: String,
    val icon: ImageVector? = null,
    val isToggleable: Boolean = false // true = can toggle on/off independently, false = mutually exclusive
)

@Composable
fun MixedFilterChipsRow(
    modifier: Modifier = Modifier,
    options: List<MixedFilterOption>,
    initialSelected: String = options.firstOrNull { !it.isToggleable }?.label.orEmpty(),
    initialToggled: Set<String> = emptySet(),
    onSelectedChange: (String) -> Unit = {},
    onToggledChange: (Set<String>) -> Unit = {},
    selectedBackgroundColor: Color = com.example.omiri.ui.theme.AppColors.BrandOrange,
    selectedTextColor: Color = com.example.omiri.ui.theme.AppColors.Surface,
    unselectedBackgroundColor: Color = com.example.omiri.ui.theme.AppColors.PastelGrey,
    unselectedTextColor: Color = com.example.omiri.ui.theme.AppColors.BrandInk
) {
    var selected by remember { mutableStateOf(initialSelected) }
    var toggled by remember { mutableStateOf(initialToggled) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val itemWidthPx = with(density) { 90.dp.toPx() }
    val centerOffsetPx = (screenWidthPx - itemWidthPx) / 2

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        itemsIndexed(options) { index, option ->
            val isSelected = if (option.isToggleable) {
                toggled.contains(option.label)
            } else {
                option.label == selected
            }
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (option.isToggleable) {
                        // Toggle on/off independently
                        val newToggled = toggled.toMutableSet().apply {
                            if (contains(option.label)) remove(option.label) else add(option.label)
                        }
                        toggled = newToggled
                        onToggledChange(newToggled)
                    } else {
                        // Mutually exclusive selection
                        selected = option.label
                        onSelectedChange(option.label)
                    }
                    
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
