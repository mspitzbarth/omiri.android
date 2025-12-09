package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun HomeHeader(
    potentialSavings: String = "€18.40"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF7ED)) // Light peach/beige background
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
    ) {
        // Reduced Header Content (No Greeting)
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "You could save $potentialSavings this week based on your list.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4B5563)
        )
    }
}
