package com.example.omiri.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111", // Test ad unit ID
    onAdLoaded: () -> Unit = {}
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(adSize)
                setAdUnitId(adUnitId)
                adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        onAdLoaded()
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
