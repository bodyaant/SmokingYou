package com.smokingtracker.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.LruCache
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.graphics.createBitmap
import com.smokingtracker.R
import kotlin.math.cos
import kotlin.math.sin

object CookieShapeDrawable {

    @DrawableRes
    const val COOKIE_DRAWABLE_RES: Int = R.drawable.bg_widget_preview_cookie

    private val cache = object : LruCache<String, Bitmap>(8) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    fun applyToRemoteViews(views: RemoteViews, @IdRes viewId: Int) {
        views.setImageViewResource(viewId, COOKIE_DRAWABLE_RES)
    }

    fun createCookieBitmap(context: Context, sizeDp: Int, color: Int, petals: Int = 12): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceIn(1, 512)
        val safePetals = petals.coerceIn(3, 64)
        val nightModeBits = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val cacheKey = "${sizePx}_${color}_${safePetals}_$nightModeBits"

        synchronized(cache) {
            cache.get(cacheKey)?.let { if (!it.isRecycled) return it else cache.remove(cacheKey) }
        }

        val bitmap = createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
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
        val totalPoints = safePetals * 2
        val angleStep = (2.0 * Math.PI / totalPoints)

        val coords = FloatArray(totalPoints * 2)
        for (i in 0 until totalPoints) {
            val angle = i * angleStep - Math.PI / 2.0
            val r = if (i % 2 == 0) outerR else innerR
            coords[i * 2] = cx + (r * cos(angle)).toFloat()
            coords[i * 2 + 1] = cy + (r * sin(angle)).toFloat()
        }

        val lastIdx = (totalPoints - 1) * 2
        val firstMidX = (coords[lastIdx] + coords[0]) / 2f
        val firstMidY = (coords[lastIdx + 1] + coords[1]) / 2f
        path.moveTo(firstMidX, firstMidY)

        for (i in 0 until totalPoints) {
            val curIdx = i * 2
            val nextIdx = ((i + 1) % totalPoints) * 2
            val curX = coords[curIdx]
            val curY = coords[curIdx + 1]
            val midX = (curX + coords[nextIdx]) / 2f
            val midY = (curY + coords[nextIdx + 1]) / 2f

            path.quadTo(curX, curY, midX, midY)
        }

        path.close()
        canvas.drawPath(path, paint)

        synchronized(cache) {
            cache.put(cacheKey, bitmap)
        }
        return bitmap
    }
}
