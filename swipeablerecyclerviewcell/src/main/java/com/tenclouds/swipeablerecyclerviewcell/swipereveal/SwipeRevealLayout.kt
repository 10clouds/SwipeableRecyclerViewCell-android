package com.tenclouds.swipeablerecyclerviewcell.swipereveal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.tenclouds.swipeablerecyclerviewcell.R
import com.tenclouds.swipeablerecyclerviewcell.metaball.MetaBalls
import com.tenclouds.swipeablerecyclerviewcell.metaball.NONE_VIEW_TO_DELETE
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OnDeleteListener
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OnIconClickListener
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OnSwipeListener
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OpenCloseListener

/**
 * Stripped down version of: https://github.com/chthai64/SwipeRevealLayout
 */

const val DRAG_EDGE_LEFT = 1
const val DRAG_EDGE_RIGHT = 2

private const val DEFAULT_MIN_FLING_VELOCITY = 300 // dp per second
private const val DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT = 1 // dp

class SwipeRevealLayout : ViewGroup, OnDeleteListener, OpenCloseListener {
    private lateinit var mainView: View
    private lateinit var secondaryView: View

    private val rectMainClose = Rect()
    private val rectMainOpen = Rect()
    private val rectSecClose = Rect()
    private val rectSecOpen = Rect()

    private var minDistRequestDisallowParent = 0
    private var isOpenBeforeInit = false
    @Volatile
    private var isScrolling = false
    @Volatile
    private var lockDrag = false
    private var minFlingVelocity = DEFAULT_MIN_FLING_VELOCITY

    private var dragEdge = DRAG_EDGE_LEFT
    private var leftIconRes = 0
    private var rightIconRes = 0
    private var revealedLeftViewColor = 0
    private var revealedRightViewColor = 0
    private var revealedConnectorViewColor = 0

    private var revealedViewBackground = 0
    private var distanceBetweenIcons = 0
    private var iconPadding = 0
    private var revealedIconsSize = 0
    private var revealedMarginStart = 0
    private var revealedMarginEnd = 0

    private var dragDist = 0f
    private var prevX = -1f

    private lateinit var dragHelper: ViewDragHelper
    private lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var metaBalls: MetaBalls
    private lateinit var gestureListener: GestureDetector.SimpleOnGestureListener

    private var onSwipeListener: OnSwipeListener? = null
    internal var onIconClickListener: OnIconClickListener? = null


    fun setOnSwipeListener(l: OnSwipeListener) {
        onSwipeListener = l
    }

    fun setOnSwipeListener(
            onOpened: () -> Unit = {},
            onClosed: () -> Unit = {},
            onSlide: (Float) -> Unit = {}) {
        setOnSwipeListener(object : OnSwipeListener {
            override fun onClosed() = onOpened()
            override fun onOpened() = onClosed()
            override fun slide(progress: Float) = onSlide(progress)
        })
    }

    fun setOnIconClickListener(l: OnIconClickListener, sideToDelete: Int = NONE_VIEW_TO_DELETE) {
        onIconClickListener = l
        metaBalls.deleteView = sideToDelete
    }

    fun setOnIconClickListener(
            onLeftIconClick: () -> Unit = {},
            onRightIconClick: () -> Unit = {},
            sideToDelete: Int = NONE_VIEW_TO_DELETE
    ) {
        setOnIconClickListener(object : OnIconClickListener {
            override fun onLeftIconClick() = onLeftIconClick()
            override fun onRightIconClick() = onRightIconClick()
        }, sideToDelete)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.SwipeRevealLayout,
                    0, 0
            )

            dragEdge = a.getInteger(R.styleable.SwipeRevealLayout_dragFromEdge,
                    DRAG_EDGE_LEFT)
            leftIconRes = a.getResourceId(R.styleable.SwipeRevealLayout_leftIcon,
                    R.drawable.ic_fav)
            rightIconRes = a.getResourceId(R.styleable.SwipeRevealLayout_rightIcon,
                    R.drawable.ic_delete)

            revealedConnectorViewColor = a.getColor(R.styleable.SwipeRevealLayout_connectorColor,
                    ContextCompat.getColor(context, R.color.redDelete))
            revealedLeftViewColor = a.getColor(R.styleable.SwipeRevealLayout_leftIconBgColor,
                    ContextCompat.getColor(context, R.color.greyFavourite))
            revealedRightViewColor = a.getColor(R.styleable.SwipeRevealLayout_rightIconBgColor,
                    ContextCompat.getColor(context, R.color.redDelete))
            revealedViewBackground = a.getResourceId(
                    R.styleable.SwipeRevealLayout_revealedViewBackground,
                    android.R.color.transparent
            )

