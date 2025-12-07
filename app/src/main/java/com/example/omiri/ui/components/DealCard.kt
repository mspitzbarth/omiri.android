package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.util.EmojiHelper

@Composable
fun DealCard(
    deal: Deal,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onClick: (Deal) -> Unit = {},
    onFavoriteChange: (Deal, Boolean) -> Unit = { _, _ -> }
) {
    var isFav by remember(deal.id) { mutableStateOf(deal.isFavorite) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(deal) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Image placeholder with time-left badge + favorite button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(backgroundColor ?: deal.heroColor ?: AppColors.SurfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            if (!deal.imageUrl.isNullOrBlank() && deal.hasDiscount == true && deal.discountPercentage > 0) {
                coil.compose.AsyncImage(
                    model = deal.imageUrl,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val emoji = EmojiHelper.getProductEmoji(deal.title, deal.category)
                if (emoji.isNotEmpty()) {
                    Text(
                        text = emoji,
                        fontSize = 64.sp
                    )
                }
            }

            if (!deal.timeLeftLabel.isNullOrBlank()) {
                val badgeColor = when {
                    deal.timeLeftLabel!!.contains("hour", ignoreCase = true) -> Color(0xFFDC2626)
                    deal.timeLeftLabel!!.contains("today", ignoreCase = true) -> Color(0xFFEA580C)
                    deal.timeLeftLabel!!.contains("1 day", ignoreCase = true) ||
                            deal.timeLeftLabel!!.contains("2 day", ignoreCase = true) -> Color(0xFFFB923C)
                    else -> Color(0xFF0EA5E9)
                }

                Surface(
                    modifier = Modifier
                        .padding(Spacing.sm)
                        .align(Alignment.TopStart),
                    shape = MaterialTheme.shapes.small,
                    color = badgeColor,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = deal.timeLeftLabel!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.sm) // Same padding as badge
                    .size(28.dp) // Slightly larger than icon for touch target, but visual alignment is key
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .clickable {
                        isFav = !isFav
                        onFavoriteChange(deal, isFav)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) AppColors.Danger else Color(0xFF1F2937),
                    modifier = Modifier.size(16.dp) // Slightly larger icon to match badge text weight
                )
            }

        }

        // Content section
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Store,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF6B7280) // Gray 500
                )
                Text(
                    text = deal.store,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(Spacing.xs))

            Text(
                text = deal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1F2937) // Gray 900
            )

            Spacer(Modifier.height(Spacing.sm))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = deal.price,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEA580C) // Orange 600
                )
                if (!deal.originalPrice.isNullOrBlank()) {
                    Text(
                        text = deal.originalPrice!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF), // Gray 400
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            if (!deal.discountLabel.isNullOrBlank() || (deal.discountPercentage > 0)) {
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     // Percentage Badge
                     if (deal.discountPercentage > 0) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Color(0xFFDCFCE7), // Green 100
                            contentColor = Color(0xFF166534) // Green 800
                        ) {
                            Text(
                                text = "${deal.discountPercentage}% OFF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                     }
                }
            }
        }
    }
}
