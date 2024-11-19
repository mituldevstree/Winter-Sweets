package com.app.development.winter.ui.session.gameView.gameobjects

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.app.development.winter.shared.extension.toPx

data class GameStateNew(
    var config: GameConfig = GameConfig(),
    var playerWidth: Int = 0,
    var playerHeight: Int = 0,
    var columnWidth: Int = 0,
    var currentLevelSpeed: Int = 0,
    var playerLives: Int = 1,
    var totalScore: Int = 0,
    var totalHitObstacles: Int = 0,
    var playerColumnPosition: Int = 0,
    val playerPaddingBottom: Int = PLAYER_BOTTOM_PADDING,
    var currentPlayerPosition: Position = Position(),
    var currentPlayerRotation: Float = 0f,
    var gameViewWidth: Int = 0,
    var gameViewHeight: Int = 0,
    var currentGameSpeed: Float = config.initialGameSpeed,
    var currentGameSpeedFactor: Float = 1.5f,
    var currentGameStatus: GameStatus = GameStatus.DEFAULT,
    var currentGameScoreFactor: Int = -1,
    var needToGenerateNewObject: Boolean = false,

    val boxPaint: Paint = Paint().apply {
        color = Color.parseColor("#4D000000"); // Set color to blue
        style = Paint.Style.FILL // Set style to fill
    }
) {
    fun resetGameValue() {
        currentGameStatus = GameStatus.DEFAULT
        playerLives = 1
        playerLives = 1
        totalScore = 0
        totalHitObstacles = 0
        playerColumnPosition = 0
        currentGameSpeedFactor = 1.5f
    }

    fun setGameConfig(gameConfig: GameConfig) {
        config = gameConfig
        currentGameSpeed = config.initialGameSpeed
    }

    val playerLeft
        get() =
            (playerColumnPosition * columnWidth) + (columnWidth / 2) - (playerWidth / 2)

    val playerTop
        get() =
            gameViewHeight - (playerHeight) - playerPaddingBottom // Ad

    val playerReact
        get() = RectF(
            /* left = */ playerLeft.toFloat(),
            /* top = */ (playerTop.toFloat() - playerHeight),
            /* right = */ (playerLeft + playerWidth).toFloat(),
            /* bottom = */ (playerTop).toFloat()
        )
}


const val WIDTH_REDUCE_FACTOR = 0.5f
const val HEIGHT_REDUCE_FACTOR = 0.5f

const val WIDTH_INCREASE_FACTOR = 0.5f
const val HEIGHT_INCREASE_FACTOR = 0.5f


const val PLAYER_BOTTOM_PADDING = 160


val TOP_FALLING_PADDING = 90.toPx()

val OBSTACLE_FACTOR = 0.8f
var COLLECTIBLE_FACTOR = 0.6f

