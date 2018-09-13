package com.tenclouds.swipeablerecyclerviewcell.metaball

import com.tenclouds.swipeablerecyclerviewcell.utils.Point


internal data class ConnectorHolder(
        val topBezierStart: Point,
        val topBezierEnd: Point,
        val bottomBezierStart: Point,
        val bottomBezierEnd: Point,
        private val segment1: Point,
        private val segment2: Point,
        private val segment3: Point,
        private val segment4: Point
){
    val topBezierFirstControlPoint= topBezierStart + segment1
    val topBezierSecondControlPoint = topBezierEnd + segment2

    val bottomBezierFirstControlPoint = bottomBezierStart + segment3
    val bottomBezierSecondControlPoint = bottomBezierEnd + segment4
}