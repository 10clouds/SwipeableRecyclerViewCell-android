package com.tenclouds.swipeablerecyclerviewcell.swipereveal

import android.graphics.Rect
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.view.View
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.AnimatedRevealView
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OnSwipeListener
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OpenCloseListener
import com.tenclouds.swipeablerecyclerviewcell.utils.pxToDp
import kotlin.math.abs


internal class DragHelperCallback(
        var lockDrag: () -> Boolean,
        var mainView: View,
        var openCloseListener: OpenCloseListener?,
        var onSwipeListener: OnSwipeListener?,
        var secondaryView: View,
        var dragEdge: Int,
        var rectMainClose: Rect,
        var rectMainOpen: Rect,
        var halfwayPivotHorizontal: Int,
        var minFlingVelocity: Int

) : ViewDragHelper.Callback() {
    private var lastMainLeft = 0
    private var lastMainTop = 0

    init {
        lastMainLeft = mainView.left
        lastMainTop = mainView.top
    }

    override fun tryCaptureView(child: View, pointerId: Int): Boolean {
        if (lockDrag()) return false

        return child == mainView
    }

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
        return when (dragEdge) {
            DRAG_EDGE_RIGHT -> Math.max(
                    Math.min(left, rectMainClose.left),
                    rectMainClose.left - secondaryView.width
            )

            DRAG_EDGE_LEFT -> Math.max(
                    Math.min(left, rectMainClose.left + secondaryView.width),
                    rectMainClose.left
            )

            else -> child.left
        }
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
        val velRightExceeded = pxToDp(xvel.toInt(), releasedChild.context) >= minFlingVelocity
        val velLeftExceeded = pxToDp(xvel.toInt(), releasedChild.context) <= -minFlingVelocity

        val pivotHorizontal = halfwayPivotHorizontal

        when (dragEdge) {
            DRAG_EDGE_RIGHT -> if (velRightExceeded) {
                openCloseListener?.close(true)
            } else if (velLeftExceeded) {
                openCloseListener?.open(true)
            } else {
                if (mainView.right < pivotHorizontal) {
                    openCloseListener?.open(true)
                } else {
                    openCloseListener?.close(true)
                }
            }

            DRAG_EDGE_LEFT -> if (velRightExceeded) {
                openCloseListener?.open(true)
            } else if (velLeftExceeded) {
                openCloseListener?.close(true)
            } else {
                if (mainView.left < pivotHorizontal) {
                    openCloseListener?.close(true)
                } else {
                    openCloseListener?.open(true)
                }
            }
        }
    }

    override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
        if (lockDrag()) return
        super.onEdgeDragStarted(edgeFlags, pointerId)
    }

    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
        super.onViewPositionChanged(changedView, left, top, dx, dy)

        val isMoved = mainView.left != lastMainLeft || mainView.top != lastMainTop
        if (isMoved) {
            if (mainView.left == rectMainClose.left && mainView.top == rectMainClose.top) {
                onSwipeListener?.onClosed()
            } else if (mainView.left == rectMainOpen.left && mainView.top == rectMainOpen.top) {
                onSwipeListener?.onOpened()
            }

            val revealProgress = abs(left.toFloat() / secondaryView.width)
            //notify children view about sliding
            (secondaryView as? AnimatedRevealView)?.reveal(revealProgress)
            //notify the user
            onSwipeListener?.slide(revealProgress)
        }


        lastMainLeft = mainView.left
        lastMainTop = mainView.top


        if (dragEdge == DRAG_EDGE_LEFT || dragEdge == DRAG_EDGE_RIGHT) {
            secondaryView.offsetLeftAndRight(dx)
        } else {
            secondaryView.offsetTopAndBottom(dy)
        }

        //call invalidate
        ViewCompat.postInvalidateOnAnimation(changedView.parent as View)
    }
}