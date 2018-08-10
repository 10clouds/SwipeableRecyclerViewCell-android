package com.tenclouds.swipeablerecycler.adapter

import android.support.annotation.DrawableRes


data class Message(
        val id: Int,
        val message: String,
        val title: String,
        val status: String,
        @DrawableRes
        val avatar: Int,
        //Used to store view state
        var viewOpened: Boolean = false
)