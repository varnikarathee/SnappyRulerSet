package com.example.snappyrulerset.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.xr.runtime.math.toRadians
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.atan2

class RulerState(
    isActive: Boolean = true,
    center: Offset = Offset(400f, 400f),
    angleRad: Float = 0f,
    lengthPx: Float = 0f,
    widthPx: Float = 0f
) {
    var isActive: Boolean by mutableStateOf(isActive)
    var center: Offset by mutableStateOf(center)
    var angleRad: Float by mutableFloatStateOf(angleRad)
    var lengthPx: Float by mutableFloatStateOf(lengthPx)
    var widthPx: Float by mutableFloatStateOf(widthPx)

    // Last snap point for visual tick
    var lastSnapPoint: Offset? by mutableStateOf(null)

    fun snap(point: Offset): Pair<Offset, Boolean> {
        if (!isActive || lengthPx <= 0f) {
            lastSnapPoint = null
            return point to false
        }
        val dir = Offset(cos(angleRad), sin(angleRad))
        val rel = point - center
        val t = rel.x * dir.x + rel.y * dir.y
        val half = lengthPx / 2f
        val clampedT = t.coerceIn(-half, half)
        val projected = center + dir * clampedT
        lastSnapPoint = projected
        return projected to true
    }
}

@Composable
fun rememberRulerState(): RulerState {
    return remember { RulerState() }
}

private fun snapAngle(angleRad: Float): Float {
    val allowedDeg = listOf(0f, 30f, 45f, 60f, 90f)
    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
    // Normalize to [0,180)
    val norm = ((angleDeg % 180f) + 180f) % 180f
    val snapped = allowedDeg.minBy { a -> kotlin.math.abs(a - norm) }
    return Math.toRadians(snapped.toDouble()).toFloat()
}

@Composable
fun RulerOverlay(
    rulerState: RulerState,
    modifier: Modifier = Modifier,
    density: Density = Density(1f)
) {
    val state: MutableState<RulerState> = rememberUpdatedState(rulerState) as MutableState<RulerState>

    // Initialize length/width defaults using density if zero
    val defaultLengthPx = with(density) { 200.dp.toPx() }
    val defaultWidthPx = with(density) { 24.dp.toPx() }
    if (rulerState.lengthPx <= 0f) rulerState.lengthPx = defaultLengthPx
    if (rulerState.widthPx <= 0f) rulerState.widthPx = defaultWidthPx

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures(panZoomLock = false) { centroid, pan, zoom, rotation ->
                    if (!rulerState.isActive) return@detectTransformGestures
                    // Move by pan
                    if (pan != Offset.Zero) {
                        rulerState.center += pan
                    }
                    if (rotation != 0f) {
                        var ang = rulerState.angleRad + rotation
                        // Snap to specific angles
                        val snapped = snapAngle(ang)
                        // If within ~5 degrees, use snapped
                        val deg = Math.toDegrees(ang.toDouble()).toFloat()
                        val sdeg = Math.toDegrees(snapped.toDouble()).toFloat()
                        if (abs(deg - sdeg) <= 5f) {
                            ang = snapped
                        }
                        rulerState.angleRad = ang
                    }
                }
            }
    ) {
        if (!rulerState.isActive) return@Canvas

        val halfL = rulerState.lengthPx / 2f
        val halfW = rulerState.widthPx / 2f
        val ux = cos(rulerState.angleRad)
        val uy = sin(rulerState.angleRad)
        val vx = -uy
        val vy = ux

        val c = rulerState.center

        // Rectangle corners for ruler body
        val p1 = c + Offset(ux * -halfL + vx * -halfW, uy * -halfL + vy * -halfW)
        val p2 = c + Offset(ux * halfL + vx * -halfW, uy * halfL + vy * -halfW)
        val p3 = c + Offset(ux * halfL + vx * halfW, uy * halfL + vy * halfW)
        val p4 = c + Offset(ux * -halfL + vx * halfW, uy * -halfL + vy * halfW)

        // Body fill
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                lineTo(p4.x, p4.y)
                close()
            },
            color = Color(0x331563C0)
        )

        // Edge line for snapping edge (along the long axis)
        drawLine(
            color = Color(0xFF1563C0),
            start = c + Offset(ux * -halfL, uy * -halfL),
            end = c + Offset(ux * halfL, uy * halfL),
            strokeWidth = 2f
        )

        // Tick marker at last snapped point
        state.value.lastSnapPoint?.let { sp ->
            drawLine(
                color = Color(0xFF0D47A1),
                start = sp + Offset(vx * -6f, vy * -6f),
                end = sp + Offset(vx * 6f, vy * 6f),
                strokeWidth = 3f
            )
        }
    }
}

