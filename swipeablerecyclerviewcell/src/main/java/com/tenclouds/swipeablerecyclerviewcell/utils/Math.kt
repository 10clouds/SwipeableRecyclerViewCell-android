package com.tenclouds.swipeablerecyclerviewcell.utils

import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


internal data class Point(var x: Float = 0.0f,
                          var y: Float = 0.0f) {
    operator fun plus(p2: Point): Point =
        Point(x + p2.x, y + p2.y)
}

internal data class Circle(var position: Point = Point(),
                           var radius: Float = 0f,
                           val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
                                   .apply { style = Paint.Style.FILL })

internal fun getVectorFrom(radianAngle: Float, length: Float) =
        Point(cos(radianAngle) * length, sin(radianAngle) * length)


internal fun getDistance(p1: Point, p2: Point): Float {
    val distX = p2.x - p1.x
    val distY = p2.y - p1.y

    return sqrt(distX * distX + distY * distY)
}

internal fun getVectorLength(x: Float, y: Float) = sqrt(x * x + y * y)
