package com.tenclouds.swipeablerecyclerviewcell.utils

import junit.framework.Assert.assertEquals
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin


class MathTest {
    @Test
    fun `point addition returns expected result`() {
        val point1 = Point(1f, 2f)
        val point2 = Point(3f, 4f)

        assertEquals(Point(4f, 6f), point1 + point2)
    }

    @Test
    fun `vector from angle and lenght returns proper point as a vector`() {
        val vector = getVectorFrom(2f, 1f)
        val expectedResult = Point(cos(2f), sin(2f))

        assertEquals(expectedResult, vector)
    }

    @Test
    fun `distance between two points returns expected result`() {
        val point1 = Point(-1f, 4f)
        val point2 = Point(7f, 4f)

        val expectedResult = 8f
        // Formula: sqrt((point2.x - point1.x).pow(2) + (point2.y - point1.y).pow(2))

        assertEquals(expectedResult, getDistance(point1, point2))
    }

    @Test
    fun `vector lenght returns expected result`() {
        val vector = Point(0f,4f)
        val expectedResult = 4f

        assertEquals(expectedResult, getVectorLength(vector.x, vector.y))
    }

}