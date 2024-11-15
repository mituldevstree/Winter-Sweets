package com.app.development.winter.shared.network.domain

import com.app.development.winter.shared.model.UserStatistics
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.data.ObjectBaseModel
import com.app.development.winter.ui.session.model.SessionData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SessionApiService {

    @POST(ApiEndpoints.CREATE_NEW_SESSION)
    suspend fun createNewSession(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<SessionData>>

    @POST(ApiEndpoints.GET_ACTIVE_SESSION)
    suspend fun getActiveSession(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<SessionData>>

    @POST(ApiEndpoints.GET_USER_STATISTICS)
    suspend fun getUserStatistics(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<UserStatistics>>

    @POST(ApiEndpoints.END_SESSION)
    suspend fun endActiveSession(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<SessionData>>

    @POST(ApiEndpoints.SAVE_FOCUSED_TIME)
    suspend fun saveFocusedTime(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<SessionData>>

}