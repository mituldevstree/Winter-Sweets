package com.app.development.winter.ui.leaderboard.domain

import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.data.ListBaseModel
import com.app.development.winter.ui.leaderboard.model.LeaderBoardUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface LeaderboardApiService {

    @POST(ApiEndpoints.GET_LEADERBOARD_USERS)
    suspend fun getLeaderboardUsers(@Body params: MutableMap<String, Any>?): Response<ListBaseModel<LeaderBoardUser>>

}