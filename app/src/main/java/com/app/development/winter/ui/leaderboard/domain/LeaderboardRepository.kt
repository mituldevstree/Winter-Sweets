package com.app.development.winter.ui.leaderboard.domain


import com.app.development.winter.BuildConfig
import com.app.development.winter.shared.network.client.RetroClient.okHttpClient
import com.app.development.winter.shared.network.client.createWebService
import com.app.development.winter.shared.network.data.BaseApiResponse
import com.app.development.winter.shared.network.data.ListBaseModel
import com.app.development.winter.ui.leaderboard.model.LeaderBoardUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext


object LeaderboardRepository : BaseApiResponse(), CoroutineScope {
    private val leaderboardApiService = createWebService(
        BuildConfig.SERVER_URL, LeaderboardApiService::class.java, okHttpClient = okHttpClient
    )
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    fun getLeaderboardUsers(
        params: MutableMap<String, Any>?,
        dispatcher: CoroutineContext,
    ): Flow<ListBaseModel<LeaderBoardUser>?> {
        return flow<ListBaseModel<LeaderBoardUser>?> {
            kotlin.runCatching {
                emit(safeApiCallList { leaderboardApiService.getLeaderboardUsers(params) })
            }
        }.flowOn(dispatcher)
    }
}