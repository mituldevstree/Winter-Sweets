package com.app.development.winter.ui.user.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CycleAdsEarnings(
    val expectedWinsUSD: Double = 0.0,
    val expectedWinsInCoins: Double = 0.0,
    @SerializedName("earnCredits")
    val earnedCredits: Double = 0.0,
    @SerializedName("credits")
    val credits: Long = 0L,
    @SerializedName("userBalance")
    val userBalance: Float = 0f,
    @SerializedName("userBalanceDouble")
    val userBalanceDouble: Float = 0f,
    @SerializedName("moneysUntilNow")
    val earnedCreditsUsd:String="",
    @SerializedName("moneysUntilNowInCredits")
    val earnedCreditsCoins:String="",
    @SerializedName("earnCreditsInUSD")
    val earnCreditsInUSD: Float = 0.00f,
    @SerializedName("totalCoinsEarnedFromGame")
    val earntCreditsFromGame:Double=0.0
)