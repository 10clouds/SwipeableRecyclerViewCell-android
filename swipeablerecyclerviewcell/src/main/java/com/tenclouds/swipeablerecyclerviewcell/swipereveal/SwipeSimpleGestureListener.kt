package com.tenclouds.swipeablerecyclerviewcell.swipereveal

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewParent


class SwipeSimpleGestureListener(
        var parent: ViewParent,
        private val isScrolling: (Boolean) -> Boolean,
        private val minDistRequestDisallowParent: Int,
        private val distToClosestEdge: () -> Int

) : GestureDetector.SimpleOnGestureListener() {
    private var hasDisallowed = false

    override fun onDown(e: MotionEvent): Boolean {
        isScrolling(false)
        hasDisallowed = false
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float)
            : Boolean {
        isScrolling(true)
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float)
            : Boolean {
        isScrolling(true)

        val shouldDisallow: Boolean

        if (!hasDisallowed) {
            shouldDisallow = distToClosestEdge() >= minDistRequestDisallowParent
            if (shouldDisallow) {
                hasDisallowed = true
            }
        } else {
            shouldDisallow = true
        }

        // disallow parent to intercept touch event so that the layout will work
        // properly on RecyclerView or view that handles scroll gesture.
        parent.requestDisallowInterceptTouchEvent(shouldDisallow)

        return false
    }
}