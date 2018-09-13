package com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces


interface OnSwipeListener {
    fun onClosed()

    fun onOpened()

    /**
     * @param progress  [0f-1f]
     */
    fun slide(progress: Float)
}