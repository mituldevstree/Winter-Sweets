package com.app.development.winter.ui.leaderboard.event

import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.ui.leaderboard.model.LeaderBoardUser

sealed class LeaderboardEvent : ViewIntent {

    // Get Program info Events
    data object RequestLeaderboardUsers : LeaderboardEvent()

    data class OnLeaderboardNewUserListAvailable(
        val top3Users: MutableList<LeaderBoardUser>?,
        val users: MutableList<LeaderBoardUser>?,
    ) : LeaderboardEvent()

    data class OnApiFailure(val message: String?) : LeaderboardEvent()
}