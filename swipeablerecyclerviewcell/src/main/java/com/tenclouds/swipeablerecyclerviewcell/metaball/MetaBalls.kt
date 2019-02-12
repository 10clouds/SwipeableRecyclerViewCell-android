package com.tenclouds.swipeablerecyclerviewcell.metaball

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import com.tenclouds.swipeablerecyclerviewcell.R
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.DRAG_EDGE_LEFT
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.DRAG_EDGE_RIGHT
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

    //TODO 12.02.2019 Dawid Jamroży remove unused properties

    private val calculatedSelectorRadius by lazy {
        max(centerView.width, centerView.height).div(2f)
    }

    private val secondCircleRadiusScale by lazy { calculatedSelectorRadius * 0.3f }
    private val secondCircleTranslationScale by lazy { calculatedSelectorRadius * 0.9f }

    private var destinationPoint: Point = Point()
    private var originPoint: Point = Point()

    private val startWhenProgress = 0.5f
    private val pauseTillProgress = 0.65f

    private var startingOriginX: Float = 0f
    private var startingRevealedParentX: Float = 0f

    companion object {
        private const val END_ANIMATE_MAX_VALUE = 40f
    }

    private lateinit var centerView: ImageView

    private var secondCircleRadius = 0f
    private var secondCircleTranslation = 0f
    private var transitionDistance = 0.0f
    private var centerCircle = Circle()

    private val maxViewScale = 1.2f

    var endViewColor = ContextCompat.getColor(context, R.color.redDelete)
    var startViewColor = ContextCompat.getColor(context, R.color.greyFavourite)

    var deleteView = NONE_VIEW_TO_DELETE

    var dragFromEdge = DRAG_EDGE_LEFT

    var viewColor: Int by Delegates.observable(
            ContextCompat.getColor(context, R.color.greyFavourite))
    { _, _, new -> centerCircle.paint.color = new }

    var iconResId: Int by Delegates.observable(R.drawable.ic_delete)
    { _, _, new -> centerView.setImageResource(new) }

    private var movementProgress = 0f
        set(value) {
            val startWhenProgress = 0.4f
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
        val rightLp = LinearLayout.LayoutParams(iconsSize, iconsSize)
                .apply { setMargins(iconsMarginStart, 0, iconsMarginEnd, 0) }

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

        if (howMuchToReveal in 0f..0.6f) {
            centerView.invisible()
        } else {
            centerView.visible()
        }

        // change to start color
        if (howMuchToReveal != 1f && viewColor != startViewColor)
            viewColor = startViewColor

        calculateValuesDependingOnMovementProgress(movementProgress)
        calculateSecondCircleTranslationDependingOnMovementProgress(movementProgress)
        calculateSecondCircleRadiusDependingOnMovementProgress(movementProgress)

        invalidate()
    }

    override fun opened() {
        viewColor = endViewColor
        val centerCircleRadius = centerCircle.radius

        ValueAnimator.ofFloat(0f, END_ANIMATE_MAX_VALUE)
                .apply {
                    repeatMode = ValueAnimator.REVERSE
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { updatedAnimation ->
                        val i = updatedAnimation.animatedValue as Float

                        centerCircle.radius = if (i in 0f..END_ANIMATE_MAX_VALUE.div(2)) {
                            centerCircleRadius + i
                        } else {
                            centerCircleRadius + END_ANIMATE_MAX_VALUE.minus(i)
                        }

                        invalidate()
                    }
                    start()
                }

        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        // main circle with icon
        canvas.drawCircle(
                originPoint.x,
                originPoint.y,
                centerCircle.radius,
                centerCircle.paint
        )

        // second smaller circle
        canvas.drawCircle(
                originPoint.x - secondCircleTranslation,
                originPoint.y,
                centerCircle.radius - secondCircleRadius,
                centerCircle.paint
        )

        super.dispatchDraw(canvas)
    }

    private fun calculateSecondCircleRadiusDependingOnMovementProgress(progress: Float) {
        secondCircleRadius = if (movementProgress < startWhenProgress) {
            progress.times(secondCircleRadiusScale)
        } else {
            1.minus(progress).times(secondCircleRadiusScale)
        }
    }

    private fun calculateSecondCircleTranslationDependingOnMovementProgress(progress: Float) {
        val circleTranslation = if (movementProgress < startWhenProgress) {
            progress.times(secondCircleTranslationScale)
        } else {
            1.minus(progress).times(secondCircleTranslationScale)
        }

        secondCircleTranslation = when (dragFromEdge) {
            DRAG_EDGE_LEFT -> -circleTranslation
            DRAG_EDGE_RIGHT -> circleTranslation
            else -> throw IllegalArgumentException()
        }
    }

    private fun calculateValuesDependingOnMovementProgress(progress: Float) {
        centerCircle.radius = getRadiusDependingOnViewPosition(progress)
    }

    private fun calculateValuesForDeleteAnimation(progress: Float, startingX: Float) {
        originPoint.x = startingX + transitionDistance * progress
        movementProgress = progress

        calculateViewPosition(centerView, originPoint)
    }

    private var currentCanvasRadius = 0f

    private fun getRadiusDependingOnViewPosition(progress: Float): Float {
        return when (progress) {
            // should decrease radius
            in 0f..startWhenProgress -> {
                currentCanvasRadius = calculatedSelectorRadius - (progress * 50)
                currentCanvasRadius
            }
            // should keep same radius
            in startWhenProgress..pauseTillProgress -> currentCanvasRadius
            // should increase size
            else -> currentCanvasRadius + ((progress - pauseTillProgress) * 50)
        }
    }

    private fun calculateViewPosition(view: View, destination: Point) {
        with(view) {
            x = destination.x - measuredWidth / 2
            y = destination.y - measuredHeight / 2
        }
    }
}
