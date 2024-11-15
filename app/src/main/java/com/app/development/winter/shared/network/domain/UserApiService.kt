package com.app.development.winter.shared.network.domain


import com.app.development.winter.shared.model.AppConfig
import com.app.development.winter.shared.model.SessionInfo
import com.app.development.winter.shared.model.User
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.data.BaseModel
import com.app.development.winter.shared.network.data.ListBaseModel
import com.app.development.winter.shared.network.data.ObjectBaseModel
import com.app.development.winter.ui.notification.model.Notification
import com.app.development.winter.ui.user.model.CanWatchVideoResponse
import com.app.development.winter.ui.user.model.CycleAdsEarnings
import com.app.development.winter.ui.user.model.EarnCoins
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface UserApiService {
    @POST(ApiEndpoints.SOCIAL_LOGIN)
    suspend fun socialLogin(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<User>>

    @POST(ApiEndpoints.GET_USER)
    suspend fun getUser(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<User>>

    @POST(ApiEndpoints.ADD_FIRST_TIME_PROFILE)
    suspend fun updateRegisteredDeviceUserInfo(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<SessionInfo>>

    @Headers("Content-Type: application/json")
    @POST(ApiEndpoints.UPLOAD_USER_MEDIA_URL)
    suspend fun linkSocialMediaProfilePhoto(@Body params: String): Response<ObjectBaseModel<User>>

    @Headers("Content-Type: application/json")
    @POST(ApiEndpoints.UPLOAD_USER_PHOTO)
    suspend fun uploadUserProfilePhoto(@Body params: String): Response<ObjectBaseModel<User>>

    @POST(ApiEndpoints.UPDATE_USER_PROFILE)
    suspend fun updateUserProfile(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<User>>

    @POST(ApiEndpoints.REGISTER_FCM_TOKEN)
    fun setFcmToken(@Body param: MutableMap<String, String>?): Call<BaseModel>?

    @POST(ApiEndpoints.GET_NOTIFICATION)
    suspend fun getNotifications(@Body params: MutableMap<String, Any>): Response<ListBaseModel<Notification>>

    @POST(ApiEndpoints.GET_AD_REWARD)
    suspend fun dailyVideoGif(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<EarnCoins>>

    @POST(ApiEndpoints.CHECK_FOR_AD_TYPE)
    suspend fun canWatchVideo(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<CanWatchVideoResponse>>

    @POST(ApiEndpoints.CLICK_ON_AD)
    suspend fun transmitAdClick(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<User>>

    @POST(ApiEndpoints.GET_CYCLIC_AD_REWARD)
    suspend fun getCyclicAdReward(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<CycleAdsEarnings>>

    @POST(ApiEndpoints.VERSION_CHECK)
    suspend fun versionCheck(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<AppConfig>>

    @POST(ApiEndpoints.REGISTER_DEVICE)
    suspend fun registerDevice(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<SessionInfo>>

    @POST(ApiEndpoints.DELETE_USER)
    suspend fun deleteUser(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<BaseModel>>

    @POST(ApiEndpoints.SEND_FEEDBACK)
    suspend fun sendFeedback(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<BaseModel>>

    @POST(ApiEndpoints.HIDE_LOGIN)
    suspend fun hiddenLogin(@Body params: MutableMap<String, Any>): Response<ObjectBaseModel<BaseModel>>

}