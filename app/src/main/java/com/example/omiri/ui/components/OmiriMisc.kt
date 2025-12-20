package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.theme.AppColors

@Composable
fun OmiriQuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AppColors.Neutral100,
        shape = RoundedCornerShape(100.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = AppColors.Neutral900,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = quantity.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Neutral900
            )
            
            IconButton(
                onClick = { onQuantityChange(quantity + 1) },
                modifier = Modifier.size(32.dp).clip(CircleShape).background(AppColors.BrandOrange)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun OmiriCountdownTimer(
    days: String,
    hours: String,
    minutes: String,
    seconds: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimerSegment(days, "Days")
        TimerSegment(hours, "Hours")
        TimerSegment(minutes, "Mins")
        TimerSegment(seconds, "Secs")
    }
}

@Composable
private fun TimerSegment(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppColors.BrandOrange, Color(0xFFFE5330))
                    ),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.Neutral500,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun OmiriStoreBadge(
    initial: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 32.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.5).sp
        )
    }
}

@Composable
fun OmiriSocialShareRow(
    onShareClick: (platform: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ShareIcon(Icons.Outlined.Share, "WhatsApp", Color(0xFF25D366), onShareClick)
        ShareIcon(Icons.Outlined.Share, "Facebook", Color(0xFF1877F2), onShareClick)
        ShareIcon(Icons.Outlined.Share, "Twitter", Color(0xFF1DA1F2), onShareClick)
        ShareIcon(Icons.Outlined.Share, "Email", AppColors.Neutral400, onShareClick)
    }
}

@Composable
private fun ShareIcon(icon: ImageVector, label: String, color: Color, onClick: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier
                .size(48.dp)
                .clickable { onClick(label) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = color)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.Neutral500,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
