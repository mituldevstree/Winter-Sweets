package com.app.development.winter.ui.session.viewmodel

import androidx.lifecycle.viewModelScope
import com.app.development.winter.BuildConfig
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.ApiEndpoints.RESPONSE_OK
import com.app.development.winter.shared.network.domain.UserRepository
import com.app.development.winter.ui.session.domain.SessionRepository
import com.app.development.winter.ui.session.event.SessionEvent
import com.app.development.winter.ui.session.state.SessionUiState
import com.app.development.winter.ui.session.state.SessionUiState.Companion.defaultValue
import com.app.development.winter.utility.ViewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.collections.set

class SessionConfigViewModel : AdvanceBaseViewModel<SessionEvent, SessionUiState>(defaultValue) {
    fun getSessionUiState() = _state
    override fun reduceState(
        currentState: SessionUiState,
        event: SessionEvent,
    ): SessionUiState = when (event) {
        is SessionEvent.RequestOngoingSessionInfo -> {
            getActiveSession(event.wantToCreateSession)
            currentState.copy(
                loadingState = Pair(
                    LoadingType.GET_ONGOING_SESSION, LoadingState.PROCESSING
                )
            )
        }

        is SessionEvent.ResponseOngoingSessionInfo -> {
            if (event.sessionData == null || event.sessionData.sessionId.isNullOrEmpty() && event.wantToCreateSession) {
                createNewSession()
                currentState.copy(
                    loadingState = Pair(
                        LoadingType.START_NEW_SESSION, LoadingState.PROCESSING
                    )
                )
            } else {
                currentState.copy(
                    loadingState = Pair(LoadingType.GET_ONGOING_SESSION, LoadingState.COMPLETED),
                    activeSession = event.sessionData
                )
            }
        }

        is SessionEvent.ResponseNewSessionInfo -> {
            currentState.copy(
                loadingState = Pair(LoadingType.START_NEW_SESSION, LoadingState.COMPLETED),
                activeSession = event.sessionData
            )
        }


        is SessionEvent.RequestSyncFocusTime -> {
            saveFocusedTime(event.focusTimeInSec)
            currentState.copy(
                loadingState = Pair(
                    LoadingType.SYNC_FOCUS_TIME, LoadingState.PROCESSING
                )
            )

        }

        is SessionEvent.ResponseSyncFocusTime -> {
            currentState.copy(
                syncSession = event.sessionData, loadingState = Pair(
                    LoadingType.SYNC_FOCUS_TIME, LoadingState.COMPLETED
                )
            )
        }

        is SessionEvent.RequestEndSession -> {
            endSession(event.focusTimeInSec)
            currentState.copy(
                loadingState = Pair(
                    LoadingType.SESSION_END, LoadingState.PROCESSING
                )
            )

        }

        is SessionEvent.ResponseEndSession -> {
            currentState.copy(
                activeSession = event.sessionData, loadingState = Pair(
                    LoadingType.SESSION_END, LoadingState.COMPLETED
                )
            )
        }

        is SessionEvent.RequestRewardForAd -> {
            requestAdReward()
            currentState.copy(
                loadingState = Pair(
                    LoadingType.GET_CYCLE_AD_REWARD, LoadingState.PROCESSING
                )
            )
        }

        is SessionEvent.ResponseRewardForAd -> {
            currentState.copy(
                loadingState = Pair(
                    LoadingType.GET_CYCLE_AD_REWARD, LoadingState.COMPLETED
                ), cycleAdsEarning = event.reward
            )
        }

        is SessionEvent.RequestUserStatistics -> {
            getUserStatistics(event.focusTimeInSec)
            currentState.copy(
                loadingState = Pair(
                    LoadingType.USER_STATE_DATA, LoadingState.PROCESSING
                )
            )
        }

        is SessionEvent.ResponseUserStatistics -> {
            currentState.copy(
                loadingState = Pair(
                    LoadingType.USER_STATE_DATA, LoadingState.COMPLETED
                ), userXpStateData = event.userState
            )
        }

        is SessionEvent.ErrorSessionInfo -> {
            currentState.copy(
                loadingState = Pair(
                    event.loadingType, LoadingState.ERROR
                ), errorMessage = event.errorMessage
            )
        }
    }

