package com.tenclouds.swipeablerecycler

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.tenclouds.swipeablerecycler.adapter.Message
import com.tenclouds.swipeablerecycler.adapter.MessageAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureRecyclerView()
    }

    private fun configureRecyclerView() {
        with(rvItems) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = MessageAdapter(items).apply { setHasStableIds(true) }
        }
    }

    private val items: MutableList<Message> = arrayListOf(
            Message(
                    id = 0,
                    title = "Eric Snyder",
                    message = "is a fir native to the mountains…",
                    avatar = R.drawable.avatar1,
                    status = "Work"
            )
            /*  Message(
                    id = 1,
                    title = "Diascia barberae",
                    message = "is a fir native to the mountains…",
                    avatar = R.drawable.avatar2,
                    status = "Work"
            ),
            Message(
                    id = 2,
                    title = "Dizzi Clara",
                    message = "is a fir native to the mountains…",
                    avatar = R.drawable.avatar3,
                    status = "Work"
            ),
            Message(
                    id = 3,
                    title = "Clara McDaniel",
                    message = "is a fir native to the mountains…",
                    avatar = R.drawable.avatar1,
                    status = "Work"
            ),
            Message(
                    id = 4,
                    title = "Diascia barberae",
                    message = "is a fir native to the mountains…",
                    avatar = R.drawable.avatar3,
                    status = "Work"
            ),
            Message(
                    id = 5,
                    title = "10Clouds made this",
                    message = "is a fir native to the mountains…\n21 items | London UK",
                    avatar = R.drawable.avatar2,
                    status = "Work"
            )*/
    )
}
