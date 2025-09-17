package com.example.snappyrulerset.geom

import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.round

fun distance(p1: PointF, p2: PointF): Float {
    return hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
}

fun midpoint(p1: PointF, p2: PointF): PointF {
    return PointF((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f)
}

fun projectionOfPointOnLine(point: PointF, lineP1: PointF, lineP2: PointF): PointF {
    val vx = lineP2.x - lineP1.x
    val vy = lineP2.y - lineP1.y
    val denom = vx * vx + vy * vy
    if (denom == 0f) return PointF(lineP1.x, lineP1.y)
    val wx = point.x - lineP1.x
    val wy = point.y - lineP1.y
    val t = (wx * vx + wy * vy) / denom
    return PointF(lineP1.x + t * vx, lineP1.y + t * vy)
}

// Returns the smaller angle in degrees between segments (vertex->p1) and (vertex->p2), in [0, 180]
fun angleBetween(p1: PointF, vertex: PointF, p2: PointF): Float {
    val aX = p1.x - vertex.x
    val aY = p1.y - vertex.y
    val bX = p2.x - vertex.x
    val bY = p2.y - vertex.y
    val ang1 = atan2(aY.toDouble(), aX.toDouble())
    val ang2 = atan2(bY.toDouble(), bX.toDouble())
    var diff = Math.toDegrees((ang2 - ang1)).toFloat()
    while (diff <= -180f) diff += 360f
    while (diff > 180f) diff -= 360f
    return kotlin.math.abs(diff)
}

fun lineIntersection(p1: PointF, p2: PointF, q1: PointF, q2: PointF): PointF? {
    val x1 = p1.x; val y1 = p1.y
    val x2 = p2.x; val y2 = p2.y
    val x3 = q1.x; val y3 = q1.y
    val x4 = q2.x; val y4 = q2.y

    val denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
    if (kotlin.math.abs(denom) < 1e-6f) return null
    val px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom
    val py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom
    return PointF(px, py)
}

