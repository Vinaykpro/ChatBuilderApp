package com.vinaykpro.chatbuilder.ui.components

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(
    adId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        factory = {
            FrameLayout(context).apply {
                val adView = AdView(context).apply {
                    setAdSize(getAdSize(context))
                    adUnitId = adId
                    loadAd(AdRequest.Builder().build())
                }
                addView(adView)
            }
        }
    )
}

fun getAdSize(context: Context): AdSize {
    val display = context.resources.displayMetrics
    val adWidth = (display.widthPixels / display.density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}
