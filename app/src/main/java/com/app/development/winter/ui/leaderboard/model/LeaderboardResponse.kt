package com.app.development.winter.ui.leaderboard.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LeaderboardResponse(
    @SerializedName("pageTitle")
    var pageTile: String? = null,
    var pageSubTitle: String? = null,
    @SerializedName("myUser")
    var myUser: LeaderBoardUser? = null,
)
