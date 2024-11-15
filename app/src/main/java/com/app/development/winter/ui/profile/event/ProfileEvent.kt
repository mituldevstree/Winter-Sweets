package com.app.development.winter.ui.profile.event

import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.ui.profile.model.ProfileEarnings
import com.app.development.winter.ui.profile.state.ProfileEarningState

sealed class ProfileEvent : ViewIntent {

    data class RequestGetEarnings(val type: ProfileEarningState.Companion.EarningFilterType) :
        ProfileEvent()

    data class OnEarningStatsAvailable(
        val data: MutableList<ProfileEarnings>?,
        val type: ProfileEarningState.Companion.EarningFilterType
    ) : ProfileEvent()

    data class OnFailure(val errorMessage: String?) : ProfileEvent()

}