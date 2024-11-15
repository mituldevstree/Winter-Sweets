package com.app.development.winter.ui.session.service

import com.app.development.winter.ui.session.model.FloatingViewState
import com.app.development.winter.ui.user.model.CycleAdsEarnings

data class FloatingViewServiceState(
    val floatingViewState: FloatingViewState = FloatingViewState.CYCLE_ADS_ENDED,
    val cycleAdsEarning: CycleAdsEarnings? = null
)
