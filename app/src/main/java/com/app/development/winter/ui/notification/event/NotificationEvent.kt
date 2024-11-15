package com.app.development.winter.ui.notification.event

import androidx.paging.PagingData
import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.ui.notification.model.Notification

sealed class NotificationEvent : ViewIntent {
    data class OnResponseFailure(val message: String) : NotificationEvent()
    data object RequestNotifications : NotificationEvent()
    data class OnNotificationResponse(val notificationList: PagingData<Notification>?) : NotificationEvent()

}