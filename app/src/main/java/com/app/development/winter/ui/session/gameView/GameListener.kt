package com.app.development.winter.ui.session.gameView

import com.app.development.winter.ui.session.gameView.gameobjects.GameStatus

interface GameListener {
    fun onGameStateChange(currentState: GameStatus, totalScore:Int, hasUserQuit:Boolean)
}

