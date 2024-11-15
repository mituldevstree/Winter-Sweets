package com.app.development.winter.ui.leaderboard.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.app.development.winter.localcache.StaticData
import com.app.development.winter.utility.ViewUtil
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LeaderBoardUser(
    @SerializedName("_id") var id: String? = null,
    @SerializedName("rank") var rank: Int = 0,
    @SerializedName("username") var username: String? = null,
    @SerializedName("photo") var photo: String? = null,
    @SerializedName("totalTimeSpentInSec") var totalTimeSpentInSec: Long = 0,
) : Parcelable {
    fun getName(): String? {
        return if (username.isNullOrEmpty().not()) {
            username
        } else {
            "Anonymous"
        }
    }

    fun getUserImage(): Int? {
        return StaticData.getAvatarImage(photo)?.icon ?: StaticData.getAvatarList()[0].icon
    }


    fun getFormatedTimeSpent(): String {
        return ViewUtil.convertToHHMMSS(totalTimeSpentInSec * 1000)
    }
}