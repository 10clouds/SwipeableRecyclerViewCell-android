package com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces


internal interface OpenCloseListener {
    fun open(animate: Boolean)

    fun close(animate: Boolean)
}