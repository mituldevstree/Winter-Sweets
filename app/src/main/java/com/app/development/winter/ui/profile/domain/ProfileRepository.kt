package com.app.development.winter.ui.profile.domain


import com.app.development.winter.BuildConfig
import com.app.development.winter.shared.network.client.RetroClient.okHttpClient
import com.app.development.winter.shared.network.client.createWebService
import com.app.development.winter.shared.network.data.BaseApiResponse
import com.app.development.winter.shared.network.data.ListBaseModel
import com.app.development.winter.ui.profile.model.ProfileEarnings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext


object ProfileRepository : BaseApiResponse(), CoroutineScope {
    private val profileApiClient = createWebService(
        BuildConfig.SERVER_URL, ProfileApiService::class.java, okHttpClient = okHttpClient
    )
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO


    /*   fun getReferralLeaderboard(
           params: MutableMap<String, Any>?,
           dispatcher: CoroutineContext,
       ): Flow<ObjectBaseModel<List<MyReferral>?>> {
           return flow {
               kotlin.runCatching {
                   emit(safeApiCall { PROFILE_API_SERVICE.getReferralLeaderboard(params) })
               }
           }.flowOn(dispatcher)
       }*/

    fun getProfileEarnings(
        params: MutableMap<String, Any>?,
        dispatcher: CoroutineContext,
    ): Flow<ListBaseModel<ProfileEarnings>?> {
        return flow<ListBaseModel<ProfileEarnings>?> {
            kotlin.runCatching {
                emit(safeApiCallList { profileApiClient.getProfileEarnings(params) })
            }
        }.flowOn(dispatcher)
    }
}