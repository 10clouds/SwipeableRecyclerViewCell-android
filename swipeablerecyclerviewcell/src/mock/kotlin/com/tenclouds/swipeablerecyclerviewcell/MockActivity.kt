package com.tenclouds.swipeablerecyclerviewcell

import android.app.Activity
import android.os.Bundle
import com.tenclouds.swipeablerecyclerviewcell.R


class MockActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.single_cell_layout)
    }
}