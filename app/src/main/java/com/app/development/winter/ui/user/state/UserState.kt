package com.app.development.winter.ui.user.state

import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.shared.model.AppConfig
import com.app.development.winter.shared.model.SessionInfo
import com.app.development.winter.shared.model.User
import com.app.development.winter.shared.network.data.BaseModel
import com.app.development.winter.ui.user.model.ReferralParams


data class UserState(
    val loadingState: Pair<String, AdvanceBaseViewModel.LoadingState> = Pair(
        "", AdvanceBaseViewModel.LoadingState.PROCESSING
    ),
    val appConfig: AppConfig?,
    val appSession: SessionInfo?,
    val referralParams: ReferralParams?,
    val userInfo: User?,
    val deleteUser: BaseModel?,
    val errorMessage: String?,

    ) : ViewState {

    companion object {
        val defaultValue = UserState(
            loadingState = Pair(
                "", AdvanceBaseViewModel.LoadingState.PROCESSING
            ),
            appConfig = null,
            appSession = null,
            referralParams = null,
            userInfo = null,
            deleteUser = null,
            errorMessage = null,
        )
    }
}
