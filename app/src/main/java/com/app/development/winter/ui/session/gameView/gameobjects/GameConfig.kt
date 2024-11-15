package com.app.development.winter.ui.session.gameView.gameobjects

import android.graphics.Color

data class GameConfig(
    var initialPlayerLives: Int = 1,
    var showOverlayBox: Boolean = false,
    var showLineSeparator: Boolean = false,
    var enableSmoothMovement: Boolean = false,
    var enableLaneChangeRotation: Boolean = false,
    var lineSeparatorEndColor: Int = Color.WHITE,
    var lineSeparatorStartColor: Int = Color.WHITE,
    var coinsNeededForSpeedIncrease: Int = 10,
    var isSoundMute: Boolean = false,
    var initialGameSpeed: Float = 12f,
    var incrementFactor: Float = 0.18f,
    var playerCollisionBoxPadding: PaddingValue = PaddingValue()
)