package com.example.snappyrulerset.model

import android.graphics.PointF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

sealed class Shape {
    data class Line(val p1: PointF, val p2: PointF) : Shape()
    data class PolyStroke(val points: List<PointF>) : Shape()
    data class Circle(val center: PointF, val radius: Float) : Shape()
}

class CanvasState {
    val shapes = mutableStateListOf<Shape>()
    private val undoStack = mutableListOf<List<Shape>>()
    private val redoStack = mutableListOf<List<Shape>>()

    var scale by mutableStateOf(1f)
    var translation by mutableStateOf(Offset.Zero)

    fun beginStroke(start: Offset, color: Color = Color.Black, strokeWidthPx: Float = 3f) {
        val stroke = Shape.PolyStroke(listOf(start.toPointF()))
        shapes.add(stroke)
    }

    fun addPoint(point: Offset) {
        val last = shapes.lastOrNull()
        if (last is Shape.PolyStroke) {
            val newPoints = last.points + point.toPointF()
            val index = shapes.lastIndex
            shapes[index] = Shape.PolyStroke(newPoints)
        }
    }

    fun endStroke() {
        // No-op for now; could simplify or resample here
    }

    fun pushUndo() {
        undoStack.add(shapes.toList())
        redoStack.clear() // Clear redo when new action is performed
    }

    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        
        redoStack.add(shapes.toList())
        shapes.clear()
        shapes.addAll(undoStack.removeLastOrNull() ?: emptyList())
        return true
    }

    fun redo(): Boolean {
        if (redoStack.isEmpty()) return false
        
        undoStack.add(shapes.toList())
        shapes.clear()
        shapes.addAll(redoStack.removeLastOrNull() ?: emptyList())
        return true
    }
}

// Extension functions for conversion
fun Offset.toPointF(): PointF = PointF(x, y)
fun PointF.toOffset(): Offset = Offset(x, y)

fun Shape.PolyStroke.asPath(): Path {
    val path = Path()
    if (points.isEmpty()) return path
    val first = points.first().toOffset()
    path.moveTo(first.x, first.y)
    for (i in 1 until points.size) {
        val p = points[i].toOffset()
        path.lineTo(p.x, p.y)
    }
    return path
}

fun strokeStyleFor(widthPx: Float = 3f): Stroke = Stroke(
    width = widthPx,
    cap = StrokeCap.Round,
    join = StrokeJoin.Round
)

