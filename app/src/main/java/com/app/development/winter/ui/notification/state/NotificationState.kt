package com.app.development.winter.ui.notification.state

import androidx.paging.PagingData
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewState

import com.app.development.winter.ui.notification.model.Notification

data class NotificationState(
    val loadingState: Pair<AdvanceBaseViewModel.LoadingType, AdvanceBaseViewModel.LoadingState> = Pair(
        AdvanceBaseViewModel.LoadingType.EMPTY_SATE, AdvanceBaseViewModel.LoadingState.PROCESSING
    ), val errorMessage: String?, val notificationList: PagingData<Notification>?
) : ViewState {

    companion object {
        val defaultValue = NotificationState(
            loadingState = Pair(
                AdvanceBaseViewModel.LoadingType.EMPTY_SATE,
                AdvanceBaseViewModel.LoadingState.PROCESSING
            ), errorMessage = null, notificationList = null
        )
    }
}
