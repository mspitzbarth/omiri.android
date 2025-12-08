
package com.example.omiri.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(progress: Float = 0f) {
    // Rotating messages
    val messages = remember {
        listOf(
            "Finding the best deals...",
            "Checking local stores...",
            "Preparing your Smart Plan...",
            "Mixing ingredients...",
            "Loading fresh discounts..."
        )
    }
    var currentMessageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500) // Change message every 1.5s
            currentMessageIndex = (currentMessageIndex + 1) % messages.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Checkmark/Logo Icon
            PennyPalLogo(modifier = Modifier.size(100.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // PennyPal Text
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)) {
                        append("Penny")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFFF7D29), fontWeight = FontWeight.Bold)) {
                        append("Pal")
                    }
                },
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline or Status Message
            Text(
                text = messages[currentMessageIndex], // Dynamic text
                color = Color(0xFF64748B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Spinner
            androidx.compose.material3.CircularProgressIndicator(
                color = Color(0xFFFF7D29),
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        }
        
    }
}
// Removed TypingDotsAnimation composable as requested

@Composable
fun PennyPalLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFFF7D29), RoundedCornerShape(24.dp)), // Orange rounded square
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            // Dashed Circle
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 2,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                )
            )
            
            // Exclamation Mark
            // Draw dot
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = center.copy(y = center.y + 10.dp.toPx())
            )
            
            // Draw vertical line
            drawLine(
                color = Color.White,
                start = center.copy(y = center.y - 12.dp.toPx()),
                end = center.copy(y = center.y + 4.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
