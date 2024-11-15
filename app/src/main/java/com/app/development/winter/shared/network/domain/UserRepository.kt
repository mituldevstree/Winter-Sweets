package com.app.development.winter.shared.network.domain

import com.app.development.winter.BuildConfig
import com.app.development.winter.shared.model.AppConfig
import com.app.development.winter.shared.model.SessionInfo
import com.app.development.winter.shared.model.User
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.client.RetroClient
import com.app.development.winter.shared.network.client.createWebService
import com.app.development.winter.shared.network.data.BaseApiResponse
import com.app.development.winter.shared.network.data.BaseModel
import com.app.development.winter.shared.network.data.ObjectBaseModel
import com.app.development.winter.ui.user.model.CanWatchVideoResponse
import com.app.development.winter.ui.user.model.CycleAdsEarnings
import com.app.development.winter.ui.user.model.EarnCoins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

object UserRepository : BaseApiResponse(), CoroutineScope {

    private val appUrlApiClient = createWebService(
        BuildConfig.SERVER_URL, UserApiService::class.java, okHttpClient = RetroClient.okHttpClient
    )
    private val generalUrlApiClient = createWebService(
        BuildConfig.GENERAL_URL,
        UserApiService::class.java,
        okHttpClient = RetroClient.unEncryptInterceptorClient
    )

    private val adClickApiClient = createWebService(
        BuildConfig.NOTIFICATION_URL,
        UserApiService::class.java,
        okHttpClient = RetroClient.unEncryptInterceptorClient
    )
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    suspend fun versionCheck(
        param: MutableMap<String, Any>,
    ): Flow<ObjectBaseModel<AppConfig>?> {
        return flow<ObjectBaseModel<AppConfig>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.versionCheck(param) })
            }
        }.flowOn(coroutineContext)
    }


    suspend fun registerDevice(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<SessionInfo>?> {
        return flow<ObjectBaseModel<SessionInfo>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.registerDevice(param) })
            }
        }.flowOn(dispatcher)
    }

    suspend fun deleteUser(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<BaseModel>?> {
        return flow<ObjectBaseModel<BaseModel>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.deleteUser(param) })
            }
        }.flowOn(dispatcher)
    }

    fun socialLogin(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<User>?> {
        return flow<ObjectBaseModel<User>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.socialLogin(param) })
            }
        }.flowOn(dispatcher)
    }

    fun getUser(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<User>?> {
        return flow<ObjectBaseModel<User>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.getUser(param) })
            }
        }.flowOn(dispatcher)
    }

    fun updateRegisteredDevicesUserInfo(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<SessionInfo>?> {
        return flow<ObjectBaseModel<SessionInfo>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.updateRegisteredDeviceUserInfo(param) })
            }
        }.flowOn(dispatcher)
    }


    fun linkSocialMediaProfile(
        param: String,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<User>?> {
        return flow<ObjectBaseModel<User>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.linkSocialMediaProfilePhoto(param) })
            }
        }.flowOn(dispatcher)
    }

    fun updateUserProfile(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<User>?> {
        return flow<ObjectBaseModel<User>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.updateUserProfile(param) })
            }
        }.flowOn(dispatcher)
    }

    fun uploadUserProfilePhoto(
        param: String,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<User>?> {
        return flow<ObjectBaseModel<User>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.uploadUserProfilePhoto(param) })
            }
        }.flowOn(dispatcher)
    }

    fun setFcmToken(params: MutableMap<String, String>): Flow<BaseModel?> {
        return flow {
            kotlin.runCatching {
                val response = appUrlApiClient.setFcmToken(params)?.execute()
                if (response?.code() != ApiEndpoints.RESPONSE_OK) {
                    throw Throwable(response?.message())
                } else {
                    emit(response.body())
                }
            }
        }.flowOn(coroutineContext)
    }

    suspend fun watchDailyRewardVideo(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<EarnCoins>?> {
        return flow<ObjectBaseModel<EarnCoins>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.dailyVideoGif(param) })
            }
        }.flowOn(dispatcher)
    }

    suspend fun canWatchVideoForReward(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<CanWatchVideoResponse>?> {
        return flow {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.canWatchVideo(param) })
            }
        }.flowOn(dispatcher)
    }

    fun onAdClick(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<User>?> {
        return flow<ObjectBaseModel<User>?> {
            kotlin.runCatching {
                emit(safeApiCall { adClickApiClient.transmitAdClick(param) })
            }
        }.flowOn(dispatcher)
    }

    suspend fun getCyclicAdsReward(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<CycleAdsEarnings>?> {
        return flow<ObjectBaseModel<CycleAdsEarnings>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.getCyclicAdReward(param) })
            }
        }.flowOn(dispatcher)
    }

    fun sendFeedback(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<BaseModel>?> {
        return flow<ObjectBaseModel<BaseModel>?> {
            kotlin.runCatching {
                emit(safeApiCall { appUrlApiClient.sendFeedback(param) })
            }
        }.flowOn(dispatcher)
    }


    fun hiddenLogin(
        param: MutableMap<String, Any>,
        dispatcher: CoroutineContext,
    ): Flow<ObjectBaseModel<BaseModel>?> {
        return flow<ObjectBaseModel<BaseModel>?> {
            kotlin.runCatching {
                emit(safeApiCall { generalUrlApiClient.hiddenLogin(param) })
            }
        }.flowOn(dispatcher)
    }
}