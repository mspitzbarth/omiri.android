package com.example.omiri.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    // adSize param removed
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111", // Test ad unit ID
    onAdLoaded: () -> Unit = {}
) {
    // No-op
}
