package com.tenclouds.swipeablerecyclerviewcell.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.util.DisplayMetrics
import android.view.View
import com.tenclouds.swipeablerecyclerviewcell.metaball.ConnectorHolder
import java.util.concurrent.atomic.AtomicInteger


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
            moveTo(topBezierStart.x, topBezierStart.y)
            // Curve 1
            cubicTo(
                    topBezierFirstControlPoint.x,
                    topBezierFirstControlPoint.y,
                    topBezierSecondControlPoint.x,
                    topBezierSecondControlPoint.y,
                    topBezierEnd.x,
                    topBezierEnd.y
            )

            // Line 1
            lineTo(bottomBezierEnd.x, bottomBezierEnd.y)

            // Curve 2
            cubicTo(
                    bottomBezierFirstControlPoint.x,
                    bottomBezierFirstControlPoint.y,
                    bottomBezierSecondControlPoint.x,
                    bottomBezierSecondControlPoint.y,
                    bottomBezierStart.x,
                    bottomBezierStart.y
            )

            // Line 2
            lineTo(topBezierStart.x, topBezierStart.y)
            close()

            drawPath(this, connectorPaint)
        }
    }
}

internal fun @receiver:ColorInt Int.blend(color: Int, ratio: Float): Int =
        ColorUtils.blendARGB(this, color, ratio)

private val nextGeneratedId = AtomicInteger(1)
fun View.generateViewId(): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
        return View.generateViewId()
    } else {
        while (true) {
            val result = nextGeneratedId.get()
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            var newValue = result + 1
            if (newValue > 0x00FFFFFF) newValue = 1 // Roll over to 1, not 0.
            if (nextGeneratedId.compareAndSet(result, newValue)) {
                return result
            }
        }
    }
}