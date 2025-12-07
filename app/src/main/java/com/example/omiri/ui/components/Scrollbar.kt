package com.example.omiri.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color = Color.Gray.copy(alpha = 0.5f)
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "ScrollbarAlpha"
    )

    drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.firstVisibleItemIndex
        val visibleItemCount = state.layoutInfo.visibleItemsInfo.size
        val totalItemCount = state.layoutInfo.totalItemsCount

        // Only draw if there are items and we need to scroll
        if (totalItemCount > visibleItemCount && totalItemCount > 0 && alpha > 0.01f) {
            val elementHeight = this.size.height / totalItemCount
            val scrollbarHeight = elementHeight * visibleItemCount
            
            // Minimal height check
            val finalScrollbarHeight = scrollbarHeight.coerceAtLeast(20.dp.toPx())
            
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight

            drawRoundRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), finalScrollbarHeight),
                alpha = alpha,
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
            )
        }
    }
}
