package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.omiri.ui.theme.Radius
import com.example.omiri.ui.theme.Spacing

@Composable
fun BannerAdPlaceholder(
    modifier: Modifier = Modifier,
    text: String = "320×50 Test Ad Space"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.xxl + Spacing.lg)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.large
            )
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
