package com.tenclouds.swipeablerecyclerviewcell.metaball

import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.tenclouds.swipeablerecyclerviewcell.R
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.SwipeRevealLayout
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.AnimatedRevealView
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OnDeleteListener
import com.tenclouds.swipeablerecyclerviewcell.utils.*
import kotlin.math.*
import kotlin.properties.Delegates

const val LEFT_VIEW_TO_DELETE = 1
const val RIGHT_VIEW_TO_DELETE = 2
const val NONE_VIEW_TO_DELETE = 0

internal class MetaBalls : LinearLayout, AnimatedRevealView {

    private val calculatedSelectorRadius by lazy {
        max(centerView.width, centerView.height).div(2f)
    }

    private var destinationPoint: Point = Point()
    private var originPoint: Point = Point()

    private val startWhenProgress = 0.8f

    private var startingOriginX: Float = 0f
    private var startingRevealedParentX: Float = 0f

    private lateinit var centerView: ImageView

    private var blobConnectorData: ConnectorHolder? = null
    private var transitionDistance = 0.0f
    private var centerCircle = Circle()

    private val maxViewScale = 1.2f

    var endViewColor: Int = R.color.redDelete

    var deleteView = NONE_VIEW_TO_DELETE

    private val connectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    var viewColor: Int by Delegates.observable(
            ContextCompat.getColor(context, R.color.greyFavourite))
    { _, _, new -> centerCircle.paint.color = new }

    var connectorColor: Int by Delegates.observable(
            ContextCompat.getColor(context, R.color.greyFavourite))
    { _, _, new -> connectorPaint.color = new }

    var iconResId: Int by Delegates.observable(
            R.drawable.ic_delete)
    { _, _, new -> centerView.setImageResource(new) }

    private var movementProgress = 0f
        set(value) {
            val startWhenProgress = 0.6f
            val diffRangeLimitOne = 1 - startWhenProgress
            field =
                    if (value in 0.0f..startWhenProgress) 0f
                    else (value - startWhenProgress) * 1.div(diffRangeLimitOne)
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        gravity = Gravity.CENTER
        orientation = LinearLayout.HORIZONTAL

        addRevealedViews()
    }

    fun configureIconsView(iconsMarginStart: Int,
                           iconsMarginEnd: Int,
                           iconsSize: Int,
                           iconsPadding: Int) {
        val minMargin = iconsSize * maxViewScale - iconsSize
        val startMargin = max(minMargin.toInt(), iconsMarginStart)
        val endMargin = max(minMargin.toInt(), iconsMarginEnd)

        val rightLp = LinearLayout.LayoutParams(iconsSize, iconsSize)
                .apply { setMargins(startMargin, 0, endMargin, 0) }

        centerView.apply {
            layoutParams = rightLp
            padding(iconsPadding)
            id = generateViewId()
        }
    }

    private fun addRevealedViews() {
        centerView = ImageView(context)
        addView(centerView)

        configureClickListeners()
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        post {
            initValues()
            reveal(1f)
        }
        super.onLayout(changed, l, t, r, b)
    }

    private fun initValues() {
        originPoint = centerView.getCenter()
//        destinationPoint = leftView.getCenter()
        transitionDistance = destinationPoint.x - originPoint.x
        startingOriginX = originPoint.x
        startingRevealedParentX = 0f
    }

    private fun clickAnimator(circle: Circle, viewColor: Int) =
            valueAnimatorOfFloat(1f, maxViewScale, 1f,
                    updateListener = {
                        circle.radius = calculatedSelectorRadius * it
                        circle.paint.color = viewColor.blend(Color.WHITE, it - 1f)
                        invalidate()
                    }).setDuration(300)

    private fun deleteAnimator() =
            valueAnimatorOfFloat(0f, 1f,
                    updateListener = {
                        calculateValuesForDeleteAnimation(it, startingOriginX)
                        invalidate()
                    },
                    onAnimEnd = { resetViewState() },
                    onAnimStart = { (parent as? OnDeleteListener)?.deleted() })
                    .setDuration(300)


    private fun configureClickListeners() {
        centerView.setOnClickListener {
            var delay = 0L
            if (deleteView == RIGHT_VIEW_TO_DELETE) {
                deleteAnimation(centerCircle, viewColor)
                delay = 300L
            } else {
                clickAnimation(centerCircle, viewColor)
            }
            postDelayed({
                (parent as? SwipeRevealLayout)?.onIconClickListener?.onRightIconClick()
            }, delay)
        }
    }

    private fun resetViewState() {
        originPoint.x = startingOriginX
        calculateViewPosition(centerView, originPoint)
    }

    private fun deleteAnimation(circle: Circle, color: Int) {
        AnimatorSet()
                .apply {
                    playSequentially(
                            clickAnimator(circle, color),
                            deleteAnimator()
                    )
                }
                .start()
    }

    private fun clickAnimation(circle: Circle, color: Int) {
        clickAnimator(circle, color)
                .start()
    }

