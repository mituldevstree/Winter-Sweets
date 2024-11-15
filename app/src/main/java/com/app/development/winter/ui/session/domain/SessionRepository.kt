package com.app.development.winter.ui.session.domain

import com.app.development.winter.BuildConfig
import com.app.development.winter.shared.model.UserStatistics
import com.app.development.winter.shared.network.client.RetroClient.okHttpClient
import com.app.development.winter.shared.network.client.createWebService
import com.app.development.winter.shared.network.data.BaseApiResponse
import com.app.development.winter.shared.network.data.ObjectBaseModel
import com.app.development.winter.shared.network.domain.SessionApiService
import com.app.development.winter.ui.session.model.SessionData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

object SessionRepository : BaseApiResponse(), CoroutineScope {
    private val sessionApiClient = createWebService(
        BuildConfig.SERVER_URL, SessionApiService::class.java, okHttpClient = okHttpClient
    )

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO


    suspend fun createNewSession(
        param: MutableMap<String, Any>,
    ): Flow<ObjectBaseModel<SessionData>?> {
        return flow<ObjectBaseModel<SessionData>?> {
            kotlin.runCatching {
                emit(safeApiCall { sessionApiClient.createNewSession(param) })
            }
        }.flowOn(coroutineContext)

    }

    suspend fun getActiveSession(
        param: MutableMap<String, Any>,
    ): Flow<ObjectBaseModel<SessionData>?> {
        return flow<ObjectBaseModel<SessionData>?> {
            kotlin.runCatching {
                emit(safeApiCall { sessionApiClient.getActiveSession(param) })
            }
        }.flowOn(coroutineContext)

    }

    suspend fun endActiveSession(
        param: MutableMap<String, Any>,
    ): Flow<ObjectBaseModel<SessionData>?> {
        return flow<ObjectBaseModel<SessionData>?> {
            kotlin.runCatching {
                emit(safeApiCall { sessionApiClient.endActiveSession(param) })
            }
        }.flowOn(coroutineContext)

    }

    suspend fun getUserStatistics(
        param: MutableMap<String, Any>,
    ): Flow<ObjectBaseModel<UserStatistics>?> {
        return flow<ObjectBaseModel<UserStatistics>?> {
            kotlin.runCatching {
                emit(safeApiCall { sessionApiClient.getUserStatistics(param) })
            }
        }.flowOn(coroutineContext)

    }

    suspend fun saveFocusedTime(
        param: MutableMap<String, Any>
    ): Flow<ObjectBaseModel<SessionData>?> {
        return flow<ObjectBaseModel<SessionData>?> {
            kotlin.runCatching {
                emit(safeApiCall { sessionApiClient.saveFocusedTime(param) })
            }
        }.flowOn(coroutineContext)
    }
}