package com.app.development.winter.ui.session.state

import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.shared.model.UserStatistics
import com.app.development.winter.shared.network.data.BaseModel
import com.app.development.winter.ui.session.model.SessionData
import com.app.development.winter.ui.user.model.CycleAdsEarnings

data class SessionUiState(
    var loadingState: Pair<AdvanceBaseViewModel.LoadingType, AdvanceBaseViewModel.LoadingState> = Pair(
        AdvanceBaseViewModel.LoadingType.EMPTY_SATE, AdvanceBaseViewModel.LoadingState.PROCESSING
    ),
    var activeSession: SessionData? = null,
    var syncSession: SessionData? = null,
    var result: BaseModel? = null,
    var cycleAdsEarning: CycleAdsEarnings? = null,
    var userXpStateData: UserStatistics? = null,
    var errorMessage: String? = null
) : ViewState {

    companion object {
        val defaultValue = SessionUiState(
            loadingState = Pair(
                AdvanceBaseViewModel.LoadingType.EMPTY_SATE,
                AdvanceBaseViewModel.LoadingState.PROCESSING
            ),
            activeSession = null,
            result = null,
            cycleAdsEarning = null,
            errorMessage = null,
        )
    }

}