    override fun reveal(howMuchToReveal: Float) {
        movementProgress = howMuchToReveal

        calculateValuesDependingOnMovementProgress(movementProgress)

        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        /*Log.d("MetaBalls", "originPoint x" + originPoint.x)
        Log.d("MetaBalls", "originPoint y" + originPoint.y)
        Log.d("MetaBalls", "centerCircle.radius" + centerCircle.radius)
        Log.d("MetaBalls", "centerCircle.paint" + centerCircle.paint)*/
        canvas.drawCircle(
                originPoint.x,
                originPoint.y,
                centerCircle.radius,
                centerCircle.paint
        )

        /*blobConnectorData?.let {
            canvas.drawConnector(
                    movementProgress,
                    it,
                    connectorPaint
            )
        }

        canvas.drawCircle(
                destinationPoint.x,
                destinationPoint.y,
                leftCircle.radius,
                leftCircle.paint
        )*/

        super.dispatchDraw(canvas)
    }

    private fun calculateValuesDependingOnMovementProgress(progress: Float) {
        /*leftCircle.radius = getRadiusDependingOnViewPosition(progress)*/
        val radius = getRadiusDependingOnViewPosition(progress)
        Log.d("radius", radius.toString())
        centerCircle.radius = radius

        destinationPoint.x = originPoint.x + transitionDistance * progress

        connectorPaint.alpha = connectorPaintAlpha(progress)

/*        blobConnectorData = calculateBlobConnector(leftCircle.radius, originPoint, destinationPoint)*/

/*        calculateViewPosition(leftView, destinationPoint)

        calculateViewScale(progress, leftView)*/
        calculateViewScale(progress, centerView)
    }

    private fun calculateValuesForDeleteAnimation(progress: Float, startingX: Float) {
        originPoint.x = startingX + transitionDistance * progress
        connectorPaint.alpha = connectorPaintAlpha(progress)
        movementProgress = progress
        /*blobConnectorData = calculateBlobConnector(leftCircle.radius, originPoint, destinationPoint)*/

        calculateViewPosition(centerView, originPoint)
    }

    private fun getRadiusDependingOnViewPosition(progress: Float): Float {
//        Log.d("progress", progress.toString())
        return if (movementProgress < startWhenProgress) {
            //max 1.6f
            val scale = (0.8f + (abs(progress - startWhenProgress)))
            Log.d("start", scale.toString())
            calculatedSelectorRadius * scale
        } else {
            Log.d("end", calculatedSelectorRadius.toString())
            calculatedSelectorRadius
        }
    }

    private fun calculateViewPosition(view: View, destination: Point) {
        with(view) {
            x = destination.x - measuredWidth / 2
            y = destination.y - measuredHeight / 2
        }
    }

    private fun calculateViewScale(progress: Float, view: View) {
        with(view) {
            scaleY = progress
            scaleX = progress
        }
    }

    private fun connectorPaintAlpha(progress: Float): Int {
        val startWhenProgress = 0.85f
        val diffRangeLimitOne = 1 - startWhenProgress
        return if (progress in 0.0f..startWhenProgress) 255
        else 255 - ((progress - startWhenProgress) * 1.div(diffRangeLimitOne) * 255).toInt()
    }

    private fun calculateBlobConnector(originRadius: Float, origin: Point, destination: Point)
            : ConnectorHolder {
        val v = 0.4f
        val handleLenRate = 2.4f
        val distanceBetweenCircles = getDistance(origin, destination)

        // Get the radius sum
        val radiusSum = originRadius * 2

        val arc = if (distanceBetweenCircles < radiusSum) {
            acos((originRadius * originRadius + distanceBetweenCircles * distanceBetweenCircles
                    - originRadius * originRadius)
                    / (2f * originRadius * distanceBetweenCircles))
        } else {
            0.0f
        }

        // Get the difference of the two centres
        val diffPoint = Point(
                destination.x - origin.x,
                destination.y - origin.y
        )

        val angle1 = atan2(diffPoint.y, diffPoint.x)
        val angle2 = acos((originRadius - originRadius) / distanceBetweenCircles)

        val angle1a = angle1 + arc + (angle2 - arc) * v
        val angle1b = angle1 - arc - (angle2 - arc) * v
        val angle2a = (angle1 + Math.PI - arc - (Math.PI - arc - angle2) * v).toFloat()
        val angle2b = (angle1 - Math.PI + arc + (Math.PI - arc - angle2) * v).toFloat()

        val p1aTemp = getVectorFrom(angle1a, originRadius)
        val p1bTemp = getVectorFrom(angle1b, originRadius)
        val p2aTemp = getVectorFrom(angle2a, originRadius)
        val p2bTemp = getVectorFrom(angle2b, originRadius)

        val p1a = Point(p1aTemp.x + origin.x, p1aTemp.y + origin.y)
        val p1b = Point(p1bTemp.x + origin.x, p1bTemp.y + origin.y)
        val p2a = Point(p2aTemp.x + destination.x, p2aTemp.y + destination.y)
        val p2b = Point(p2bTemp.x + destination.x, p2bTemp.y + destination.y)
        // Define handle length by the distance between both ends of the curve to draw
        val diffp1p2 = Point(p1a.x - p2a.x, p1a.y - p2a.y)

        val minDist = min(v * handleLenRate, getVectorLength(diffp1p2.x, diffp1p2.y) / radiusSum)
        val radius = originRadius * minDist
        val pi2 = (PI / 2).toFloat()

        val segment1 = getVectorFrom(angle1a - pi2, radius)
        val segment2 = getVectorFrom(angle2a + pi2, radius)
        val segment3 = getVectorFrom(angle2b - pi2, radius)
        val segment4 = getVectorFrom(angle1b + pi2, radius)

        return ConnectorHolder(p1a, p2a, p1b, p2b, segment1, segment2, segment3, segment4)
    }

}
