package com.app.development.winter.ui.session.event


import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.shared.model.UserStatistics
import com.app.development.winter.ui.session.model.SessionData
import com.app.development.winter.ui.user.model.CycleAdsEarnings

sealed class SessionEvent : ViewIntent {

    // Get game info
    data class RequestOngoingSessionInfo(val wantToCreateSession: Boolean = false) : SessionEvent()
    data class ResponseOngoingSessionInfo(
        val sessionData: SessionData?,
        val wantToCreateSession: Boolean = false
    ) : SessionEvent()

    data class ResponseNewSessionInfo(val sessionData: SessionData?) : SessionEvent()
    data class RequestSyncFocusTime(val focusTimeInSec: String?) : SessionEvent()
    data class ResponseSyncFocusTime(val sessionData: SessionData?) : SessionEvent()
    data class RequestEndSession(val focusTimeInSec: String?) : SessionEvent()
    data class ResponseEndSession(val sessionData: SessionData?) : SessionEvent()
    data object RequestRewardForAd : SessionEvent()
    data class ResponseRewardForAd(val reward: CycleAdsEarnings?) : SessionEvent()
    data object RequestUserStatistics : SessionEvent()
    data class ResponseUserStatistics(val userState: UserStatistics) : SessionEvent()
    data class ErrorSessionInfo(
        val loadingType: AdvanceBaseViewModel.LoadingType,
        val errorMessage: String
    ) : SessionEvent()
}