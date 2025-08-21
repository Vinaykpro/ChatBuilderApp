package com.vinaykpro.chatbuilder.ui.components

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun SmallNativeAdView(
    adLoader: AdLoader,
    ad: NativeAd?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adView = remember { NativeAdView(context) }

    val bgColor = if (isDark) 0xFF2C2C2E.toInt() else 0xFFFFFFFF.toInt()
    val textColor = if (isDark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
    val secondaryTextColor = if (isDark) 0xFFAAAAAA.toInt() else 0xFF555555.toInt()

    LaunchedEffect(Unit) {
        adLoader.loadAd(AdRequest.Builder().build())
    }

    if (ad != null)
        AndroidView(
            factory = {
                adView.apply {
                    val layout = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(40, 40, 40, 40)
                        background = bgColor.toDrawable()
                        elevation = 8f
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        // Top Row (Icon + Texts)
                        val topRow = LinearLayout(context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }

                        val icon = ImageView(context).apply {
                            id = View.generateViewId()
                            layoutParams = LinearLayout.LayoutParams(150, 150).apply {
                                rightMargin = 16
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                        adView.iconView = icon
                        topRow.addView(icon)

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
                        }
                        adView.headlineView = headline
                        textColumn.addView(headline)

                        val body = TextView(context).apply {
                            id = View.generateViewId()
                            setTextColor(secondaryTextColor)
                            textSize = 14f
                            maxLines = 2
                        }
                        adView.bodyView = body
                        textColumn.addView(body)

                        topRow.addView(textColumn)
                        addView(topRow)

                        val cta = Button(context).apply {
                            id = View.generateViewId()
                            setTextColor(0xFFFFFFFF.toInt())
                            setBackgroundColor(secondaryTextColor)
                            textSize = 14f
                            setPadding(16, 8, 16, 8)
                        }
                        adView.callToActionView = cta
                        addView(cta)
                    }

                    addView(layout)
                }
            },
            update = { nativeAdView ->
                ad.let { nativeAd ->
                    (nativeAdView.headlineView as TextView).text = nativeAd.headline
                    (nativeAdView.bodyView as TextView).text = nativeAd.body
                    (nativeAdView.callToActionView as Button).text = nativeAd.callToAction

                    nativeAd.icon?.let { iconDrawable ->
                        (nativeAdView.iconView as ImageView).setImageDrawable(iconDrawable.drawable)
                        nativeAdView.iconView?.visibility = View.VISIBLE
                    } ?: run {
                        nativeAdView.iconView?.visibility = View.GONE
                    }

                    nativeAdView.setNativeAd(nativeAd)
                }
            },
            modifier = modifier
        )
}
