package com.app.development.winter.ui.leaderboard.state

import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.ui.leaderboard.model.LeaderBoardUser

data class LeaderboardUiState(
    val loadingState: Pair<String, AdvanceBaseViewModel.LoadingState> = Pair(
        "", AdvanceBaseViewModel.LoadingState.PROCESSING
    ),
    val users: MutableList<LeaderBoardUser>? = null,
    val top3Users: MutableList<LeaderBoardUser>? = null,
    val myUserRank: LeaderBoardUser? = null,
    val pageTitle: String? = null,
    val pageSubTitle: String? = null,
    val errorMessage: String? = null,

    ) : ViewState {
    companion object {
        val defaultValue = LeaderboardUiState(
            loadingState = Pair("", AdvanceBaseViewModel.LoadingState.PROCESSING),
            users = null,
            top3Users = null,
            myUserRank = null,
            pageSubTitle = null,
            pageTitle = null,
            errorMessage = null
        )
    }
}