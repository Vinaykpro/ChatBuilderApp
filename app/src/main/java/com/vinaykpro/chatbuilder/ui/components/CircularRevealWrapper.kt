package com.vinaykpro.chatbuilder.ui.components

import android.animation.Animator
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.hypot

@Composable
fun CircularRevealWrapper(
    modifier: Modifier = Modifier,
    centerX: Int = -1,
    centerY: Int = -1,
    triggerAnimation: Boolean = false,
    isDark: Boolean = false,
    onAnimationStart: () -> Unit = {},
    onAnimationEnd: () -> Unit = {},
    content: @Composable () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FrameLayout(ctx).apply {
                val composeView = androidx.compose.ui.platform.ComposeView(ctx).apply {
                    setContent { content() }
                }
                addView(composeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        },
        update = { frameLayout ->
            frameLayout.alpha = 0f
            if(isDark) frameLayout.alpha = 1f
            if (triggerAnimation) {
                frameLayout.post {
                    frameLayout.alpha = 1f
                    if (frameLayout.isAttachedToWindow) {
                        val cx = if (centerX >= 0) centerX else frameLayout.width / 2
                        val cy = if (centerY >= 0) centerY else frameLayout.height / 2
                        val finalRadius = hypot(frameLayout.height.toDouble(), frameLayout.width.toDouble()).toFloat()

                        val anim = if(isDark) ViewAnimationUtils.createCircularReveal(frameLayout, cx, cy, finalRadius,0f)
                                   else ViewAnimationUtils.createCircularReveal(frameLayout, cx, cy,0f, finalRadius)

                        anim.duration = 800
                        anim.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                            }
                            override fun onAnimationEnd(animation: Animator) {
                                if(!isDark) {
                                    onAnimationStart()
                                }
                                onAnimationEnd()
                            }
                            override fun onAnimationCancel(animation: Animator) {}
                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                        anim.start()
                    }
                }
            }
        }

    )
}
