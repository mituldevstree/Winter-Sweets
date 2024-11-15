package com.app.development.winter.shared.base.event

import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.ui.user.model.CanWatchVideoResponse
import com.app.development.winter.ui.user.model.EarnCoins
import com.monetizationlib.data.ads.AdType

sealed class MonetizationEvents : ViewIntent {

    data class RequestAdReward(val adType: AdType) : MonetizationEvents()

    data class OnAdRewardReceived(val adType: AdType, val reward: EarnCoins?) : MonetizationEvents()
    data class OnAdRewardApiFailure(val message: String) : MonetizationEvents()

    data class RequestAdType(val adType: AdType) : MonetizationEvents()
    data class OnAdTypeAvailable(
        val adType: AdType, val canWatchVideoResponse: CanWatchVideoResponse?
    ) : MonetizationEvents()

    data class OnAdTypeFailed(val message: String) : MonetizationEvents()

}