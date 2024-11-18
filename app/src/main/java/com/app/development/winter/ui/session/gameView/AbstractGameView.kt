package com.app.development.winter.ui.session.gameView

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import com.app.development.winter.R
import com.app.development.winter.shared.extension.getDrawable
import com.app.development.winter.shared.extension.toJson
import com.app.development.winter.ui.session.gameView.gameUtils.CoinCollectEffect
import com.app.development.winter.ui.session.gameView.gameUtils.GameSoundEffectPlayer
import com.app.development.winter.ui.session.gameView.gameobjects.CoinNew
import com.app.development.winter.ui.session.gameView.gameobjects.GameConfig
import com.app.development.winter.ui.session.gameView.gameobjects.GameObjectsNew
import com.app.development.winter.ui.session.gameView.gameobjects.GameStatus
import com.app.development.winter.ui.session.gameView.gameobjects.GameStateNew
import com.app.development.winter.ui.session.gameView.gameobjects.ObstacleNew
import com.app.development.winter.ui.session.gameView.gameobjects.PlayerFacing
import com.app.development.winter.ui.session.gameView.gameobjects.TOP_FALLING_PADDING
import kotlinx.coroutines.Job
import java.util.LinkedList
import kotlin.math.abs

abstract class AbstractGameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    protected var gameThreadJob: Job? = null

    protected val canvasPaint by lazy { Paint() }
    protected val gameSoundPlayer by lazy { GameSoundEffectPlayer(context) }
    private val collectedCoinEffects = mutableListOf<CoinCollectEffect>()
    private var currentPlayerFacing = PlayerFacing.FACING_LEFT
    private val maxRotationAngle = 15f

    private var movementAnimator: ValueAnimator? = null
    private val movementQueue = LinkedList<Int>()

    protected var myGameObjects = ArrayList<GameObjectsNew>()
    private var characterObjectAsset =
        ContextCompat.getDrawable(context, R.drawable.ic_player_character)
    private val explosionDrawable by lazy {
        ContextCompat.getDrawable(
            context, R.drawable.explosion
        )
    }

    private val explosionDrawableHeight = 100
    private val explosionDrawableWidth = 100

    private val collectibleObjectAsset by lazy {
        listOf(
            getDrawable(R.drawable.ic_collect_one).toBitmap(),
            getDrawable(R.drawable.ic_collect_two).toBitmap(),
            getDrawable(R.drawable.ic_collect_three).toBitmap(),
        )
    }
    private val callableObjectAsset by lazy {
        listOf(
            getDrawable(R.drawable.ic_obstacle_first).toBitmap(),
            getDrawable(R.drawable.ic_obstacle_second).toBitmap(),
        )
    }

    protected val stringScore by lazy { "Score" }
    protected val stringSpeed by lazy { "Speed" }
    protected val stringDamage by lazy { "Damage" }
    protected val stringAvailableLives by lazy { "Lives" }

    protected var isExplosionVisible = false
    private var explosionDrawableTime = 10


    var gameState = GameStateNew()
    var gameConfig = GameConfig()

    protected var listener: GameListener? = null

    abstract fun gameThread()

    fun setGameListener(listener: GameListener): AbstractGameView {
        this.listener = listener
        return this
    }

    fun setGameConfig(builderAction: GameConfig.() -> Unit) {
        gameConfig.apply(builderAction)
        gameState.setGameConfig(gameConfig)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gameState.gameViewWidth = w
        gameState.gameViewHeight = h
        // Calculate robot width and height only once when the size changes
        gameState.playerWidth = gameState.gameViewWidth / 5
        gameState.playerHeight = gameState.playerWidth + 10
        gameState.columnWidth = gameState.gameViewWidth / 4

        canvasPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
    }

    fun obstacleIterator(
        iterator: IntIterator,
        laneList: MutableList<Int>,
        callback: (MutableList<Int>) -> Unit
    ) {
        if (iterator.hasNext()) {
            val objectNo = iterator.next()
            Log.e("Iterator", "NextItemTriggered ${objectNo}")
            generateObstacles(laneList) {
                Log.e("Iterator", "MoveToNext")
                obstacleIterator(iterator, it, callback)
            }
        } else {
            callback.invoke(laneList)
        }
    }

    fun generateCollectibles(lanes: MutableList<Int>) {
        Log.e("Generate", "Coint:Triggered")
        if (lanes.isEmpty()) {
            return
        } else {
            val lane = lanes.random()
            lanes.remove(lane)
            val reduceFactor = 0.6f
            val reducedWidth = (gameState.playerWidth * reduceFactor).toInt()
            val reducedHeight = (gameState.playerHeight * reduceFactor).toInt()
            myGameObjects.add(
                CoinNew(
                    lane = lane,
                    startY = gameState.currentGameSpeed,
                    bitmap = collectibleObjectAsset.random().scale(reducedWidth, reducedHeight),
                )
            )
        }

    }

    private fun generateObstacles(
        seedRange: MutableList<Int>,
        callback: (MutableList<Int>) -> Unit
    ) {
        if (seedRange.isEmpty()) callback.invoke(seedRange)
        val firstObjectLane = (seedRange).random()
        Log.e("Iterator", "Seed${seedRange.toJson()}")
        seedRange.remove(firstObjectLane)
        val reduceFactor = 0.8f // Reduce to 50%
        val reducedWidth = (gameState.playerWidth * reduceFactor).toInt()
        val reducedHeight = (gameState.playerHeight * reduceFactor).toInt()
        myGameObjects.add(
            ObstacleNew(
                lane = firstObjectLane,
                startY = -(0..gameState.gameViewHeight).random().toFloat(),
                isCollusionHappen = false,
                bitmap = callableObjectAsset.random().scale(reducedWidth, reducedHeight)
            )
        )
        //  }
        callback.invoke(seedRange)

    }

    protected fun drawPlayer(canvas: Canvas) {

        val playerWidth = (gameState.playerWidth)
        val aspectRatio = (characterObjectAsset?.intrinsicHeight
            ?: 1).toFloat() / (characterObjectAsset?.intrinsicWidth ?: 1)

        val playerHeight = (playerWidth * aspectRatio).toInt()

        if (gameState.playerHeight != playerHeight) {
            gameState.playerHeight = playerHeight
        }

        canvas.save()
        canvas.translate(gameState.playerLeft, gameState.playerTop)

        if (gameState.config.enableLaneChangeRotation) {
            canvas.rotate(
                gameState.currentPlayerRotation,
                (playerWidth / 2).toFloat(),
                (playerHeight / 2).toFloat()
            )
        }

        characterObjectAsset?.setBounds(
            0,
            0,
            playerWidth,
            playerHeight - characterGrowingFactor.toInt()
        )
        characterObjectAsset?.draw(canvas)
        canvas.restore()

        manageCharacterGrowing()
    }

    private var characterGrowingFactor = 15.0
    private var isCharacterGrowing = false
    private fun manageCharacterGrowing() {
        if (gameState.currentGameStatus == GameStatus.RUNNING) {
            if (isCharacterGrowing) {
                characterGrowingFactor -= 0.5
                if (characterGrowingFactor < 0) {
                    isCharacterGrowing = false
                }
            } else {
                characterGrowingFactor += 0.5
                if (characterGrowingFactor > 15) {
                    isCharacterGrowing = true
                }
            }
        }
    }

    protected fun explosionEffect(canvas: Canvas) {
        if (isExplosionVisible.not()) return

        val explosionLeft =
            (gameState.playerLeft + (gameState.playerWidth / 2) - (explosionDrawableWidth / 2)).toInt()
        val explosionTop = (gameState.playerTop - explosionDrawableHeight).toInt()
        val explosionRight = explosionLeft + explosionDrawableWidth
        val explosionBottom = explosionTop + explosionDrawableHeight

        explosionDrawable?.setBounds(
            explosionLeft,
            explosionTop,
            explosionRight,
            explosionBottom
        )
        explosionDrawable?.draw(canvas)

        explosionDrawableTime--
        if (explosionDrawableTime <= 0) {
            isExplosionVisible = false
            explosionDrawableTime = 10
        } else {
            explosionEffect(canvas)
        }
    }

    protected fun drawLaneSeparators(canvas: Canvas) {
        if (gameState.config.showLineSeparator.not()) return
        val gradient = LinearGradient(
            0f, 0f, 0f, gameState.gameViewHeight.toFloat(),
            gameState.config.lineSeparatorStartColor,
            gameState.config.lineSeparatorEndColor,
            Shader.TileMode.CLAMP
        )

        canvasPaint.color = Color.GRAY // Fallback color if needed
        canvasPaint.strokeWidth = 3f // Width of the separators
        canvasPaint.pathEffect =
            DashPathEffect(floatArrayOf(10f, 10f), 0f) // Define dash and gap lengths

        canvasPaint.shader = gradient

        val laneWidth = gameState.gameViewWidth / 4
        for (i in 1 until 4) { // Draw separators between 4 lanes
            val x = (i * laneWidth).toFloat() // Centered between lanes
            canvas.drawLine(
                x,
                TOP_FALLING_PADDING.toFloat(),
                x,
                (gameState.gameViewHeight / 1.2).toFloat(),
                canvasPaint
            )
        }
    }

    protected fun triggerCoinCollectEffect(x: Int, y: Int) {
        collectedCoinEffects.add(CoinCollectEffect(x + (gameState.playerWidth / 2), y - 50, 4.0f))
        collectedCoinEffects.add(CoinCollectEffect(x + (gameState.playerWidth / 2), y - 50, 2.0f))
    }

    protected fun drawCoinCollectEffects(canvas: Canvas) {
        // Iterate through effects and draw them
        val iterator = collectedCoinEffects.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            effect.update() // Update position, scale, etc.
            if (effect.isFinished()) {
                iterator.remove() // Remove if finished
            } else {
                effect.draw(canvas) // Draw effect
            }
        }
    }

    fun moveCharacterToLeft() {
        if (gameState.playerColumnPosition > 0) {
            if (currentPlayerFacing != PlayerFacing.FACING_LEFT) {
                currentPlayerFacing = PlayerFacing.FACING_LEFT
                characterObjectAsset = ContextCompat.getDrawable(
                    context, R.drawable.ic_player_character
                )
            }

            if (gameState.config.enableSmoothMovement) {
                val targetLane = gameState.playerColumnPosition - 1
                movementQueue.add(targetLane)
                processMovementQueue()
            } else {
                gameState.playerColumnPosition--
                gameState.currentPlayerPosition.x =
                    (gameState.playerColumnPosition * gameState.columnWidth).toFloat()
            }
        }
    }

    fun moveCharacterToRight() {
        if (gameState.playerColumnPosition < 3) { // Changed to accommodate 4 lanes
            if (currentPlayerFacing != PlayerFacing.FACING_RIGHT) {
                currentPlayerFacing = PlayerFacing.FACING_RIGHT
                characterObjectAsset = ContextCompat.getDrawable(
                    context, R.drawable.ic_player_character
                )
            }

            if (gameState.config.enableSmoothMovement) {
                val targetLane = gameState.playerColumnPosition + 1
                movementQueue.add(targetLane)
                processMovementQueue()
            } else {
                gameState.playerColumnPosition++
                gameState.currentPlayerPosition.x =
                    (gameState.playerColumnPosition * gameState.columnWidth).toFloat()
            }
        }
    }


    private fun processMovementQueue() {
        if (movementAnimator?.isRunning == true || movementQueue.isEmpty()) {
            return
        }

        val targetLane = movementQueue.poll() ?: 0

        val startX = gameState.currentPlayerPosition.x
        val endX = (targetLane * gameState.columnWidth).toFloat()

        val angle =
            if (targetLane < gameState.playerColumnPosition) -maxRotationAngle else maxRotationAngle

        movementAnimator = ValueAnimator.ofFloat(startX, endX).apply {
            duration = 200L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val currentX = animator.animatedValue as Float
                gameState.currentPlayerPosition.x = currentX

                if (gameState.config.enableLaneChangeRotation) {
                    val deltaX = abs(currentX - startX) / abs(endX - startX)
                    gameState.currentPlayerRotation = angle * (1 - abs(2 * deltaX - 1))
                }
            }

            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    movementAnimator = null
                    processMovementQueue()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })

            start()
        }

        gameState.playerColumnPosition = targetLane
    }

    fun clearGameView() {
        myGameObjects.clear()
    }

    fun startGame() {
        if (gameState.currentGameStatus != GameStatus.RUNNING) {
            gameState.currentGameStatus = GameStatus.RUNNING
            gameThread()
            listener?.onGameStateChange(gameState.currentGameStatus, gameState.totalScore, false)
        }

    }

    fun pauseGame() {
        gameState.currentGameStatus = GameStatus.PAUSE
        gameThreadJob?.cancel()
    }

    fun resumeGame() {
        if (gameState.currentGameStatus != GameStatus.RUNNING) {
            gameState.currentGameStatus = GameStatus.RUNNING
            gameThread()
        }
    }

    protected fun endGame(hasUserQuit: Boolean) {
        // this.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        gameThreadJob?.cancel()
        gameState.currentGameStatus = GameStatus.ENDED
        listener?.onGameStateChange(gameState.currentGameStatus, gameState.totalScore, hasUserQuit)
        clearGameView()
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return this.bitmap
        }
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}