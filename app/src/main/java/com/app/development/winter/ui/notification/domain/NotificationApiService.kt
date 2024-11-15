package com.app.development.winter.ui.notification.domain

import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.data.ListBaseModel
import com.app.development.winter.ui.notification.model.Notification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApiService {
    @POST(ApiEndpoints.GET_LATEST_NOTIFICATION)
    suspend fun getLatestNotifications(@Body params: MutableMap<String, Any>?): Response<ListBaseModel<Notification>>
}