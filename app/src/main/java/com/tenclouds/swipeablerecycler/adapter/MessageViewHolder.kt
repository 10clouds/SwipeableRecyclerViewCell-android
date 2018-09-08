package com.tenclouds.swipeablerecycler.adapter

import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.tenclouds.swipeablerecycler.databinding.VhMessageBinding
import com.tenclouds.swipeablerecyclerviewcell.metaball.RIGHT_VIEW_TO_DELETE
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.SwipeRevealLayout


class MessageViewHolder(val b: VhMessageBinding, val endAction: (Message) -> Unit)
    : RecyclerView.ViewHolder(b.root) {

    fun bind(item: Message) {
        b.item = item
        b.executePendingBindings()

        (b.root as? SwipeRevealLayout)
                ?.apply {
                    setOnSwipeListener(
                            onClosed = { item.viewOpened = false },
                            onOpened = { item.viewOpened = true }
                    )
                    setOnIconClickListener(
                            onLeftIconClick = {
                                Toast.makeText(b.root.context, "LEFT", Toast.LENGTH_LONG).show()
                            },
                            //If the deleteView is set to the right then this call will be delayed
                            //for 300ms for animation of deleting to finish
                            onRightIconClick = { endAction(item) },
                            sideToDelete = RIGHT_VIEW_TO_DELETE
                    )
                }
    }
}