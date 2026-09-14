package com.vinaykpro.chatbuilder.ui.components

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun BigNativeAdView(
    ad: NativeAd?,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onLoadAdRequested: () -> Unit = {}
) {
    LaunchedEffect(ad) {
        if (ad == null) {
            onLoadAdRequested()
        }
    }

    if (ad == null) return

    val bgColor = if (isDark) 0xFF2C2C2E.toInt() else 0xFFFFFFFF.toInt()
    val textColor = if (isDark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
    val secondaryTextColor = if (isDark) 0xFFAAAAAA.toInt() else 0xFF555555.toInt()
    val accentColor = if (isDark) 0xFF0A84FF.toInt() else 0xFF007AFF.toInt()

    AndroidView(
        factory = { context ->
            // 2. Always create NativeAdView inside the factory
            NativeAdView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                val mainLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 32, 32, 32)
                    background = ColorDrawable(bgColor)
                    elevation = 4f
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                // Header Row (Icon + Text Column)
                val headerRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 24 }
                }

                val icon = ImageView(context).apply {
                    id = View.generateViewId()
                    layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                        rightMargin = 24
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                this.iconView = icon
                headerRow.addView(icon)

                val textColumn = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val headline = TextView(context).apply {
                    id = View.generateViewId()
                    setTextColor(textColor)
                    setTypeface(null, Typeface.BOLD)
                    textSize = 16f
                    maxLines = 1
                }
                this.headlineView = headline
                textColumn.addView(headline)

                val body = TextView(context).apply {
                    id = View.generateViewId()
                    setTextColor(secondaryTextColor)
                    textSize = 13f
                    maxLines = 2
                }
                this.bodyView = body
                textColumn.addView(body)

                headerRow.addView(textColumn)
                mainLayout.addView(headerRow)

                // 3. BIG MEDIA VIEW (For main image or video content)
                val mediaView = MediaView(context).apply {
                    id = View.generateViewId()
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        500 // Fixed height for a huge banner feel
                    ).apply { bottomMargin = 24 }
                }
                this.mediaView = mediaView
                mainLayout.addView(mediaView)

                // Bottom Call-To-Action Button
                val cta = Button(context).apply {
                    id = View.generateViewId()
                    setTextColor(0xFFFFFFFF.toInt())
                    setBackgroundColor(accentColor)
                    textSize = 15f
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                this.callToActionView = cta
                mainLayout.addView(cta)

                addView(mainLayout)
            }
        },
        update = { nativeAdView ->
            // 4. Safely extract and populate views inside the update block
            val headline = nativeAdView.headlineView as? TextView
            val body = nativeAdView.bodyView as? TextView
            val cta = nativeAdView.callToActionView as? Button
            val icon = nativeAdView.iconView as? ImageView
            val mediaView = nativeAdView.mediaView

            headline?.text = ad.headline
            body?.text = ad.body
            cta?.text = ad.callToAction

            if (ad.icon != null) {
                icon?.setImageDrawable(ad.icon?.drawable)
                icon?.visibility = View.VISIBLE
            } else {
                icon?.visibility = View.GONE
            }

            // Bind the media content to the MediaView container
            mediaView?.let {
                nativeAdView.mediaView?.setMediaContent(ad.mediaContent)
            }

            // Commit the ad binding
            nativeAdView.setNativeAd(ad)
        },
        modifier = modifier
    )
}