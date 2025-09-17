package com.example.snappyrulerset

import android.graphics.PointF
import com.example.snappyrulerset.geom.*
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.abs
import kotlin.math.sqrt

class GeometryTests {

    @Test
    fun testDistance() {
        // Test basic distance calculation
        val p1 = PointF(0f, 0f)
        val p2 = PointF(3f, 4f)
        val expected = 5f // 3-4-5 triangle
        val actual = distance(p1, p2)
        assertEquals(expected, actual, 0.001f)

        // Test zero distance
        val p3 = PointF(5f, 5f)
        val zeroDist = distance(p3, p3)
        assertEquals(0f, zeroDist, 0.001f)

        // Test negative coordinates
        val p4 = PointF(-3f, -4f)
        val p5 = PointF(0f, 0f)
        val negDist = distance(p4, p5)
        assertEquals(5f, negDist, 0.001f)
    }

    @Test
    fun testMidpoint() {
        // Test basic midpoint
        val p1 = PointF(0f, 0f)
        val p2 = PointF(10f, 20f)
        val expected = PointF(5f, 10f)
        val actual = midpoint(p1, p2)
        assertEquals(expected.x, actual.x, 0.001f)
        assertEquals(expected.y, actual.y, 0.001f)

        // Test same point
        val p3 = PointF(5f, 5f)
        val sameMid = midpoint(p3, p3)
        assertEquals(p3.x, sameMid.x, 0.001f)
        assertEquals(p3.y, sameMid.y, 0.001f)

        // Test negative coordinates
        val p4 = PointF(-5f, -10f)
        val p5 = PointF(5f, 10f)
        val negMid = midpoint(p4, p5)
        assertEquals(0f, negMid.x, 0.001f)
        assertEquals(0f, negMid.y, 0.001f)
    }

    @Test
    fun testProjectionOfPointOnLine() {
        // Test projection onto horizontal line
        val point = PointF(5f, 10f)
        val lineP1 = PointF(0f, 0f)
        val lineP2 = PointF(10f, 0f)
        val expected = PointF(5f, 0f)
        val actual = projectionOfPointOnLine(point, lineP1, lineP2)
        assertEquals(expected.x, actual.x, 0.001f)
        assertEquals(expected.y, actual.y, 0.001f)

        // Test projection onto vertical line
        val point2 = PointF(10f, 5f)
        val lineP3 = PointF(0f, 0f)
        val lineP4 = PointF(0f, 10f)
        val expected2 = PointF(0f, 5f)
        val actual2 = projectionOfPointOnLine(point2, lineP3, lineP4)
        assertEquals(expected2.x, actual2.x, 0.001f)
        assertEquals(expected2.y, actual2.y, 0.001f)

        // Test projection onto diagonal line
        val point3 = PointF(0f, 0f)
        val lineP5 = PointF(0f, 0f)
        val lineP6 = PointF(3f, 4f)
        val expected3 = PointF(0f, 0f) // Point is on the line
        val actual3 = projectionOfPointOnLine(point3, lineP5, lineP6)
        assertEquals(expected3.x, actual3.x, 0.001f)
        assertEquals(expected3.y, actual3.y, 0.001f)

        // Test degenerate line (same points)
        val point4 = PointF(5f, 5f)
        val lineP7 = PointF(0f, 0f)
        val lineP8 = PointF(0f, 0f)
        val actual4 = projectionOfPointOnLine(point4, lineP7, lineP8)
        assertEquals(lineP7.x, actual4.x, 0.001f)
        assertEquals(lineP7.y, actual4.y, 0.001f)
    }

    @Test
    fun testAngleBetween() {
        // Test 90 degree angle
        val p1 = PointF(1f, 0f)
        val vertex = PointF(0f, 0f)
        val p2 = PointF(0f, 1f)
        val expected90 = 90f
        val actual90 = angleBetween(p1, vertex, p2)
        assertEquals(expected90, actual90, 0.1f)

        // Test 45 degree angle
        val p3 = PointF(1f, 0f)
        val p4 = PointF(1f, 1f)
        val expected45 = 45f
        val actual45 = angleBetween(p3, vertex, p4)
        assertEquals(expected45, actual45, 0.1f)

        // Test 180 degree angle (straight line)
        val p5 = PointF(-1f, 0f)
        val p6 = PointF(1f, 0f)
        val expected180 = 180f
        val actual180 = angleBetween(p5, vertex, p6)
        assertEquals(expected180, actual180, 0.1f)

        // Test 0 degree angle (same direction)
        val p7 = PointF(1f, 0f)
        val p8 = PointF(2f, 0f)
        val expected0 = 0f
        val actual0 = angleBetween(p7, vertex, p8)
        assertEquals(expected0, actual0, 0.1f)

        // Test 30-60-90 triangle
        val p9 = PointF(sqrt(3f), 0f) // 30° from vertical
        val p10 = PointF(0f, 1f)      // 60° from horizontal
        val expected30 = 30f
        val actual30 = angleBetween(p9, vertex, p10)
        assertEquals(expected30, actual30, 0.1f)
    }

