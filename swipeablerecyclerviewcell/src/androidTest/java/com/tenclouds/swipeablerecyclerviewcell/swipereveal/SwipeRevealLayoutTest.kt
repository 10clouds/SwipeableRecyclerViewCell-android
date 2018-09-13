package com.tenclouds.swipeablerecyclerviewcell.swipereveal

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.tenclouds.swipeablerecyclerviewcell.MockActivity
import com.tenclouds.swipeablerecyclerviewcell.R
import com.tenclouds.swipeablerecyclerviewcell.metaball.MetaBalls
import com.tenclouds.swipeablerecyclerviewcell.metaball.RIGHT_VIEW_TO_DELETE
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OnDeleteListener
import com.tenclouds.swipeablerecyclerviewcell.swipereveal.interfaces.OnIconClickListener
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock


@RunWith(AndroidJUnit4::class)
class SwipeRevealLayoutTest {
    @Rule
    @JvmField
    val activityRule = ActivityTestRule<MockActivity>(MockActivity::class.java, true, true)

    private lateinit var main: SwipeRevealLayout
    private lateinit var metaBalls: MetaBalls

    private val onIconClickListener = mock(OnIconClickListener::class.java)

    @Before
    fun setUp() {
        main = activityRule.activity.findViewById(R.id.swipeLayout)
        metaBalls = main.getChildAt(0) as MetaBalls
    }

    @Test
    fun textView_has_proper_text() {
        onView(withId(R.id.tvText)).check(matches(withText(R.string.string_test_value)))
    }

    @Test
    fun swipeLeftRevealsLayout() {
        onView(withId(metaBalls.id)).check(matches(not(isDisplayed())))

        onView(withId(main.id))
                .perform(swipeLeft())

        onView(withId(metaBalls.id)).check(matches(isDisplayed()))
    }

    @Test
    fun swipeRightHidesLayout() {
        onView(withId(main.id))
                .perform(swipeLeft())

        onView(withId(metaBalls.id)).check(matches(isDisplayed()))

        onView(withId(main.id))
                .perform(swipeRight())

        onView(withId(metaBalls.id)).check(matches(not(isDisplayed())))

    }

    @Test
    fun clickOnLeftIconCallsOnLeftIconClicked() {
        main.setOnIconClickListener(onIconClickListener)
        val leftIcon = metaBalls.getChildAt(0)

        onView(withId(main.id))
                .perform(swipeLeft())

        onView(withId(leftIcon.id)).perform(click())
        verify(onIconClickListener).onLeftIconClick()
    }

    @Test
    fun clickOnRightIconCallsOnRightIconClick() {
        main.setOnIconClickListener(onIconClickListener)
        val rightIcon = metaBalls.getChildAt(1)

        onView(withId(main.id))
                .perform(swipeLeft())

        onView(withId(rightIcon.id)).perform(click())
        verify(onIconClickListener).onRightIconClick()
    }
}