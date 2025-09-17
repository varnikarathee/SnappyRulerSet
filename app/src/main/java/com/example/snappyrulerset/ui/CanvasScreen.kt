package com.example.snappyrulerset.ui

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.snappyrulerset.model.CanvasState
import com.example.snappyrulerset.model.Shape
import com.example.snappyrulerset.model.asPath
import com.example.snappyrulerset.model.strokeStyleFor
import kotlin.math.max
import com.example.snappyrulerset.ui.rememberRulerState
import com.example.snappyrulerset.ui.RulerOverlay

@Composable
fun CanvasScreen(
    state: CanvasState,
    modifier: Modifier = Modifier
) {
    val rulerState = rememberRulerState()
    val gridBitmapState: MutableState<ImageBitmap?> = remember { mutableStateOf(null) }
    val gridSizeState: MutableState<IntSize?> = remember { mutableStateOf(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        // Gesture handling: one finger draw, two fingers pan/zoom
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(
                        panZoomLock = true
                    ) { centroid, pan, zoom, rotation ->
                        // Heuristic: if zoom is ~1 and pan is small with one pointer -> drawing
                        // We can't directly get number of pointers here; use zoom threshold
                        if (zoom == 1f && pan == Offset.Zero) {
                            // ignore pure rotation updates
                            return@detectTransformGestures
                        }
                        if (zoom != 1f) {
                            val newScale = (state.scale * zoom).coerceIn(0.2f, 10f)
                            state.scale = newScale
                        }
                        if (pan != Offset.Zero) {
                            state.translation += pan
                        }
                    }
                }
                .pointerInput(Unit) {
                    // Dedicated pointer input for freehand drawing
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitPointerEvent().changes.firstOrNull { it.pressed } ?: continue
                            if (currentEvent.changes.size == 1) {
                                val (startPos, snapped) = rulerState.snap(down.position)
                                state.beginStroke(startPos)
                                down.consume()
                                while (down.pressed) {
                                    val evt = awaitPointerEvent()
                                    val change = evt.changes.firstOrNull() ?: break
                                    val (pt, isSnap) = rulerState.snap(change.position)
                                    state.addPoint(pt)
                                    change.consume()
                                }
                                state.endStroke()
                            } else {
                                // Skip drawing when multitouch
                                awaitPointerEvent()
                            }
                        }
                    }
                }
        ) {
            val canvasSize = size

            // Grid draw and cache
            drawIntoCanvas { canvas ->
                val currentSize = IntSize(canvasSize.width.toInt(), canvasSize.height.toInt())
                if (gridBitmapState.value == null || gridSizeState.value != currentSize) {
                    gridBitmapState.value = generateGridBitmap(currentSize)
                    gridSizeState.value = currentSize
                }
                gridBitmapState.value?.let { bmp ->
                    canvas.nativeCanvas.drawBitmap(bmp.asAndroidBitmap(), 0f, 0f, null)
                }
            }

            // Apply pan/zoom
            drawIntoCanvas { canvas ->
                canvas.save()
                canvas.translate(state.translation.x, state.translation.y)
                canvas.scale(state.scale, state.scale)

                // Render shapes
                state.shapes.forEachIndexed { index, shape ->
                    when (shape) {
                        is Shape.PolyStroke -> {
                            val path = shape.asPath()
                            drawPath(
                                path = path,
                                color = if (index == state.shapes.lastIndex && rulerState.lastSnapPoint != null) Color(0xFF1563C0) else Color.Black,
                                style = strokeStyleFor(3f)
                            )
                        }
                        is Shape.Line -> {
                            drawLine(
                                color = Color.Black,
                                start = androidx.compose.ui.geometry.Offset(shape.p1.x, shape.p1.y),
                                end = androidx.compose.ui.geometry.Offset(shape.p2.x, shape.p2.y),
                                strokeWidth = 3f
                            )
                        }
                        is Shape.Circle -> {
                            drawCircle(
                                color = Color.Black,
                                radius = shape.radius,
                                center = androidx.compose.ui.geometry.Offset(shape.center.x, shape.center.y),
                                style = strokeStyleFor(3f)
                            )
                        }
                    }
                }

                canvas.restore()
            }
        }

        // Overlay the ruler controls/visuals on top
        RulerOverlay(rulerState = rulerState, modifier = Modifier.fillMaxSize())
    }
}

private fun generateGridBitmap(size: IntSize): ImageBitmap {
    if (size.width <= 0 || size.height <= 0) {
        return ImageBitmap(1, 1)
    }
    val androidBitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    val nativeCanvas = AndroidCanvas(androidBitmap)
    val densityDpi = nativeCanvas.density.takeIf { it > 0 } ?: 440 // fallback
    // 5mm grid spacing: 1 inch = 25.4 mm ⇒ 5mm = 0.19685 in
    val spacingPx = (densityDpi * 0.19685f)
        .coerceAtLeast(4f)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = false
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 1f
        color = android.graphics.Color.argb(64, 0, 0, 0)
    }

    // Fill background
    nativeCanvas.drawColor(android.graphics.Color.WHITE)

    var x = 0f
    while (x <= size.width) {
        nativeCanvas.drawLine(x, 0f, x, size.height.toFloat(), paint)
        x += spacingPx
    }
    var y = 0f
    while (y <= size.height) {
        nativeCanvas.drawLine(0f, y, size.width.toFloat(), y, paint)
        y += spacingPx
    }
    return androidBitmap.asImageBitmap()
}

