package com.tenclouds.swipeablerecycler.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tenclouds.swipeablerecycler.R
import com.tenclouds.swipeablerecycler.databinding.VhMessageBinding


class MessageAdapter(private val list: MutableList<Message>)
    : RecyclerView.Adapter<MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.vh_message, parent, false)

        return MessageViewHolder(binding as VhMessageBinding) {
            notifyItemRemoved(list.indexOf(it))
            list.remove(it)
        }
    }

    override fun getItemId(position: Int): Long = list[position].id.toLong()

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(list[position])
    }
}