package com.app.development.winter.ui.session.gameView.gameobjects

import kotlin.math.sqrt

data class Position(var x: Float = 0.0f, var y: Float = 0.0f) {
    infix fun distanceTo(other: Position): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}
