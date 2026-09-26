package com.vinaykpro.chatbuilder.ui.components

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun ChatNativeAdView(
    adLoader: AdLoader? = null,
    ad: NativeAd?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bgColor = if (isDark) 0xFF252525.toInt() else 0xFFF8F8F8.toInt()
    val textColor = if (isDark) 0xFFFFFFFF.toInt() else 0xFF171717.toInt()
    val secondaryTextColor = if (isDark) 0xFFB0B0B0.toInt() else 0xFF666666.toInt()
    val accentColor = if (isDark) 0xFF4CAF50.toInt() else 0xFF2E7D32.toInt()

    LaunchedEffect(Unit) {
        adLoader?.loadAd(AdRequest.Builder().build())
    }

    if (ad != null) {
        AndroidView(
            factory = {
                NativeAdView(context).apply {
                    val adView = this

                    val container = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(14, 10, 14, 10)

                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            setColor(bgColor)
                            cornerRadius = 18f
                            setStroke(
                                1,
                                if (isDark) 0xFF3A3A3A.toInt()
                                else 0xFFE5E5E5.toInt()
                            )
                        }

                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 6
                            bottomMargin = 6
                        }
                    }

                    val adLabel = TextView(context).apply {
                        text = "ADVERTISEMENT"
                        setTextColor(secondaryTextColor)
                        textSize = 9f
                        setTypeface(null, Typeface.BOLD)
                        letterSpacing = 0.08f
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = 6
                        }
                    }

                    container.addView(adLabel)

                    val contentRow = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val icon = ImageView(context).apply {
                        id = View.generateViewId()
                        layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                            rightMargin = 14
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    adView.iconView = icon
                    contentRow.addView(icon)

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
                        textSize = 14f
                        maxLines = 1
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }

                    adView.headlineView = headline
                    textColumn.addView(headline)

                    val body = TextView(context).apply {
                        id = View.generateViewId()
                        setTextColor(secondaryTextColor)
                        textSize = 12f
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 2
                        }
                    }

                    adView.bodyView = body
                    textColumn.addView(body)

                    contentRow.addView(textColumn)

                    val cta = Button(context).apply {
                        id = View.generateViewId()
                        setTextColor(0xFFFFFFFF.toInt())
                        textSize = 12f
                        setTypeface(null, Typeface.BOLD)
                        isAllCaps = false
                        minHeight = 0
                        minimumHeight = 0
                        minWidth = 0
                        minimumWidth = 0
                        setPadding(12, 5, 12, 5)

                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            setColor(accentColor)
                            cornerRadius = 12f
                        }

                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            leftMargin = 8
                        }
                    }

                    adView.callToActionView = cta
                    contentRow.addView(cta)

                    container.addView(contentRow)
                    addView(container)
                }
            },
            update = { nativeAdView ->
                nativeAdView.setNativeAd(ad)

                val headlineView = nativeAdView.headlineView as TextView
                val bodyView = nativeAdView.bodyView as TextView
                val iconView = nativeAdView.iconView as ImageView
                val ctaView = nativeAdView.callToActionView as Button

                headlineView.text = ad.headline

                if (!ad.body.isNullOrBlank()) {
                    bodyView.text = ad.body
                    bodyView.visibility = View.VISIBLE
                } else {
                    bodyView.visibility = View.GONE
                }

                if (!ad.callToAction.isNullOrBlank()) {
                    ctaView.text = ad.callToAction
                    ctaView.visibility = View.VISIBLE
                } else {
                    ctaView.visibility = View.GONE
                }

                ad.icon?.let {
                    iconView.setImageDrawable(it.drawable)
                    iconView.visibility = View.VISIBLE
                } ?: run {
                    iconView.visibility = View.GONE
                }
            },
            modifier = modifier
        )
    }
}