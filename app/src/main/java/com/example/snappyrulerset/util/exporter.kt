package com.example.snappyrulerset.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.snappyrulerset.model.CanvasState
import com.example.snappyrulerset.model.Shape
import java.io.File
import java.io.FileOutputStream

object Exporter {
    fun exportToImage(context: Context, canvasState: CanvasState, widthPx: Int, heightPx: Int, backgroundColor: Int = Color.WHITE): File? {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(backgroundColor)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        canvas.save()
        canvas.translate(canvasState.translation.x, canvasState.translation.y)
        canvas.scale(canvasState.scale, canvasState.scale)

        canvasState.shapes.forEach { shape ->
            when (shape) {
                is Shape.PolyStroke -> {
                    val pts = shape.points
                    if (pts.size >= 2) {
                        for (i in 0 until pts.size - 1) {
                            val a = pts[i]
                            val b = pts[i + 1]
                            canvas.drawLine(a.x, a.y, b.x, b.y, paint)
                        }
                    }
                }
                is Shape.Line -> {
                    canvas.drawLine(shape.p1.x, shape.p1.y, shape.p2.x, shape.p2.y, paint)
                }
                is Shape.Circle -> {
                    canvas.drawCircle(shape.center.x, shape.center.y, shape.radius, paint)
                }
            }
        }
        canvas.restore()

        val outFile = File(context.cacheDir, "snappy_export_${System.currentTimeMillis()}.png")
        FileOutputStream(outFile).use { fos ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        return outFile
    }

    fun shareImage(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share drawing"))
    }
}


