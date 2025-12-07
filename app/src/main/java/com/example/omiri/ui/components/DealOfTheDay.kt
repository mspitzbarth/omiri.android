package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.theme.Spacing

@Composable
fun DealOfTheDay(
    title: String = "Apple AirPods Pro 2nd Gen",
    store: String = "Apple Store",
    price: String = "$199",
    originalPrice: String = "$249",
    discount: String = "20% OFF",
    imageColor: Color = Color(0xFF60A5FA),
    modifier: Modifier = Modifier,
    onGetDealClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            // Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💰",
                    fontSize = 20.sp
                )
                Text(
                    text = "DEAL OF THE DAY",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24)
                )
            }

            Spacer(Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side - Product info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 28.sp
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Store,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = store,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    Spacer(Modifier.height(Spacing.md))

                    // Price
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = price,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = originalPrice,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF9CA3AF),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = discount,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Right side - Product image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(imageColor)
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            // CTA Button
            Button(
                onClick = onGetDealClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Get This Deal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
