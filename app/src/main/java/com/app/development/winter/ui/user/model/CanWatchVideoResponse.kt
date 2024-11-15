package com.app.development.winter.ui.user.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CanWatchVideoResponse(
    @SerializedName("isAbleToWatch") val isAbleToWatch: Boolean,
    @SerializedName("isInterstitial") var isInterstitial: Boolean = true,
    @SerializedName("timerForVideo1") var timerForVideo: Long = 30 * 1000,
)