    private fun requestAdReward() {
        val params: HashMap<String, Any> = HashMap()
        params["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        addNewJob(ApiEndpoints.GET_CYCLIC_AD_REWARD, viewModelScope.launch {
            UserRepository.getCyclicAdsReward(params, Dispatchers.IO).catch { exception ->
                exception.message?.let { message ->
                    getSessionUiState().handleEvent(
                        SessionEvent.ErrorSessionInfo(LoadingType.GET_CYCLE_AD_REWARD, message)
                    )
                }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    if (result.data != null) {
                        val user = LocalDataHelper.getUserDetail()
                        result.data?.credits?.let {
                            user?.coinBalance = it.toDouble()
                        }
                        result.data?.userBalanceDouble?.let {
                            user?.userBalanceDouble = it
                        }
                        result.data?.userBalance?.let {
                            user?.userBalance = it
                        }
                        LocalDataHelper.setUserDetail(user)
                        getSessionUiState().handleEvent(SessionEvent.ResponseRewardForAd(reward = result.data))
                    }
                }
            }
        })
    }

    /**
     * Start new game session and for new deck generation.
     */
    private fun createNewSession() {
        val param: MutableMap<String, Any> = HashMap()
        param["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        viewModelScope.launch {
            SessionRepository.createNewSession(param).catch { exception ->
                exception.message?.let { message ->
                    getSessionUiState().handleEvent(
                        SessionEvent.ErrorSessionInfo(LoadingType.START_NEW_SESSION, message)
                    )
                }
            }.collect { result ->
                if (result?.data != null && result.code == RESPONSE_OK) {
                    getSessionUiState().handleEvent(
                        SessionEvent.ResponseNewSessionInfo(result.data)
                    )
                } else {
                    result?.message?.let { message ->
                        getSessionUiState().handleEvent(
                            SessionEvent.ErrorSessionInfo(
                                LoadingType.START_NEW_SESSION, message
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Get user state for non earning mode.
     */
    private fun getUserStatistics(focusTimeInSec: String?) {
        val param: MutableMap<String, Any> = HashMap()
        param["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        param["focusedTimeInSec"] = focusTimeInSec ?: ""
        viewModelScope.launch {
            SessionRepository.getUserStatistics(param).catch { exception ->
                exception.message?.let { message ->
                    getSessionUiState().handleEvent(
                        SessionEvent.ErrorSessionInfo(LoadingType.USER_STATE_DATA, message)
                    )
                }
            }.collect { result ->
                if (result?.data != null && result.code == RESPONSE_OK) {
                    getSessionUiState().handleEvent(
                        SessionEvent.ResponseUserStatistics(result.data!!)
                    )
                } else {
                    result?.message?.let { message ->
                        getSessionUiState().handleEvent(
                            SessionEvent.ErrorSessionInfo(
                                LoadingType.USER_STATE_DATA, message
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Check for any active game session and restore.
     */
    private fun getActiveSession(wantToCreateSession: Boolean) {
        val param: MutableMap<String, Any> = HashMap()
        param["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        viewModelScope.launch {
            SessionRepository.getActiveSession(param).catch { exception ->
                exception.message?.let { message ->
                    getSessionUiState().handleEvent(
                        SessionEvent.ErrorSessionInfo(LoadingType.GET_ONGOING_SESSION, message)
                    )
                }
            }.collect { result ->
                if (result?.code == RESPONSE_OK && result.data != null) {
                    getSessionUiState().handleEvent(
                        SessionEvent.ResponseOngoingSessionInfo(
                            result.data, wantToCreateSession
                        )
                    )

                } else {
                    getSessionUiState().handleEvent(
                        SessionEvent.ResponseOngoingSessionInfo(
                            sessionData = null,
                            wantToCreateSession
                        )
                    )
                }
            }
        }
    }

    /**
     * Check for any active game session and restore.
     */
    private fun endSession(focusTimeInSec: String?) {
        val param: MutableMap<String, Any> = HashMap()
        param["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        param["focusedTimeInSec"] = focusTimeInSec ?: ""
        viewModelScope.launch {
            SessionRepository.endActiveSession(param).catch { exception ->
                exception.message?.let { message ->
                    getSessionUiState().handleEvent(
                        SessionEvent.ErrorSessionInfo(LoadingType.SESSION_END, message)
                    )
                }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    if (result.data != null) {
                        getSessionUiState().handleEvent(SessionEvent.ResponseEndSession(result.data))
                    } else {
                        result.message.let { message ->
                            getSessionUiState().handleEvent(
                                SessionEvent.ErrorSessionInfo(
                                    LoadingType.SESSION_END, message
                                )
                            )
                        }
                    }
                } else {
                    result?.message?.let { message ->
                        getSessionUiState().handleEvent(
                            SessionEvent.ErrorSessionInfo(
                                LoadingType.SESSION_END, message
                            )
                        )
                    }
                }
            }
        }
    }


    /**
     * Save game board state.
     */
    private fun saveFocusedTime(focusedTimeInSec: String?) {
        val params: MutableMap<String, Any> = HashMap()
        params["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        params["focusedTimeInSec"] = focusedTimeInSec ?: ""
        viewModelScope.launch {
            SessionRepository.saveFocusedTime(params).catch { exception ->
                exception.message?.let { message ->
                    getSessionUiState().handleEvent(
                        SessionEvent.ErrorSessionInfo(LoadingType.SYNC_FOCUS_TIME, message)
                    )
                }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    if (result.data != null) {
                        LocalDataHelper.updateUserXPData(result.data?.currentEarningsData)
                        getSessionUiState().handleEvent(SessionEvent.ResponseSyncFocusTime(result.data))
                    } else {
                        result.message.let { message ->
                            getSessionUiState().handleEvent(
                                SessionEvent.ErrorSessionInfo(
                                    LoadingType.SYNC_FOCUS_TIME, message
                                )
                            )
                        }
                    }
                } else {
                    result?.message?.let { message ->
                        getSessionUiState().handleEvent(
                            SessionEvent.ErrorSessionInfo(
                                LoadingType.SYNC_FOCUS_TIME, message
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * user click on ads
     */
    fun userClickOnAd(onSuccessCallback: (() -> Unit)?, onFailureCallback: (() -> Unit)?) {
        val params: HashMap<String, Any> = HashMap()
        params["packageName"] = BuildConfig.APPLICATION_ID
        params["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        viewModelScope.launch {
            UserRepository.onAdClick(params, Dispatchers.IO).catch { exception ->
                ViewUtil.printLog("Error", exception.message.toString())
                onFailureCallback?.invoke()
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    if (result.data != null) {
                        MainScope().launch {
                            onSuccessCallback?.invoke()
                        }
                    } else {
                        onFailureCallback?.invoke()
                    }
                } else {
                    onFailureCallback?.invoke()
                }
            }
        }
    }
}