package com.tenclouds.swipeablerecyclerviewcell.swipereveal

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.tenclouds.swipeablerecyclerviewcell.MockActivity
import com.tenclouds.swipeablerecyclerviewcell.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SwipeRevealLayoutTest {
    @Rule
    @JvmField
    val activityRule = ActivityTestRule<MockActivity>(MockActivity::class.java, true, true)

    @Test
    fun textView_has_proper_text() {
        onView(withId(R.id.tvText)).check(matches(withText(R.string.string_test_value)))
    }

    @Test
    fun swipeLeftRevealsLayout() {

    }

}