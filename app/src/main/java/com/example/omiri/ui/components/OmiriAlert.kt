package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors

enum class OmiriAlertType {
    Success,
    Error,
    Warning,
    Info
}

@Composable
fun OmiriAlert(
    type: OmiriAlertType,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, borderColor, icon, iconColor) = when (type) {
        OmiriAlertType.Success -> Quadruple(
            Color(0xFFE8F7EF),
            Color(0xFF1E9E5A).copy(alpha = 0.2f),
            Icons.Default.CheckCircle,
            Color(0xFF1E9E5A)
        )
        OmiriAlertType.Error -> Quadruple(
            Color(0xFFFFE7E4),
            Color(0xFFE24A3B).copy(alpha = 0.2f),
            Icons.Default.Error,
            Color(0xFFE24A3B)
        )
        OmiriAlertType.Warning -> Quadruple(
            Color(0xFFFFF7E6),
            Color(0xFFF59E0B).copy(alpha = 0.2f),
            Icons.Default.Warning,
            Color(0xFFF59E0B)
        )
        OmiriAlertType.Info -> Quadruple(
            Color(0xFFEAF1FF),
            Color(0xFF2563EB).copy(alpha = 0.2f),
            Icons.Default.Info,
            Color(0xFF2563EB)
        )
    }

    Surface(
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Neutral900
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Neutral700,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = AppColors.Neutral400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
