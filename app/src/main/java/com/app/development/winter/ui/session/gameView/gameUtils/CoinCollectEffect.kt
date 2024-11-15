package com.app.development.winter.ui.session.gameView.gameUtils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

// Define your CoinCollectEffect class
class CoinCollectEffect(
    private val startX: Int,
    private val startY: Int,
    private var scale: Float = 4.0f
) {
    private var alpha = 255f
    private var lifetime = 20 // Adjust lifetime based on desired duration
    private var frame = 0

    fun update() {
        frame++
        if (frame < lifetime) {
            scale += 0.1f // Scale up effect
            alpha -= 12.75f // Fade out
        }
    }

    fun draw(canvas: Canvas) {
        // Draw your coin collect animation here using the current scale and alpha
        val paint = Paint().apply {
            color = Color.argb(alpha, 254, 166, 59) // Gold color for coin effect
            alpha = this@CoinCollectEffect.alpha.toInt()
        }
        canvas.save()
        canvas.scale(scale, scale, startX.toFloat(), startY.toFloat())
        canvas.drawCircle(
            startX.toFloat(),
            startY.toFloat(),
            10f,
            paint
        ) // Replace with your coin graphic
        canvas.restore()
    }

    fun isFinished(): Boolean {
        return frame >= lifetime
    }
}