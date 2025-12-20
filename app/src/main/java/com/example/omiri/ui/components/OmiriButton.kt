package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.theme.AppColors

enum class OmiriButtonStyle {
    Primary,
    Secondary,
    Soft,
    Neutral,
    Gradient,
    Danger
}

enum class OmiriButtonSize {
    Large,
    Medium,
    Small
}

@Composable
fun OmiriButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: OmiriButtonStyle = OmiriButtonStyle.Primary,
    size: OmiriButtonSize = OmiriButtonSize.Medium,
    shape: androidx.compose.ui.graphics.Shape? = null,
    isRounded: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    fullWidth: Boolean = true
) {
    val height = when (size) {
        OmiriButtonSize.Large -> 56.dp
        OmiriButtonSize.Medium -> 48.dp
        OmiriButtonSize.Small -> 36.dp
    }

    val finalShape = shape ?: if (isRounded) CircleShape else RoundedCornerShape(12.dp)
    
    val contentColor = when (style) {
        OmiriButtonStyle.Primary, OmiriButtonStyle.Gradient, OmiriButtonStyle.Danger -> Color.White
        OmiriButtonStyle.Secondary -> AppColors.BrandOrange
        OmiriButtonStyle.Soft -> AppColors.BrandOrange
        OmiriButtonStyle.Neutral -> AppColors.Neutral900
    }

    val backgroundColor = when (style) {
        OmiriButtonStyle.Primary -> AppColors.BrandOrange
        OmiriButtonStyle.Secondary -> Color.Transparent
        OmiriButtonStyle.Soft -> AppColors.BrandOrangeSoft
        OmiriButtonStyle.Neutral -> AppColors.Neutral100
        OmiriButtonStyle.Gradient -> Color.Transparent // Handled via modifier
        OmiriButtonStyle.Danger -> AppColors.Danger
    }

    val borderStroke = if (style == OmiriButtonStyle.Secondary) {
        androidx.compose.foundation.BorderStroke(1.dp, AppColors.BrandOrange)
    } else null

    val containerModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
        .height(height)
        .clip(finalShape)
        .then(
            if (style == OmiriButtonStyle.Gradient && enabled) {
                Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.BrandOrange,
                            Color(0xFFFE5330) // Darker orange for gradient
                        )
                    )
                )
            } else if (!enabled) {
                Modifier.background(AppColors.Neutral300)
            } else {
                Modifier.background(backgroundColor)
            }
        )
        .then(if (borderStroke != null && enabled) Modifier.border(borderStroke, finalShape) else Modifier)
        .clickable(enabled = enabled, onClick = onClick)

    Row(
        modifier = containerModifier.padding(horizontal = if (size == OmiriButtonSize.Small) 12.dp else 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (enabled) contentColor else AppColors.Neutral500,
                modifier = Modifier.size(if (size == OmiriButtonSize.Small) 18.dp else 20.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = when (size) {
                    OmiriButtonSize.Small -> 14.sp
                    else -> 16.sp
                }
            ),
            color = if (enabled) contentColor else AppColors.Neutral500
        )

        if (trailingIcon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = if (enabled) contentColor else AppColors.Neutral500,
                modifier = Modifier.size(if (size == OmiriButtonSize.Small) 18.dp else 20.dp)
            )
        }
    }
}

@Composable
fun OmiriIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: OmiriButtonStyle = OmiriButtonStyle.Primary,
    isRounded: Boolean = true,
    enabled: Boolean = true
) {
    val backgroundColor = when (style) {
        OmiriButtonStyle.Primary -> AppColors.BrandOrange
        OmiriButtonStyle.Secondary -> Color.Transparent
        OmiriButtonStyle.Soft -> AppColors.BrandOrangeSoft
        OmiriButtonStyle.Neutral -> AppColors.Neutral100
        else -> AppColors.BrandOrange
    }

    val contentColor = when (style) {
        OmiriButtonStyle.Primary -> Color.White
        OmiriButtonStyle.Secondary -> AppColors.BrandOrange
        OmiriButtonStyle.Soft -> AppColors.BrandOrange
        OmiriButtonStyle.Neutral -> AppColors.Neutral900
        else -> Color.White
    }

    val shape = if (isRounded) CircleShape else RoundedCornerShape(12.dp)
    val borderStroke = if (style == OmiriButtonStyle.Secondary) {
        androidx.compose.foundation.BorderStroke(1.dp, AppColors.BrandOrange)
    } else null

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .background(if (enabled) backgroundColor else AppColors.Neutral300)
            .then(if (borderStroke != null && enabled) Modifier.border(borderStroke, shape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) contentColor else AppColors.Neutral500,
            modifier = Modifier.size(24.dp)
        )
    }
}
