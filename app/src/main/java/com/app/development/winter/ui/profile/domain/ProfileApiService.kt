package com.app.development.winter.ui.profile.domain


import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.data.ListBaseModel
import com.app.development.winter.ui.profile.model.ProfileEarnings
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {

    /*    @POST(ApiEndpoints.GET_REFERRAL_LEADERBOARD)
        suspend fun getReferralLeaderboard(@Body params: MutableMap<String, Any>?): Response<ObjectBaseModel<List<MyReferral>?>>*/

    @POST(ApiEndpoints.GET_PROFILE_EARNINGS)
    suspend fun getProfileEarnings(@Body params: MutableMap<String, Any>?): Response<ListBaseModel<ProfileEarnings>>

}