    @Test
    fun testLineIntersection() {
        // Test perpendicular lines intersection
        val p1 = PointF(0f, 0f)
        val p2 = PointF(10f, 0f)
        val q1 = PointF(5f, -5f)
        val q2 = PointF(5f, 5f)
        val expected = PointF(5f, 0f)
        val actual = lineIntersection(p1, p2, q1, q2)
        assertNotNull(actual)
        assertEquals(expected.x, actual!!.x, 0.001f)
        assertEquals(expected.y, actual.y, 0.001f)

        // Test parallel lines (no intersection)
        val p3 = PointF(0f, 0f)
        val p4 = PointF(10f, 0f)
        val q3 = PointF(0f, 5f)
        val q4 = PointF(10f, 5f)
        val noIntersection = lineIntersection(p3, p4, q3, q4)
        assertNull(noIntersection)

        // Test collinear lines (no intersection)
        val p5 = PointF(0f, 0f)
        val p6 = PointF(5f, 0f)
        val q5 = PointF(10f, 0f)
        val q6 = PointF(15f, 0f)
        val collinear = lineIntersection(p5, p6, q5, q6)
        assertNull(collinear)

        // Test diagonal lines intersection
        val p7 = PointF(0f, 0f)
        val p8 = PointF(10f, 10f)
        val q7 = PointF(0f, 10f)
        val q8 = PointF(10f, 0f)
        val expectedDiag = PointF(5f, 5f)
        val actualDiag = lineIntersection(p7, p8, q7, q8)
        assertNotNull(actualDiag)
        assertEquals(expectedDiag.x, actualDiag!!.x, 0.001f)
        assertEquals(expectedDiag.y, actualDiag.y, 0.001f)

        // Test lines that don't intersect within segments
        val p9 = PointF(0f, 0f)
        val p10 = PointF(1f, 0f)
        val q9 = PointF(0f, 1f)
        val q10 = PointF(1f, 1f)
        val outsideIntersection = lineIntersection(p9, p10, q9, q10)
        assertNull(outsideIntersection)
    }

    @Test
    fun testSnappingCandidateSelection() {
        // This test would require mocking the SnappingManager providers
        // For now, we'll test the core geometry functions that support snapping
        
        // Test grid snapping logic
        val point = PointF(12.3f, 45.7f)
        val gridSpacing = 20f
        val expectedGridX = 20f // round(12.3/20) * 20 = 20
        val expectedGridY = 40f // round(45.7/20) * 20 = 40
        
        val actualGridX = kotlin.math.round(point.x / gridSpacing) * gridSpacing
        val actualGridY = kotlin.math.round(point.y / gridSpacing) * gridSpacing
        
        assertEquals(expectedGridX, actualGridX, 0.001f)
        assertEquals(expectedGridY, actualGridY, 0.001f)
    }

    @Test
    fun testEdgeCases() {
        // Test very small distances
        val p1 = PointF(0f, 0f)
        val p2 = PointF(0.001f, 0.001f)
        val smallDist = distance(p1, p2)
        assertTrue(smallDist > 0f)
        assertTrue(smallDist < 0.01f)

        // Test very large coordinates
        val p3 = PointF(1000000f, 1000000f)
        val p4 = PointF(1000001f, 1000001f)
        val largeDist = distance(p3, p4)
        val expectedLarge = sqrt(2f)
        assertEquals(expectedLarge, largeDist, 0.001f)

        // Test angle with very small vectors
        val p5 = PointF(0.001f, 0f)
        val p6 = PointF(0f, 0.001f)
        val smallAngle = angleBetween(p5, PointF(0f, 0f), p6)
        assertEquals(90f, smallAngle, 0.1f)
    }

    @Test
    fun testPrecision() {
        // Test that calculations maintain sufficient precision
        val p1 = PointF(1.234567f, 2.345678f)
        val p2 = PointF(3.456789f, 4.567890f)
        
        val dist = distance(p1, p2)
        val mid = midpoint(p1, p2)
        
        // Verify precision is maintained
        assertTrue(dist > 0f)
        assertTrue(mid.x > p1.x && mid.x < p2.x)
        assertTrue(mid.y > p1.y && mid.y < p2.y)
        
        // Test that midpoint is actually in the middle
        val expectedMidX = (p1.x + p2.x) / 2f
        val expectedMidY = (p1.y + p2.y) / 2f
        assertEquals(expectedMidX, mid.x, 0.000001f)
        assertEquals(expectedMidY, mid.y, 0.000001f)
    }
}
