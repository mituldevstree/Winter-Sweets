package com.app.development.winter.shared.model

import android.os.Parcelable
import android.text.Spanned
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.development.winter.ui.user.model.Intro
import com.app.development.winter.utility.Util
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UserConfig(
    @SerializedName("serverTimestamp") var serverTimestamp: Long = 0,
    @SerializedName("serverTimezone") var serverTimezone: Long = 0,
    @SerializedName("easiestTutorialIndex") var easiestTutorialIndex: Long = 5L,
    @SerializedName("canShowRatingDialog") var canShowRatingDialog: Boolean = false,
    @SerializedName("coinsForCentExchangeRate") var coinsForCentExchangeRate: Long? = 0,
    @SerializedName("welcomeTextTitle") var welcomeTextTitle: String = "",
    @SerializedName("welcomeTextDescription") var welcomeTextDescription: String = "",
    @SerializedName("needToShowEasiestWinBarTitle") var needToShowEasiestWinBarTitle: String = "",
    @SerializedName("needToShowEasiestWinBarDesc") var needToShowEasiestWinBarDesc: String = "",
    @SerializedName("firstInterstitialIndex") var firstInterstitialIndex: Int = 0,
    @SerializedName("nextInterstitialIndex") var nextInterstitialIndex: Int = 0,
    @SerializedName("canWatchVideoForPresent") var canWatchVideoForPresent: Boolean = false,
    @SerializedName("needToShowEasiestWinBar") var needToShowEasiestWinBar: Boolean = true,
    @SerializedName("totalVideosSecureReward") var totalVideoForSecureReward: Int = 0,
    @SerializedName("coinsSecurrewardPerAd") var rewardAmountPerAd: Long = 0,
    @SerializedName("minWithdrawAmount") var minWithdrawAmount: Double = 0.0,
    @SerializedName("earnUpToInterstitial") private var _earnUpToInterstitial: String = "",
    @SerializedName("earnUpToRewardedVideo") private var _earnUpToRewardVideo: String = "",
    @SerializedName("numberOfAdsForReward") var noOfVideoForSecureReward: Int = 0,
    @SerializedName("offersMaxWin") var maxEarnedByOffer: String = "",
    @SerializedName("dailySecureReward") var dailySecureRewardAmount: String = "",
    @SerializedName("facebookPageLink") var faceBookLink: String? = null,
    @SerializedName("instagramPageLink") var instagramLink: String? = null,
    @SerializedName("givvyEmailId") var givvyEmailId: String? = null,
    @SerializedName("termsAndConditionUrl") var termsAndConditionUrl: String? = null,
    @SerializedName("privacyPolicyUrl") var privacyPolicyUrl: String? = null,
    @SerializedName("countries") var countries: ArrayList<String>? = null,
    @SerializedName("tutorial") var tutorial: ArrayList<Intro>? = null,
    @SerializedName("multiplePlayerPeopleInfo") var multiplePlayerPeopleInfo: String = "",
    @SerializedName("contactFormText") val contactUsInfo: String? = null,
    @SerializedName("downloadsNeededForCyclicAds") var downloadsNeededForCyclicAds: Int = 7,
    @SerializedName("getCycleAdsRewardForceTime") var getCycleAdsRewardForceTime: Long = 60000,
    @SerializedName("coinsForHint") var coinsForHint: Long = 0,
    @SerializedName("coinsForHammerHint") var coinsForHammerHint: Long = 0,
    @SerializedName("coinsForAdditionalTime") var coinsForAdditionalTime: Long = 0,
    var downloadBonusForMiningSession: Int = 0,
    @SerializedName("specialRewardTriggerIndex") val specialRewardTriggerIndex: Int = 3,
    @SerializedName("gcid") var googleClientId: String? = ""
) : BaseObservable(), Parcelable {
    fun getWelcomeMessage(): Spanned {
        var htmlAsString: String = welcomeTextDescription
        if (htmlAsString.isEmpty()) {
            htmlAsString =
                "The best and fastest money earning legendary in the store with the lowest cash out."
        }
        return Util.fromHTML(htmlAsString)
    }

    fun getWelcomeTitle(): Spanned {
        var htmlAsString: String = welcomeTextTitle
        if (htmlAsString.isEmpty()) {
            htmlAsString = "Make Money"
        }
        return Util.fromHTML(htmlAsString)
    }


    var earnUpToInterstitial: String
        @Bindable get() = if (_earnUpToInterstitial.contains("c")) _earnUpToInterstitial.replace(
            "c", ""
        ) else _earnUpToInterstitial
        set(value) {
            _earnUpToInterstitial = value
        }

    var earnUpToRewardVideo: String
        @Bindable get() = if (_earnUpToRewardVideo.contains("c")) _earnUpToRewardVideo.replace(
            "c", ""
        ) else _earnUpToRewardVideo
        set(value) {
            _earnUpToRewardVideo = value
        }

    fun getFacebookPageLink(): String {
        return faceBookLink ?: ""
    }

    fun getInstrgramPageLink(): String {
        return instagramLink ?: ""
    }

    fun getGivvyEmail(): String {
        return givvyEmailId ?: "givvyproject@gmail.com"
    }

    fun getMinWithdrawWithCurrency(): String {
        return "".plus(String.format("%.2f", minWithdrawAmount)).plus(" USD")
    }

    fun getExchangeRate(): String {
        return (coinsForCentExchangeRate.toString()).plus(" = 0.01 USD")
    }

    /*    fun precacheTutorialImages(context: Context) {
            if (tutorial.isNullOrEmpty().not()) {
                tutorial?.forEach { intro ->
                    if (intro.image.isEmpty().not()) {
                        Glide.with(Controller.instance.applicationContext).load(intro.image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL).preload(1024, 1024)
                    }
                }
            }
        }*/


}