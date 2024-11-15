package com.app.development.winter.shared.base.viewmodel


import androidx.lifecycle.viewModelScope
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.event.MonetizationEvents
import com.app.development.winter.shared.base.state.MonetizationState
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.ApiEndpoints.RESPONSE_OK
import com.app.development.winter.shared.network.domain.UserRepository
import com.app.development.winter.utility.ViewUtil
import com.monetizationlib.data.ads.AdType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MonetizationViewModel : AdvanceBaseViewModel<MonetizationEvents, MonetizationState>(MonetizationState.defaultValue) {

    fun getUiState() = _state

    override fun reduceState(
        currentState: MonetizationState,
        event: MonetizationEvents,
    ): MonetizationState = when (event) {
        is MonetizationEvents.RequestAdReward -> {
            getAdReward(event.adType)
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.GET_AD_REWARD, LoadingState.PROCESSING
                )
            )
        }

        is MonetizationEvents.OnAdRewardReceived -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.GET_AD_REWARD, LoadingState.COMPLETED
                ), reward = event.reward
            )
        }

        is MonetizationEvents.OnAdRewardApiFailure -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.GET_AD_REWARD,
                    LoadingState.ERROR,

                    ), errorMessage = event.message

            )
        }

        is MonetizationEvents.RequestAdType -> {
            getAdTypeToWatch(event.adType)
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.CHECK_FOR_AD_TYPE, LoadingState.PROCESSING
                )
            )
        }

        is MonetizationEvents.OnAdTypeAvailable -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.CHECK_FOR_AD_TYPE, LoadingState.COMPLETED
                ), canWatchVideoResponse = event.canWatchVideoResponse
            )
        }

        is MonetizationEvents.OnAdTypeFailed -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.CHECK_FOR_AD_TYPE, LoadingState.ERROR
                ), errorMessage = event.message
            )
        }


    }

    /**
     * Get reward for watching ads based on type.
     */
    private fun getAdReward(adType: AdType) {
        val params: MutableMap<String, Any> = HashMap()
        params["userId"] = com.app.development.winter.localcache.LocalDataHelper.getUserDetail()?.id ?: ""
        params["type"] = if (adType == AdType.Interstitial) "interstitial" else "rewarded"
        viewModelScope.launch {
            UserRepository.watchDailyRewardVideo(params, Dispatchers.IO).catch { exception ->
                ViewUtil.printLog("Error", exception.message.toString())
                exception.message?.let {
                    getUiState().handleEvent(
                        MonetizationEvents.OnAdRewardApiFailure(
                            it
                        )
                    )
                }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    if (result.data != null) {
                        val credits = result.data?.credits
                        val userBalance = result.data?.userBalance
                        val userBalanceDouble = result.data?.userBalanceDouble
                        val user = com.app.development.winter.localcache.LocalDataHelper.getUserDetail()
                        if (credits != null) {
                            userBalance?.let { user?.userBalance = it.toFloat() }
                            userBalanceDouble?.let { user?.userBalanceDouble = it.toFloat() }
                            user?.coinBalance = credits
                            com.app.development.winter.localcache.LocalDataHelper.setUserDetail(user)
                        }
                        getUiState().handleEvent(
                            MonetizationEvents.OnAdRewardReceived(
                                adType, result.data
                            )
                        )
                    }
                } else {
                    result?.message?.let {
                        getUiState().handleEvent(
                            MonetizationEvents.OnAdRewardApiFailure(
                                it
                            )
                        )
                    }
                }
            }
        }
    }


    /**
     * Can watch video for reward
     */
    private fun getAdTypeToWatch(adType: AdType) {
        val params: MutableMap<String, Any> = HashMap()
        params["userId"] = com.app.development.winter.localcache.LocalDataHelper.getUserDetail()?.id ?: ""
        params["type"] = if (adType == AdType.Interstitial) "interstitial" else "rewarded"
        MainScope().launch {
            UserRepository.canWatchVideoForReward(params, Dispatchers.IO).catch { exception ->
                ViewUtil.printLog("Error", exception.message.toString())
                getUiState().handleEvent(MonetizationEvents.OnAdTypeFailed(exception.message.toString()))
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    if (result.data != null) {
                        getUiState().handleEvent(
                            MonetizationEvents.OnAdTypeAvailable(
                                adType, result.data
                            )
                        )
                    }
                } else {
                    result?.message?.let {
                        getUiState().handleEvent(
                            MonetizationEvents.OnAdTypeFailed(
                                it
                            )
                        )
                    }

                }
            }
        }

    }

}