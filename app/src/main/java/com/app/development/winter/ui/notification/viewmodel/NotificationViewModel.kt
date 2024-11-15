package com.app.development.winter.ui.notification.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.paging.CommonPagingSource
import com.app.development.winter.ui.notification.event.NotificationEvent
import com.app.development.winter.ui.notification.model.Notification
import com.app.development.winter.ui.notification.state.NotificationState
import com.app.development.winter.ui.notification.state.NotificationState.Companion.defaultValue
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NotificationViewModel : AdvanceBaseViewModel<NotificationEvent, NotificationState>(defaultValue) {

    fun getPagingState() = _state

    override fun reduceState(
        currentState: NotificationState,
        event: NotificationEvent,
    ): NotificationState = when (event) {
        is NotificationEvent.RequestNotifications -> {
            getNotificationList()
            currentState.copy(
                loadingState = Pair(
                    LoadingType.GET_NOTIFICATION_STATE, LoadingState.PROCESSING
                ),
            )
        }

        is NotificationEvent.OnNotificationResponse -> {
            currentState.copy(
                loadingState = Pair(
                    LoadingType.GET_NOTIFICATION_STATE, LoadingState.COMPLETED
                ), notificationList = event.notificationList
            )
        }
        is NotificationEvent.OnResponseFailure -> {
            currentState.copy(
                loadingState = Pair(
                    LoadingType.GET_NOTIFICATION_STATE, LoadingState.ERROR
                ), errorMessage = event.message
            )
        }
    }

    private fun getNotificationList() {
        val param = HashMap<String, Any>()
        if (LocalDataHelper.getUserDetail()?.id != null) {
            param["userId"] = LocalDataHelper.getUserDetail()?.id.toString()
        }

        viewModelScope.launch {
            Pager(
                PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    prefetchDistance = 5,
                    initialLoadSize = 20
                )
            ) {
                CommonPagingSource(
                    ApiEndpoints.GET_NOTIFICATION, param
                )
            }.flow.cachedIn(viewModelScope).catch { exception ->
                exception.message?.let { _state.handleEvent(NotificationEvent.OnResponseFailure(it)) }
            }.collect { result ->
                _state.handleEvent(NotificationEvent.OnNotificationResponse(result as PagingData<Notification>))
            }
        }
    }
}
