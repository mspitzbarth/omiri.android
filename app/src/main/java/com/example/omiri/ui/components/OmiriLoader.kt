package com.example.omiri.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OmiriLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = Color(0xFFEA580B),
    trackColor: Color = Color(0xFFFFE4D6), // Light orange/peach track
    strokeWidth: Dp = 6.dp
) {
    val transition = rememberInfiniteTransition(label = "SpinnerTransition")
    
    // Rotate the whole spinner
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "SpinnerRotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
        
        // Draw Track
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )

        // Draw Progress Arc (Fixed size arc rotating)
        // From image: looks like ~25-30% of the circle is active color
        drawArc(
            color = color,
            startAngle = rotation - 90f, // Start from top
            sweepAngle = 90f, // 90 degrees arc length
            useCenter = false,
            style = stroke
        )
    }
}
