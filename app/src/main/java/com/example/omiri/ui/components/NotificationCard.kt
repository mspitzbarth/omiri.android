package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.models.NotificationUiModel
import com.example.omiri.ui.theme.Spacing

@Composable
fun NotificationCard(
    notification: NotificationUiModel,
    onCardClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)), // Light gray border
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(notification.iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header (Title + Time + Unread Dot)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFF97316), CircleShape) // Orange dot
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Description and Content
                when (notification) {
                    is NotificationUiModel.FlashSale -> {
                        Text(
                            text = notification.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NotificationTag(
                                text = notification.timeLeft,
                                backgroundColor = Color(0xFFFEE2E2), // Light Red
                                contentColor = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notification.savings,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981) // Green
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = onActionClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF97316) // Orange
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(notification.actionLabel, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalIconButton(
                                onClick = { /* Save/Bookmark */ },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0xFFF3F4F6)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = "Save",
                                    tint = Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                    is NotificationUiModel.PriceDrop -> {
                        Text(
                            text = notification.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = notification.currentPrice,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notification.originalPrice,
                                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                                color = Color(0xFF9CA3AF)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            NotificationTag(
                                text = notification.discountPercentage,
                                backgroundColor = Color(0xFFD1FAE5), // Light Green
                                contentColor = Color(0xFF10B981)
                            )
                        }
                    }
                    is NotificationUiModel.ListUpdate -> {
                        Text(
                            text = notification.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NotificationTag(
                            text = notification.locationTag,
                            backgroundColor = Color(0xFFDBEAFE), // Light Blue
                            contentColor = Color(0xFF3B82F6)
                        )
                    }
                    is NotificationUiModel.Reward -> {
                        Text(
                            text = notification.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NotificationTag(
                            text = notification.pointsTag,
                            backgroundColor = Color(0xFFF3E8FF), // Light Purple
                            contentColor = Color(0xFFA855F7)
                        )
                    }
                    is NotificationUiModel.General -> {
                        Text(
                            text = notification.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )
                        if (notification.tag != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            NotificationTag(
                                text = notification.tag,
                                backgroundColor = Color(0xFFFEF3C7), // Light Yellow/Orange
                                contentColor = Color(0xFFD97706)
                            )
                        }
                        if (notification.actionLabel != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onActionClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDBEAFE), // Light Blue for secondary action?
                                    contentColor = Color(0xFF1D4ED8)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(notification.actionLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationTag(
    text: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
