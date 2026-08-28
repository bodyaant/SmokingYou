package com.smokingtracker.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.LruCache
import kotlin.math.cos
import kotlin.math.sin

object CookieShapeDrawable {

    private val cache = LruCache<String, Bitmap>(16)

    fun createCookieBitmap(context: Context, sizeDp: Int, color: Int, petals: Int = 12): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
        val cacheKey = "${sizePx}_${color}_$petals"

        cache.get(cacheKey)?.let { if (!it.isRecycled) return it }

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val outerR = sizePx / 2f * 0.95f
        val innerR = outerR * 0.85f

        val path = Path()
        val totalPoints = petals * 2
        val angleStep = (2 * Math.PI / totalPoints)

        val points = Array(totalPoints) { i ->
            val angle = i * angleStep - Math.PI / 2
            val r = if (i % 2 == 0) outerR else innerR
            val px = cx + (r * cos(angle)).toFloat()
            val py = cy + (r * sin(angle)).toFloat()
            Pair(px, py)
        }

        val firstMidX = (points[totalPoints - 1].first + points[0].first) / 2f
        val firstMidY = (points[totalPoints - 1].second + points[0].second) / 2f
        path.moveTo(firstMidX, firstMidY)

        for (i in 0 until totalPoints) {
            val pCurrent = points[i]
            val pNext = points[(i + 1) % totalPoints]
            val midX = (pCurrent.first + pNext.first) / 2f
            val midY = (pCurrent.second + pNext.second) / 2f

            path.quadTo(pCurrent.first, pCurrent.second, midX, midY)
        }

        path.close()
        canvas.drawPath(path, paint)

        cache.put(cacheKey, bitmap)
        return bitmap
    }
}