            distanceBetweenIcons = a.getDimensionPixelSize(
                    R.styleable.SwipeRevealLayout_iconsDistance, resources.getDimensionPixelSize(R.dimen.default_icons_distance))
            iconPadding = a.getDimensionPixelSize(R.styleable.SwipeRevealLayout_iconsPadding, resources.getDimensionPixelSize(R.dimen.default_icons_padding))
            revealedIconsSize = a.getDimensionPixelSize(R.styleable.SwipeRevealLayout_iconsSize, resources.getDimensionPixelSize(R.dimen.default_icons_size))
            revealedMarginStart = a.getDimensionPixelSize(
                    R.styleable.SwipeRevealLayout_revealedViewMarginStart, resources.getDimensionPixelSize(R.dimen.default_icons_margin))
            revealedMarginEnd = a.getDimensionPixelSize(
                    R.styleable.SwipeRevealLayout_revealedViewMarginEnd, resources.getDimensionPixelSize(R.dimen.default_icons_margin))
        }

        minFlingVelocity = DEFAULT_MIN_FLING_VELOCITY
        minDistRequestDisallowParent = DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT

        addMetaballsView()
    }

    private fun addMetaballsView() {
        metaBalls = MetaBalls(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                    WRAP_CONTENT,
                    MATCH_PARENT
            )

            rightViewColor = revealedRightViewColor
            leftViewColor = revealedLeftViewColor
            connectorColor = revealedConnectorViewColor

            leftIconResId = leftIconRes
            rightIconResId = rightIconRes

            setBackgroundResource(revealedViewBackground)

            configureIconsView(
                    revealedMarginStart,
                    revealedMarginEnd,
                    distanceBetweenIcons,
                    revealedIconsSize,
                    iconPadding
            )
        }

        if (getChildAt(0) != metaBalls) {
            addView(metaBalls, 0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        dragHelper.processTouchEvent(event)
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isDragLocked()) {
            return super.onInterceptTouchEvent(ev)
        }

        dragHelper.processTouchEvent(ev)
        gestureDetector.onTouchEvent(ev)
        accumulateDragDist(ev)

        val couldBecomeClick = couldBecomeClick(ev)
        val settling = dragHelper.viewDragState == ViewDragHelper.STATE_SETTLING
        val idleAfterScrolled = dragHelper.viewDragState == ViewDragHelper.STATE_IDLE && isScrolling

        // must be placed as the last statement
        prevX = ev.x

        // return true => intercept, cannot trigger onClick event
        return !couldBecomeClick && (settling || idleAfterScrolled)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //Has to be called after attaching to the view (parent needed), that's why in onAttachedToWindow
        initGestureDetector()
        initDragHelper()
    }

    private fun initGestureDetector() {
        gestureListener = SwipeSimpleGestureListener(
                parent = parent,
                minDistRequestDisallowParent = minDistRequestDisallowParent,
                isScrolling = {
                    isScrolling = it
                    isScrolling
                },
                distToClosestEdge = { getDistToClosestEdge() }
        )
        gestureDetector = GestureDetectorCompat(context, gestureListener)
    }

    private fun initDragHelper() {
        val dragHelperCallback = DragHelperCallback(
                lockDrag = { lockDrag },
                openCloseListener = this,
                onSwipeListener = onSwipeListener,
                secondaryView = secondaryView,
                mainView = mainView,
                dragEdge = dragEdge,
                rectMainClose = rectMainClose,
                rectMainOpen = rectMainOpen,
                halfwayPivotHorizontal = getHalfwayPivotHorizontal(),
                minFlingVelocity = minFlingVelocity
        )
        dragHelper = ViewDragHelper.create(this, 1.0f, dragHelperCallback)
        dragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        // get views
        if (childCount >= 2) {
            secondaryView = getChildAt(0)
            mainView = getChildAt(1)
        } else if (childCount == 1) {
            mainView = getChildAt(0)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (index in 0 until childCount) {
            val child = getChildAt(index)

            var left: Int
            var right: Int
            var top: Int
            var bottom: Int
            bottom = 0
            top = bottom
            right = top
            left = right

            val minLeft = paddingLeft
            val maxRight = Math.max(r - paddingRight - l, 0)
            val minTop = paddingTop
            val maxBottom = Math.max(b - paddingBottom - t, 0)

            var measuredChildHeight = child.measuredHeight
            var measuredChildWidth = child.measuredWidth

            // need to take account if child size is match_parent
            val childParams = child.layoutParams
            var matchParentHeight = false
            var matchParentWidth = false

            if (childParams != null) {
                matchParentHeight = childParams.height == ViewGroup.LayoutParams.MATCH_PARENT ||
                        childParams.height == ViewGroup.LayoutParams.MATCH_PARENT
                matchParentWidth = childParams.width == ViewGroup.LayoutParams.MATCH_PARENT ||
                        childParams.width == ViewGroup.LayoutParams.MATCH_PARENT
            }

            if (matchParentHeight) {
                measuredChildHeight = maxBottom - minTop
                childParams!!.height = measuredChildHeight
            }

            if (matchParentWidth) {
                measuredChildWidth = maxRight - minLeft
                childParams!!.width = measuredChildWidth
            }

            when (dragEdge) {
                DRAG_EDGE_RIGHT -> {
                    left = Math.max(r - measuredChildWidth - paddingRight - l, minLeft)
                    top = Math.min(paddingTop, maxBottom)
                    right = Math.max(r - paddingRight - l, minLeft)
                    bottom = Math.min(measuredChildHeight + paddingTop, maxBottom)
                }

                DRAG_EDGE_LEFT -> {
                    left = Math.min(paddingLeft, maxRight)
                    top = Math.min(paddingTop, maxBottom)
                    right = Math.min(measuredChildWidth + paddingLeft, maxRight)
                    bottom = Math.min(measuredChildHeight + paddingTop, maxBottom)
                }
            }

            child.layout(left, top, right, bottom)
        }

        when (dragEdge) {
            DRAG_EDGE_LEFT -> secondaryView.offsetLeftAndRight(-secondaryView.width)

            DRAG_EDGE_RIGHT -> secondaryView.offsetLeftAndRight(secondaryView.width)
        }

        initRects()

        if (isOpenBeforeInit) {
            open(false)
        } else {
            close(false)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var currentWidthMeasureSpec = widthMeasureSpec
        var currentMeasureSpec = heightMeasureSpec

        //Secondary view is added from code (metaballs)
        if (childCount != 2) {
            throw RuntimeException("Only one child view is allowed")
        }

        val params = layoutParams

        val widthMode = View.MeasureSpec.getMode(currentWidthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(currentMeasureSpec)

        var desiredWidth = 0
        var desiredHeight = 0

        // first find the largest child
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, currentWidthMeasureSpec, currentMeasureSpec)
            desiredWidth = Math.max(child.measuredWidth, desiredWidth)
            desiredHeight = Math.max(child.measuredHeight, desiredHeight)
        }
        // create new measure spec using the largest child width
        currentWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(desiredWidth, widthMode)
        currentMeasureSpec = View.MeasureSpec.makeMeasureSpec(desiredHeight, heightMode)

        val measuredWidth = View.MeasureSpec.getSize(currentWidthMeasureSpec)
        val measuredHeight = View.MeasureSpec.getSize(currentMeasureSpec)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childParams = child.layoutParams

            if (childParams != null) {
                if (childParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.minimumHeight = measuredHeight
                }

                if (childParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.minimumWidth = measuredWidth
                }
            }

            measureChild(child, currentWidthMeasureSpec, currentMeasureSpec)
            desiredWidth = Math.max(child.measuredWidth, desiredWidth)
            desiredHeight = Math.max(child.measuredHeight, desiredHeight)
        }

        // taking accounts of padding
        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        // adjust desired width
        if (widthMode == View.MeasureSpec.EXACTLY) {
            desiredWidth = measuredWidth
        } else {
            if (params.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                desiredWidth = measuredWidth
            }

            if (widthMode == View.MeasureSpec.AT_MOST) {
                desiredWidth = if (desiredWidth > measuredWidth) measuredWidth else desiredWidth
            }
        }

        // adjust desired height
        if (heightMode == View.MeasureSpec.EXACTLY) {
            desiredHeight = measuredHeight
        } else {
            if (params.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                desiredHeight = measuredHeight
            }

            if (heightMode == View.MeasureSpec.AT_MOST) {
                desiredHeight = if (desiredHeight > measuredHeight) measuredHeight else desiredHeight
            }
        }

        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun setOpened(opened: Boolean) {
        isOpenBeforeInit = opened
        if (::dragHelper.isInitialized.not()) return
        if (opened) {
            open(false)
        } else {
            close(false)
        }
    }

    /**
     * Open the panel to show the secondary view
     */
    override fun open(animate: Boolean) {
        isOpenBeforeInit = true

        if (animate) {
            dragHelper.smoothSlideViewTo(mainView, rectMainOpen.left, rectMainOpen.top)
        } else {
            dragHelper.abort()

            mainView.layout(
                    rectMainOpen.left,
                    rectMainOpen.top,
                    rectMainOpen.right,
                    rectMainOpen.bottom
            )
            secondaryView.layout(
                    rectSecOpen.left,
                    rectSecOpen.top,
                    rectSecOpen.right,
                    rectSecOpen.bottom
            )
        }

        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * Close the panel to hide the secondary view
     */
    override fun close(animate: Boolean) {
        isOpenBeforeInit = false

        if (animate) {
            dragHelper.smoothSlideViewTo(mainView, rectMainClose.left, rectMainClose.top)
        } else {
            dragHelper.abort()
            mainView.layout(
                    rectMainClose.left,
                    rectMainClose.top,
                    rectMainClose.right,
                    rectMainClose.bottom
            )
            secondaryView.layout(
                    rectSecClose.left,
                    rectSecClose.top,
                    rectSecClose.right,
                    rectSecClose.bottom
            )
        }

        ViewCompat.postInvalidateOnAnimation(this)
    }

    private fun animateDeleting() {
        secondaryView.animate()
                .translationX(-rectMainClose.right.toFloat())
                .setDuration(300)
                .start()

        mainView.animate()
                .translationX(-rectMainClose.right.toFloat())
                .setDuration(300)
                .start()
    }

    /**
     * @return true if the drag/swipe motion is currently locked.
     */
    fun isDragLocked(): Boolean {
        return lockDrag
    }

    private fun getMainOpenLeft(): Int {
        return when (dragEdge) {
            DRAG_EDGE_LEFT -> rectMainClose.left + secondaryView.width

            DRAG_EDGE_RIGHT -> rectMainClose.left - secondaryView.width

            else -> 0
        }
    }

    private fun getMainOpenTop(): Int {
        return when (dragEdge) {
            DRAG_EDGE_LEFT -> rectMainClose.top

            DRAG_EDGE_RIGHT -> rectMainClose.top

            else -> 0
        }
    }

    private fun getSecOpenLeft(): Int {
        return if (dragEdge == DRAG_EDGE_LEFT) {
            rectSecClose.left + secondaryView.width
        } else {
            rectSecClose.left - secondaryView.width
        }
    }

    private fun getSecOpenTop(): Int {
        return rectSecClose.top
    }

    private fun initRects() {
        // close position of main view
        rectMainClose.set(
                mainView.left,
                mainView.top,
                mainView.right,
                mainView.bottom
        )

        // close position of secondary view
        rectSecClose.set(
                secondaryView.left,
                secondaryView.top,
                secondaryView.right,
                secondaryView.bottom
        )

        // open position of the main view
        rectMainOpen.set(
                getMainOpenLeft(),
                getMainOpenTop(),
                getMainOpenLeft() + mainView.width,
                getMainOpenTop() + mainView.height
        )

        // open position of the secondary view
        rectSecOpen.set(
                getSecOpenLeft(),
                getSecOpenTop(),
                getSecOpenLeft() + secondaryView.width,
                getSecOpenTop() + secondaryView.height
        )
    }

    private fun couldBecomeClick(ev: MotionEvent): Boolean {
        return isInMainView(ev) && !shouldInitiateADrag()
    }

    private fun isInMainView(ev: MotionEvent): Boolean {
        val x = ev.x
        val y = ev.y

        val withinVertical = mainView.top <= y && y <= mainView.bottom
        val withinHorizontal = mainView.left <= x && x <= mainView.right

        return withinVertical && withinHorizontal
    }

    private fun shouldInitiateADrag(): Boolean {
        val minDistToInitiateDrag = dragHelper.touchSlop.toFloat()
        return dragDist >= minDistToInitiateDrag
    }

    private fun accumulateDragDist(ev: MotionEvent) {
        val action = ev.action
        if (action == MotionEvent.ACTION_DOWN) {
            dragDist = 0f
            return
        }

        val dragged = Math.abs(ev.x - prevX)

        dragDist += dragged
    }

    override fun deleted() {
        animateDeleting()
    }

    private fun getDistToClosestEdge(): Int {
        when (dragEdge) {
            DRAG_EDGE_LEFT -> {
                val pivotRight = rectMainClose.left + secondaryView.width

                return Math.min(
                        mainView.left - rectMainClose.left,
                        pivotRight - mainView.left
                )
            }

            DRAG_EDGE_RIGHT -> {
                val pivotLeft = rectMainClose.right - secondaryView.width

                return Math.min(
                        mainView.right - pivotLeft,
                        rectMainClose.right - mainView.right
                )
            }
        }

        return 0
    }

    private fun getHalfwayPivotHorizontal(): Int {
        return if (dragEdge == DRAG_EDGE_LEFT) {
            rectMainClose.left + secondaryView.width / 2
        } else {
            rectMainClose.right - secondaryView.width / 2
        }
    }
}