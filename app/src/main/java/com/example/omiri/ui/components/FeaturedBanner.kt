package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun FeaturedBanner(
    modifier: Modifier = Modifier,
    badge: String = "FLASH SALE",
    title: String = "Summer Essentials",
    description: String = "Get up to 50% off on all summer gear and electronics.",
    buttonText: String = "Shop Now",
    onButtonClick: () -> Unit = {},
    backgroundColor: List<Color> = listOf(
        Color(0xFFE91E63),
        Color(0xFFFF6F00)
    )
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(backgroundColor)
                )
                .padding(Spacing.lg)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Badge
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        labelColor = backgroundColor.first()
                    ),
                    modifier = Modifier.height(24.dp)
                )

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.widthIn(max = 200.dp)
                )

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.widthIn(max = 220.dp)
                )

                Spacer(Modifier.height(Spacing.xs))

                // Button
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = backgroundColor.first()
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
