package com.app.development.winter.ui.leaderboard.viewmodel

import androidx.lifecycle.viewModelScope
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.leaderboard.domain.LeaderboardRepository
import com.app.development.winter.ui.leaderboard.event.LeaderboardEvent
import com.app.development.winter.ui.leaderboard.model.LeaderBoardUser
import com.app.development.winter.ui.leaderboard.state.LeaderboardUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LeaderboardViewModel :
    AdvanceBaseViewModel<LeaderboardEvent, LeaderboardUiState>(LeaderboardUiState.defaultValue),
    CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO
    fun getLeaderboardState() = _state

    override fun reduceState(
        currentState: LeaderboardUiState,
        event: LeaderboardEvent,
    ): LeaderboardUiState = when (event) {
        is LeaderboardEvent.RequestLeaderboardUsers -> {
            getUserLeaderboard()

            if (currentState.top3Users.isNullOrEmpty()) {
                currentState.copy(
                    loadingState = Pair(
                        ApiEndpoints.GET_LEADERBOARD_USERS, LoadingState.PROCESSING
                    ),
                )
            } else {
                currentState.copy(
                    loadingState = Pair(
                        ApiEndpoints.GET_LEADERBOARD_USERS, LoadingState.RELOAD,
                    ), top3Users = currentState.top3Users, users = currentState.users
                )
            }

        }

        is LeaderboardEvent.OnLeaderboardNewUserListAvailable -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.GET_LEADERBOARD_USERS, LoadingState.COMPLETED,
                ), top3Users = event.top3Users, users = event.users
            )
        }

        is LeaderboardEvent.OnApiFailure -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.GET_LEADERBOARD_USERS, LoadingState.ERROR
                ), errorMessage = event.message
            )
        }
    }

    private fun getUserLeaderboard() {
        viewModelScope.launch {
            val params = HashMap<String, Any>()
            if (LocalDataHelper.getUserDetail()?.id != null) {
                params["userId"] = LocalDataHelper.getUserDetail()?.id.toString()
            }
            viewModelScope.launch {
                LeaderboardRepository.getLeaderboardUsers(params, Dispatchers.IO)
                    .catch { exception ->
                        exception.message?.let {
                            _state.handleEvent(LeaderboardEvent.OnApiFailure(it))
                        }
                    }.collect { result ->
                        if (result != null && result.code == ApiEndpoints.RESPONSE_OK) {
                            if (result.data != null) {
                                val topThreeUser: MutableList<LeaderBoardUser> = mutableListOf()
                                val users: MutableList<LeaderBoardUser> = mutableListOf()
                                result.data?.filter { leaderboardUser ->
                                    when (leaderboardUser.rank) {
                                        1, 2, 3 -> {
                                            topThreeUser.add(leaderboardUser)
                                            false
                                        }

                                        else -> {
                                            users.add(leaderboardUser)
                                            false
                                        }
                                    }
                                }
                                _state.handleEvent(
                                    LeaderboardEvent.OnLeaderboardNewUserListAvailable(
                                        top3Users = topThreeUser, users = users
                                    )
                                )
                            } else {
                                _state.handleEvent(LeaderboardEvent.OnApiFailure("No data available"))
                            }
                        } else {
                            _state.handleEvent(LeaderboardEvent.OnApiFailure(result?.message))
                        }
                    }
            }

        }
    }
}

