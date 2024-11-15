package com.app.development.winter.ui.profile.viewmodel

import androidx.lifecycle.viewModelScope
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.profile.domain.ProfileRepository
import com.app.development.winter.ui.profile.event.ProfileEvent
import com.app.development.winter.ui.profile.state.ProfileEarningState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ProfileViewModel :
    AdvanceBaseViewModel<ProfileEvent, ProfileEarningState>(ProfileEarningState.defaultValue),
    CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO


    enum class LoadingType {
        EMPTY_SATE,
        LOADING
    }

    fun getProfileState() = _state

    override fun reduceState(
        currentState: ProfileEarningState,
        event: ProfileEvent,
    ): ProfileEarningState = when (event) {


        is ProfileEvent.RequestGetEarnings -> {
            getProfileEarnings(event.type)
            when (event.type) {
                ProfileEarningState.Companion.EarningFilterType.DAILY -> {
                    if (currentState.daily.isNullOrEmpty().not()) {
                        currentState.copy(
                            loadingState = Pair(
                                ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.RELOAD
                            ), daily = currentState.daily, selectedType = event.type
                        )
                    } else {
                        currentState.copy(
                            loadingState = Pair(
                                ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.PROCESSING
                            )
                        )
                    }
                }

                ProfileEarningState.Companion.EarningFilterType.MONTHLY -> {
                    if (currentState.monthly.isNullOrEmpty().not()) {
                        currentState.copy(
                            loadingState = Pair(
                                ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.RELOAD
                            ), monthly = currentState.monthly, selectedType = event.type
                        )
                    } else {
                        currentState.copy(
                            loadingState = Pair(
                                ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.PROCESSING
                            )
                        )
                    }
                }

                ProfileEarningState.Companion.EarningFilterType.WEEKLY -> {
                    if (currentState.weekly.isNullOrEmpty().not()) {
                        currentState.copy(
                            loadingState = Pair(
                                ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.RELOAD
                            ), weekly = currentState.weekly, selectedType = event.type
                        )
                    } else {
                        currentState.copy(
                            loadingState = Pair(
                                ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.PROCESSING
                            )
                        )
                    }
                }
            }
        }

        is ProfileEvent.OnFailure -> {
            currentState.copy(
                errorMessage = event.errorMessage,
                loadingState = Pair(
                    ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.ERROR
                )
            )
        }

        is ProfileEvent.OnEarningStatsAvailable -> {
            when (event.type) {
                ProfileEarningState.Companion.EarningFilterType.DAILY -> {
                    currentState.copy(
                        daily = event.data, selectedType = event.type, loadingState = Pair(
                            ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.COMPLETED
                        )
                    )
                }

                ProfileEarningState.Companion.EarningFilterType.WEEKLY -> {
                    currentState.copy(
                        weekly = event.data, selectedType = event.type, loadingState = Pair(
                            ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.COMPLETED
                        )
                    )
                }

                else -> {
                    currentState.copy(
                        monthly = event.data, selectedType = event.type, loadingState = Pair(
                            ApiEndpoints.GET_PROFILE_EARNINGS, LoadingState.COMPLETED
                        )
                    )
                }

            }
        }

    }

    private fun getProfileEarnings(filterType: ProfileEarningState.Companion.EarningFilterType) {
        viewModelScope.launch {
            delay(1000)
            val params = HashMap<String, Any>()
            if (LocalDataHelper.getUserDetail()?.id != null) {
                params["userId"] = LocalDataHelper.getUserDetail()?.id.toString()
            }
            params["filterType"] = filterType.typeName
            viewModelScope.launch {
                ProfileRepository.getProfileEarnings(params, Dispatchers.IO)
                    .catch { exception ->
                        exception.message?.let {
                            _state.handleEvent(
                                ProfileEvent.OnFailure(
                                    it
                                )
                            )
                        }
                    }.collect { result ->
                        if (result != null && result.code == ApiEndpoints.RESPONSE_OK) {
                            if (result.data.isNullOrEmpty().not()) {
                                _state.handleEvent(
                                    ProfileEvent.OnEarningStatsAvailable(
                                        data = result.data, type = filterType
                                    )
                                )
                            } else {
                                _state.handleEvent(ProfileEvent.OnFailure("No data available"))
                            }
                        } else {
                            _state.handleEvent(ProfileEvent.OnFailure(result?.message))
                        }
                    }
            }

        }
    }
}

