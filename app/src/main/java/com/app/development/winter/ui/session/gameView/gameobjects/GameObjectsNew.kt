package com.app.development.winter.ui.session.gameView.gameobjects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import com.app.development.winter.BuildConfig
import kotlin.math.abs

private fun isVisibleOnScreen(yPosition: Float, gameState: GameStateNew): Boolean {
    return yPosition >= 0 && yPosition <= gameState.gameViewHeight
}

abstract class GameObjectsNew {
    abstract val lane: Int
    abstract var startY: Float
    abstract var bitmap: Bitmap
    abstract var rotation: Float

    var rectObject: RectF = RectF()
    abstract fun draw(
        canvas: Canvas,
        gameState: GameStateNew
    )

    abstract fun generateNewObject(gameState: GameStateNew): Boolean

    abstract fun isDestroyed(
        gameState: GameStateNew
    ): Boolean

    abstract fun isOutOfScreen(gameState: GameStateNew): Boolean

    fun readyToPrintBoxInDebug(gameState: GameStateNew, canvas: Canvas) {
        if (BuildConfig.DEBUG && gameState.config.showOverlayBox) {
            canvas.drawRect(rectObject, gameState.boxPaint) // Draw the rectangle
            canvas.drawRect(gameState.playerReact, gameState.boxPaint) // Draw the rectangle
        }
    }

    fun rotation() {
        rotation += 0.5f
        if (rotation > 360) {
            rotation = 0f
        }
    }
}

data class ObstacleNew(
    override var lane: Int,
    override var startY: Float,
    override var bitmap: Bitmap,
    override var rotation: Float,
    var isCollusionHappen: Boolean = false,
) :
    GameObjectsNew() {


    override fun draw(
        canvas: Canvas,
        gameState: GameStateNew
    ) {

        val obstacleX =
            (lane * gameState.columnWidth) + (gameState.columnWidth / 2) - (gameState.playerWidth / 2)
        val getCurrentGameLevel = abs(gameState.totalScore / 10)
        startY =
            startY.plus(gameState.currentGameSpeed.minus((gameState.config.initialGameSpeed + getCurrentGameLevel)))
        if (isVisibleOnScreen(startY, gameState).not()) return
        canvas.save()
        val reduceFactor = OBSTACLE_FACTOR // Reduce to 50%
        val reducedWidth = (gameState.playerWidth * reduceFactor).toInt()
        val reducedHeight = (gameState.playerWidth * reduceFactor).toInt()
//        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, reducedWidth, reducedHeight, true)
        val centeredX =
            obstacleX + (gameState.playerWidth - reducedWidth) / 2 // Center horizontally
        val centeredY = startY - reducedHeight // Center vertically above the Y position
        rectObject.left = centeredX.toFloat()
        rectObject.top = centeredY
        rectObject.right = centeredX.toFloat() + reducedWidth.toFloat()
        rectObject.bottom = centeredY + reducedHeight.toFloat()

        readyToPrintBoxInDebug(gameState, canvas)
        canvas.drawBitmap(bitmap.rotate(rotation), centeredX.toFloat(), centeredY, null)
        canvas.restore()
    }

    override fun generateNewObject(gameState: GameStateNew): Boolean {
        if (startY >= gameState.gameViewHeight.minus(gameState.gameViewHeight / 3)) {
            if (gameState.needToGenerateNewObject) {
                gameState.needToGenerateNewObject = false
                return true
            }
        }
        return false
    }


    override fun isDestroyed(
        gameState: GameStateNew
    ): Boolean {
        if (lane == gameState.playerColumnPosition && !isCollusionHappen) {
            return rectObject.intersect(gameState.playerReact)
        }
        return false
    }

    override fun isOutOfScreen(gameState: GameStateNew): Boolean {
        if (startY > (gameState.gameViewHeight + (gameState.playerHeight))) {
            return true
        }
        return false
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

data class CoinNew(
    override var lane: Int,
    override var startY: Float,
    override var bitmap: Bitmap,
    override var rotation: Float,
    var isCollusionHappen: Boolean = false
) :
    GameObjectsNew() {

    override fun draw(
        canvas: Canvas,
        gameState: GameStateNew
    ) {

        // Calculate the X position for the object
        val objectX =
            (lane * gameState.columnWidth) + (gameState.columnWidth / 2) - (gameState.playerWidth / 2)
        val getCurrentGameLevel = abs(gameState.totalScore / 10)
        startY =
            startY.plus(gameState.currentGameSpeed.minus((gameState.config.initialGameSpeed + getCurrentGameLevel)))
        if (isVisibleOnScreen(startY, gameState).not()) return
        // Calculate the Y position for the object
//        val objectY = (gameState.consumedTime - startY)* LEVEL_SPEED

        val reduceFactor = COLLECTIBLE_FACTOR // Reduce to 50%

        // Specify the reduced size for the bitmap
        val reducedWidth = (gameState.playerWidth * reduceFactor).toInt()
        val reducedHeight = (gameState.playerWidth * reduceFactor).toInt()

        // Create a scaled bitmap
//        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, reducedWidth, reducedHeight, true)

        // Center the bitmap
        val centeredX = objectX + (gameState.playerWidth - reducedWidth) / 2 // Center horizontally
        val centeredY = startY - reducedHeight // Center vertically above the Y position

        rectObject.left = centeredX.toFloat()
        rectObject.top = centeredY
        rectObject.right = centeredX.toFloat() + reducedWidth.toFloat()
        rectObject.bottom = centeredY + reducedHeight.toFloat()
        // Draw the scaled bitmap on the canvas
        readyToPrintBoxInDebug(gameState, canvas)
        //canvas.rotate(rotation, rectObject.centerX(), rectObject.centerY())
        canvas.drawBitmap(bitmap.rotate(rotation), centeredX.toFloat(), centeredY, null)
    }

    override fun generateNewObject(gameState: GameStateNew): Boolean {
        return false
    }


    override fun isDestroyed(
        gameState: GameStateNew
    ): Boolean {
        if (lane == gameState.playerColumnPosition) {
            return rectObject.intersect(gameState.playerReact)
        }

        return false
    }

    override fun isOutOfScreen(gameState: GameStateNew): Boolean {
        if (startY > gameState.gameViewHeight) {
            return true
        }
        return false
    }

}