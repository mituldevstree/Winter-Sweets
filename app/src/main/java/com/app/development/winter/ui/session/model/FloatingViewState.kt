package com.app.development.winter.ui.session.model

import androidx.annotation.Keep

@Keep
enum class FloatingViewState {
    CYCLE_ADS_ENDED, CYCLE_ADS_STARTED, ACTION_NAVIGATION_BAR, STOP_SERVICE, LIMIT_REACHED, EMPTY, STOP_STATS_VIEW, ON_BACK_PRESSED, MINIMIZE_APP
}