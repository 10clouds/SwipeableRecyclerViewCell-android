package com.tenclouds.swipeablerecyclerviewcell.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.util.DisplayMetrics
import android.view.View
import com.tenclouds.swipeablerecyclerviewcell.metaball.ConnectorHolder


internal fun pxToDp(px: Int, context: Context): Int =
        (px / (context.resources.displayMetrics.densityDpi.toFloat()
                / DisplayMetrics.DENSITY_DEFAULT)).toInt()

internal fun View.getCenter() =
        Point(left + measuredWidth / 2f, top + measuredHeight / 2f)

internal fun View.padding(paddingInPx: Int) =
        this.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)


internal fun valueAnimatorOfFloat(
        vararg values: Float,
        updateListener: (Float) -> Unit = {},
        onAnimStart: () -> Unit = {},
        onAnimEnd: () -> Unit = {}): ValueAnimator = ValueAnimator.ofFloat(*values)
        .apply {
            addUpdateListener {
                updateListener.invoke(it.animatedValue as Float)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    onAnimEnd()
                    super.onAnimationEnd(animation)
                }

                override fun onAnimationStart(animation: Animator?) {
                    onAnimStart()
                    super.onAnimationStart(animation)
                }
            })
        }


internal fun Canvas.drawConnector(movementProgress: Float,
                                  connectorHolder: ConnectorHolder,
                                  connectorPaint: Paint) {
    //stop drawing connector when view is opened
    if (movementProgress > 0.95f) return

    with(connectorHolder) {
        with(Path()) {
            moveTo(p1a.x, p1a.y)
            // Curve 1
            cubicTo(
                    p1a.x + segment1.x,
                    p1a.y + segment1.y,
                    p2a.x + segment2.x,
                    p2a.y + segment2.y,
                    p2a.x,
                    p2a.y
            )

            // Line 1
            lineTo(p2b.x, p2b.y)

            // Curve 2
            cubicTo(
                    p2b.x + segment3.x,
                    p2b.y + segment3.y,
                    p1b.x + segment4.x,
                    p1b.y + segment4.y,
                    p1b.x,
                    p1b.y
            )

            // Line 2
            lineTo(p1a.x, p1a.y)
            close()

            drawPath(this, connectorPaint)
        }
    }
}

internal fun @receiver:ColorInt Int.blend(color: Int, ratio: Float): Int =
        ColorUtils.blendARGB(this, color, ratio)

