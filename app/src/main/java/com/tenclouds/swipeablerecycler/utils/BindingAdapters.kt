package com.tenclouds.swipeablerecycler.utils

import android.databinding.BindingAdapter
import android.support.annotation.DrawableRes
import android.widget.TextView
import com.tenclouds.swipeablerecycler.R
import de.hdodenhof.circleimageview.CircleImageView


@BindingAdapter("drawableSrc")
fun setDrawableSrc(view: CircleImageView?, @DrawableRes drawable: Int) {
    view?.setImageResource(drawable)
}

@BindingAdapter("workStatus")
fun setWorkStatus(view: TextView?, status: String) {
    view?.let { tv ->
        tv.text = tv.context.resources.getString(R.string.work_status, status)
    }
}