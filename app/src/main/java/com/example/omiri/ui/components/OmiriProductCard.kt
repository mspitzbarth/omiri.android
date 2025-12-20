package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.theme.AppColors

@Composable
fun OmiriVerticalProductCard(
    title: String,
    storeName: String,
    price: String,
    oldPrice: String? = null,
    savePercentage: String? = null,
    timeLeft: String? = null,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Neutral200)
    ) {
        Column {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(AppColors.Neutral100)
            ) {
                // Placeholder for Image
                // Image(...) 
                
                if (timeLeft != null) {
                    OmiriDiscountBadge(
                        text = timeLeft,
                        modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                        isHot = true
                    )
                }

                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                    color = Color.White,
                    shape = CircleShape,
                ) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = AppColors.Neutral400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Content Section
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small Store Icon Placeholder
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AppColors.Info),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("C", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Neutral500
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Neutral900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.BrandOrange
                    )
                    if (oldPrice != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = oldPrice,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = AppColors.Neutral400
                        )
                    }
                }

                if (savePercentage != null) {
                    Spacer(Modifier.height(8.dp))
                    OmiriStatusBadge(
                        text = "Save $savePercentage",
                        color = AppColors.Success,
                        softBackground = true
                    )
                }
            }
        }
    }
}

@Composable
fun OmiriHorizontalProductCard(
    title: String,
    storeName: String,
    price: String,
    oldPrice: String? = null,
    discountBadge: String? = null,
    timeLeft: String? = null,
    isOnList: Boolean = false,
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Neutral200)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small Image Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.Neutral100)
            ) {
                if (discountBadge != null) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(AppColors.BrandOrange, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(discountBadge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AppColors.Danger),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("T", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Neutral500
                    )
                    
                    if (isOnList) {
                        Spacer(Modifier.width(8.dp))
                        OmiriStatusBadge(text = "On List", color = AppColors.Success)
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Neutral900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.BrandOrange
                    )
                    if (oldPrice != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = oldPrice,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = AppColors.Neutral400
                        )
                    }
                }

                if (timeLeft != null) {
                    Spacer(Modifier.height(8.dp))
                    OmiriTimeBadge(text = timeLeft, color = AppColors.Danger)
                }
            }

            Spacer(Modifier.width(8.dp))

            OmiriButton(
                text = "Add",
                onClick = onAddClick,
                style = OmiriButtonStyle.Primary,
                size = OmiriButtonSize.Small,
                fullWidth = false
            )
        }
    }
}