class SetSquareState(
    isActive: Boolean = false,
    center: Offset = Offset(300f, 300f),
    angleRad: Float = 0f,
    val variant: Variant = Variant.Right45,
    sizePx: Float = 0f
) {
    enum class Variant { Right45, Right30_60_90 }
    var isActive: Boolean by mutableStateOf(isActive)
    var center: Offset by mutableStateOf(center)
    var angleRad: Float by mutableFloatStateOf(angleRad)
    var sizePx: Float by mutableFloatStateOf(sizePx)
}

@Composable
fun rememberSetSquareState(variant: SetSquareState.Variant): SetSquareState {
    return remember { SetSquareState(isActive = false, variant = variant) }
}

// --- Protractor ---

class ProtractorState(
    isActive: Boolean = false,
    center: Offset = Offset(500f, 500f),
    var firstRay: Offset? = null,
    var secondRay: Offset? = null
) {
    var isActive: Boolean by mutableStateOf(isActive)
    var center: Offset by mutableStateOf(center)

    fun angleDegrees(): Float? {
        val a = firstRay ?: return null
        val b = secondRay ?: return null
        val ang1 = atan2((a.y - center.y), (a.x - center.x))
        val ang2 = atan2((b.y - center.y), (b.x - center.x))
        var deg = Math.toDegrees((ang2 - ang1).toDouble()).toFloat()
        while (deg < 0f) deg += 360f
        if (deg > 180f) deg = 360f - deg
        return deg
    }
}

// --- Compass ---

class CompassState(
    isActive: Boolean = false,
    center: Offset = Offset(600f, 600f),
    radius: Float = 120f
) {
    var isActive: Boolean by mutableStateOf(isActive)
    var center: Offset by mutableStateOf(center)
    var radius: Float by mutableFloatStateOf(radius)
}

@Composable
fun rememberCompassState(): CompassState = remember { CompassState() }

@Composable
fun CompassOverlay(
    state: CompassState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    if (!state.isActive) return@detectTransformGestures
                    if (pan != Offset.Zero) state.center += pan
                    if (zoom != 1f) state.radius = (state.radius * zoom).coerceIn(10f, 2000f)
                }
            }
    ) {
        if (!state.isActive) return@Canvas
        drawCircle(
            color = Color(0x333F51B5),
            radius = state.radius,
            center = state.center
        )
        drawCircle(
            color = Color(0xFF303F9F),
            radius = state.radius,
            center = state.center,
            style = Stroke(width = 3f)
        )
        // Handle to adjust radius (visual only)
        drawCircle(
            color = Color(0xFF303F9F),
            radius = 6f,
            center = state.center + Offset(state.radius, 0f)
        )
    }
}

@Composable
fun rememberProtractorState(): ProtractorState = remember { ProtractorState() }

