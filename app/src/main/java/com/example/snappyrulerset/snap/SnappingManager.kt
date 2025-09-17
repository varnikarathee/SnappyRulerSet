package com.example.snappyrulerset.snap

import android.graphics.PointF
import com.example.snappyrulerset.model.Shape
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.round

object SnappingManager {
    // Providers are set by the canvas at runtime to avoid tight coupling
    var shapesProvider: () -> List<Shape> = { emptyList() }
    var gridSpacingPxProvider: () -> Float = { 20f } // should be 5mm in px
    var zoomProvider: () -> Float = { 1f }

    /**
     * Returns closest snap candidate to [point] if within a dynamic radius; otherwise null.
     * Candidates include:
     * - Grid intersections at 5mm spacing (provided in pixels)
     * - Existing shape endpoints and midpoints
     * - Intersections of existing lines
     */
    fun findSnapCandidate(point: PointF): PointF? {
        val candidates = mutableListOf<PointF>()

        val spacing = gridSpacingPxProvider().coerceAtLeast(2f)
        val zoom = zoomProvider().coerceAtLeast(0.1f)
        val snapRadius = (24f / zoom).coerceIn(8f, 48f)

        // 1) Grid intersection nearest to point
        val gx = round(point.x / spacing) * spacing
        val gy = round(point.y / spacing) * spacing
        candidates.add(PointF(gx, gy))

        // 2) Existing shape endpoints/midpoints
        val shapes = shapesProvider()
        for (shape in shapes) {
            when (shape) {
                is Shape.Line -> {
                    candidates.add(PointF(shape.p1.x, shape.p1.y))
                    candidates.add(PointF(shape.p2.x, shape.p2.y))
                    candidates.add(PointF((shape.p1.x + shape.p2.x) * 0.5f, (shape.p1.y + shape.p2.y) * 0.5f))
                }
                is Shape.PolyStroke -> {
                    val pts = shape.points
                    if (pts.isNotEmpty()) {
                        candidates.add(PointF(pts.first().x, pts.first().y))
                        candidates.add(PointF(pts.last().x, pts.last().y))
                        // Add some segment midpoints (sampled up to ~20 segments)
                        val step = max(1, pts.size / 20)
                        for (i in 0 until pts.size - 1 step step) {
                            val a = pts[i]
                            val b = pts[i + 1]
                            candidates.add(PointF((a.x + b.x) * 0.5f, (a.y + b.y) * 0.5f))
                        }
                    }
                }
                is Shape.Circle -> {
                    // Could add 4 cardinal points, but spec doesn't require; skip
                }
            }
        }

        // 3) Intersections of existing lines (pairwise infinite lines)
        val lines = shapes.filterIsInstance<Shape.Line>()
        for (i in 0 until lines.size) {
            for (j in i + 1 until lines.size) {
                intersectionPoint(lines[i], lines[j])?.let { candidates.add(it) }
            }
        }

        // Choose closest candidate within radius
        var best: PointF? = null
        var bestDist = Float.MAX_VALUE
        for (c in candidates) {
            val d = distance(point, c)
            if (d < bestDist) {
                bestDist = d
                best = c
            }
        }
        return if (best != null && bestDist <= snapRadius) best else null
    }

    private fun distance(a: PointF, b: PointF): Float {
        return hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble()).toFloat()
    }

    // Intersection of infinite lines defined by segments l1 and l2
    private fun intersectionPoint(l1: Shape.Line, l2: Shape.Line): PointF? {
        val x1 = l1.p1.x; val y1 = l1.p1.y
        val x2 = l1.p2.x; val y2 = l1.p2.y
        val x3 = l2.p1.x; val y3 = l2.p1.y
        val x4 = l2.p2.x; val y4 = l2.p2.y

        val denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        if (abs(denom) < 1e-6f) return null

        val px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom
        val py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom
        return PointF(px, py)
    }
}

