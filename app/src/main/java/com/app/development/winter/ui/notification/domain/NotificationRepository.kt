package com.app.development.winter.ui.notification.domain

import com.app.development.winter.BuildConfig
import com.app.development.winter.shared.network.client.RetroClient.unEncryptInterceptorClient
import com.app.development.winter.shared.network.client.createWebService
import com.app.development.winter.shared.network.data.BaseApiResponse
import com.app.development.winter.shared.network.data.ListBaseModel
import com.app.development.winter.ui.notification.model.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext


object NotificationRepository : BaseApiResponse(), CoroutineScope {
    private val notificationApiService = createWebService(
        BuildConfig.NOTIFICATION_URL, NotificationApiService::class.java, okHttpClient = unEncryptInterceptorClient
    )
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO


    fun getLatestNotifications(
        params: MutableMap<String, Any>?
    ): Flow<ListBaseModel<Notification>?> {
        return flow<ListBaseModel<Notification>?> {
            kotlin.runCatching {
                emit(safeApiCallList { notificationApiService.getLatestNotifications(params) })
            }
        }.flowOn(coroutineContext)
    }

}