@Composable
fun ProtractorOverlay(
    state: ProtractorState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    if (!state.isActive) return@detectTransformGestures
                    if (pan != Offset.Zero) state.center += pan
                    // Rays set via taps/gestures in higher-level UI (not implemented here)
                }
            }
    ) {
        if (!state.isActive) return@Canvas
        val c = state.center
        val radius = 160f

        // Semi-circle
        drawArc(
            color = Color(0x332196F3),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(c.x - radius, c.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
        drawArc(
            color = Color(0xFF1976D2),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(c.x - radius, c.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 3f)
        )

        // Ticks every 10°
        for (i in 0..18) {
            val deg = i * 10f
            val rad = Math.toRadians(deg.toDouble()).toFloat()
            val sx = c.x + radius * kotlin.math.cos(Math.PI.toFloat() + rad)
            val sy = c.y + radius * kotlin.math.sin(Math.PI.toFloat() + rad)
            val ix = c.x + (radius - if (i % 3 == 0) 14f else 8f) * kotlin.math.cos(Math.PI.toFloat() + rad)
            val iy = c.y + (radius - if (i % 3 == 0) 14f else 8f) * kotlin.math.sin(Math.PI.toFloat() + rad)
            drawLine(Color(0xFF1976D2), Offset(ix, iy), Offset(sx, sy), 2f)
        }

        // Rays
        state.firstRay?.let { a ->
            drawLine(Color(0xFF0D47A1), c, a, 3f)
        }
        state.secondRay?.let { b ->
            drawLine(Color(0xFF0D47A1), c, b, 3f)
        }

        // Angle readout (snapped to nearest 1°)
        state.angleDegrees()?.let { deg ->
            val snapped = kotlin.math.round(deg)
            // Simple label: small circle near center with text not implemented (no text here)
            drawCircle(Color(0xFF1976D2), radius = 6f, center = c + Offset(0f, -radius + 20f))
        }
    }
}

@Composable
fun SetSquareOverlay(
    state: SetSquareState,
    modifier: Modifier = Modifier,
    density: Density = Density(1f)
) {
    val defaultSize = with(density) { 220.dp.toPx() }
    if (state.sizePx <= 0f) state.sizePx = defaultSize

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    if (!state.isActive) return@detectTransformGestures
                    if (pan != Offset.Zero) state.center += pan
                    if (rotation != 0f) state.angleRad += rotation
                }
            }
    ) {
        if (!state.isActive) return@Canvas

        // Build triangle points in local space then transform
        val size = state.sizePx
        val vertsLocal = when (state.variant) {
            SetSquareState.Variant.Right45 -> listOf(
                Offset(0f, 0f), Offset(size, 0f), Offset(0f, size)
            )
            SetSquareState.Variant.Right30_60_90 -> listOf(
                Offset(0f, 0f), Offset(size * 1.732f, 0f), Offset(0f, size)
            )
        }

        val cosA = cos(state.angleRad)
        val sinA = sin(state.angleRad)
        fun xform(p: Offset): Offset {
            val r = Offset(
                p.x * cosA - p.y * sinA,
                p.x * sinA + p.y * cosA
            )
            return state.center + r
        }
        val verts = vertsLocal.map { xform(it) }

        // Fill body
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(verts[0].x, verts[0].y)
                lineTo(verts[1].x, verts[1].y)
                lineTo(verts[2].x, verts[2].y)
                close()
            },
            color = Color(0x3327AE60)
        )

        // Draw edges and highlight the longest edge
        val edges = listOf(verts[0] to verts[1], verts[1] to verts[2], verts[2] to verts[0])
        val lengths = edges.map { (a, b) ->
            val dx = b.x - a.x; val dy = b.y - a.y
            sqrt(dx * dx + dy * dy)
        }
        val activeIndex = lengths.indexOf(lengths.maxOrNull())
        edges.forEachIndexed { i, (a, b) ->
            drawLine(
                color = if (i == activeIndex) Color(0xFF1B5E20) else Color(0xFF2E7D32),
                start = a,
                end = b,
                strokeWidth = if (i == activeIndex) 5f else 3f
            )
        }
    }
}

