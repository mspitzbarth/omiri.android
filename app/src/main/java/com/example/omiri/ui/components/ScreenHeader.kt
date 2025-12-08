package com.example.omiri.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun ScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
         Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
            Spacer(Modifier.height(Spacing.xxs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Left)
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(32.dp) // Smaller touch target as requested visually, or keep 40 but no bg
                ) {
                     Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(20.dp) // Smaller icon
                    )
                }
                
                Spacer(Modifier.width(Spacing.xs))

                // Title
                Text(
                     text = title,
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold,
                     color = Color(0xFF111827)
                )

                Spacer(Modifier.weight(1f))

                // Optional Action (Right)
                if (action != null) {
                    action()
                }
            }
            Spacer(Modifier.height(Spacing.xxs))
         }
    }
}
