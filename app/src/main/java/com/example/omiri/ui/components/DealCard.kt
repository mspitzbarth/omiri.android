package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

    val shape = MaterialTheme.shapes.medium
    val borderColor = AppColors.Neutral100

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(deal) },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Neutral200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Image placeholder with time-left badge + favorite button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .height(140.dp)
                .background(backgroundColor ?: deal.heroColor ?: EmojiHelper.getCategoryColor(deal.category)),
            contentAlignment = Alignment.Center
        ) {
            if (!deal.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = deal.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val emoji = EmojiHelper.getProductEmoji(deal.title, deal.category)
                if (emoji.isNotEmpty()) {
                    Text(text = emoji, fontSize = 64.sp)
                }
            }

            // Badges Container at Top Start (Discount, Time Left, In List)
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                
                // 2. Time Left Badge
                if (!deal.timeLeftLabel.isNullOrBlank()) {
                     val badgeColor = when {
                        deal.timeLeftLabel!!.contains("hour", ignoreCase = true) -> AppColors.Red600
                        deal.timeLeftLabel!!.contains("today", ignoreCase = true) -> AppColors.Red600
                        deal.timeLeftLabel!!.contains("expired", ignoreCase = true) -> AppColors.Red600
                        deal.timeLeftLabel!!.contains("1 day", ignoreCase = true) ||
                                deal.timeLeftLabel!!.contains("2 day", ignoreCase = true) -> AppColors.Orange400
                        else -> AppColors.Sky500
                    }
                    
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = deal.timeLeftLabel!!,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // 3. In List Badge
                if (deal.isOnShoppingList) {
                    Surface(
                        color = AppColors.Purple50, // Light Purple
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color(0xFFA12AF9), // Purple
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "In List",
                                color = AppColors.PurpleText, // Purple
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.sm)
                    .size(28.dp)
                    .background(
                        color = AppColors.Surface.copy(alpha = 0.9f),
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
                    tint = if (isFav) AppColors.Danger else AppColors.Neutral800,
                    modifier = Modifier.size(16.dp)
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
                    tint = AppColors.Neutral500
                )
                Text(
                    text = deal.store,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Neutral500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(Spacing.xs))

            Text(
                text = deal.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = AppColors.Neutral800
            )

            Spacer(Modifier.height(Spacing.sm))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = deal.price,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.BrandOrange
                )
                if (!deal.originalPrice.isNullOrBlank() && deal.discountPercentage > 0) {
                    Text(
                        text = deal.originalPrice!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Neutral400,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            if (!deal.discountLabel.isNullOrBlank() || (deal.discountPercentage > 0)) {
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (deal.discountPercentage > 0) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = AppColors.Green100,
                            contentColor = AppColors.GreenTextDark
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
