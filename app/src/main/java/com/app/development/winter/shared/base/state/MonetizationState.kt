package com.app.development.winter.shared.base.state

import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.ui.user.model.CanWatchVideoResponse
import com.app.development.winter.ui.user.model.EarnCoins
import com.monetizationlib.data.ads.AdType


data class MonetizationState(
    val loadingState: Pair<String, AdvanceBaseViewModel.LoadingState> = Pair(
        "", AdvanceBaseViewModel.LoadingState.PROCESSING
    ),
    val reward: EarnCoins? = null,
    val requestAdType: AdType,
    val canWatchVideoResponse: CanWatchVideoResponse?,
    val errorMessage: String? = null,
) : ViewState {

    companion object {
        val defaultValue = MonetizationState(
            loadingState = Pair(
                "", AdvanceBaseViewModel.LoadingState.PROCESSING
            ),
            reward = null,
            canWatchVideoResponse = null,
            requestAdType = AdType.Interstitial,
            errorMessage = null
        )
    }
}
