package com.app.development.winter.shared.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.development.winter.BR
import com.app.development.winter.localcache.StaticData
import com.app.development.winter.shared.extension.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Keep
@Parcelize
data class User(
    @SerializedName(value = "id", alternate = ["_id", "friendId"]) var id: String? = null,
    @SerializedName(
        value = "currentCoins", alternate = ["credits", "userCredits"]
    ) private var _coinBalance: Double = 0.0,
    @SerializedName(
        value = "photo", alternate = ["playerProfile", "userPhoto"]
    ) private var _image: String? = null,
    @SerializedName("showWithdrawShareReminder") var showWithdrawShareReminder: Boolean = false,
    @SerializedName("referralCode") var referralCode: String? = null,
    @SerializedName("externalAuthToken") var externalAuthToken: String? = null,
    @SerializedName("needToMakeSocialLogin") var needToMakeSocialLogin: Boolean = true,
    @SerializedName("externalId") var externalUserID: String? = null,
    @SerializedName("username") private var _username: String? = null,
    @SerializedName("email") private var _email: String? = null,
    @SerializedName("country", alternate = ["userCountry"]) private var _country: String? = null,
    @SerializedName("loginType") var _loginType: String? = null,
    @SerializedName("referralLink") var referralLink: String? = null,
    @SerializedName("currency") var currency: String? = "$",
    @SerializedName("userBalance") private var _userBalance: Float = 0f,
    @SerializedName("userBalanceDouble") private var _userBalanceDouble: Float = 0f,
    @SerializedName("referralTextInfo") val referralTextInfo: String? = "",
    @SerializedName("earnCredits") private var _coinsWon: Double = 0.0,
    @SerializedName("percentOfMinCashOut") var cashOutPercent: Float = 0f,
    @SerializedName("watchVideoForPresent") private var _bonusRewardProgress: Int = 0,
    var externalSessionCode: String? = null,

    @SerializedName("shouldShowCycleAds") var shouldShowCycleAds: Boolean = true,
    @SerializedName("cycleAdsForRentApp") var cycleAdsForRentApp: Boolean = false,
    @SerializedName("maxTryForAds") var maxTryForAds: Int = 20,
    @SerializedName("appWillReopenAfterTimeDecrement") var appWillReopenAfterTimeDecrement: Int = 31000,
    @SerializedName("clickPercentage") var clickPercentage: Int? = 0,
    @SerializedName("maxAdLoadAttempts") var maxAdLoadAttempts: Int? = 0,
    @SerializedName("firstAdsLoadsWaitTime") var firstAdsLoadsWaitTime: Long? = 30000,
    @SerializedName("videoAdsWaitTimeForClickAndOpen") var videoAdsWaitTimeForClickAndOpen: Long? = 30000,
    @SerializedName("waitTimeForTheAdsClick") var waitTimeForTheAdsClick: Long = 2000,
    @SerializedName("waitTimeForTheReopenApp") var waitTimeForTheReopenApp: Long = 10000,
    @SerializedName("currentProgressForCylicAds") var currentAppInstalled: Int = 0,
    @SerializedName("maxBonusEarningPercentagePerPool") var maxBonusEarningPercentagePerPool: Int = 30,
    @SerializedName("shouldShowBigReward") var shouldShowBigReward: Boolean = false,
    @SerializedName("canUseTransparentOverlay") var canUseTransparentOverlay: Boolean = false,
    @SerializedName("isOnProduction") var isOnProduction: Boolean = false,
    @Transient var dailyEarningLimitReached: Boolean = false,
) : BaseObservable(), Parcelable {

    fun getCashOutPercentInFloat(): Int {
        return cashOutPercent.toInt()
    }

    var coinBalance: Double
        @Bindable get() = _coinBalance
        set(value) {
            _coinBalance = value
            notifyPropertyChanged(BR.coinBalance)
        }

    var userBalance: Float
        @Bindable get() = _userBalance
        set(value) {
            _userBalance = value
            notifyPropertyChanged(BR.userBalance)
        }
    var userBalanceDouble: Float
        @Bindable get() = _userBalanceDouble
        set(value) {
            _userBalanceDouble = value
            notifyPropertyChanged(BR.userBalanceDouble)
        }

    var earnedCoins: Double
        @Bindable get() = _coinsWon
        set(value) {
            _coinsWon = value
            notifyPropertyChanged(BR.earnedCoins)
        }

    var loginType: String?
        @Bindable get() = _loginType
        set(value) {
            _loginType = value
            notifyPropertyChanged(BR.loginType)
        }

    var photo: String?
        @Bindable get() = _image
        set(value) {
            _image = value
            notifyPropertyChanged(BR.photo)
        }
    var name: String?
        @Bindable get() = _username
        set(value) {
            _username = value
            notifyPropertyChanged(BR.name)
        }

    var email: String?
        @Bindable get() = _email
        set(value) {
            _email = value
            notifyPropertyChanged(BR.email)
        }
    var country: String?
        @Bindable get() = _country
        set(value) {
            _country = value
            notifyPropertyChanged(BR.country)
        }

    var bonusRewardProgress: Int
        @Bindable get() = _bonusRewardProgress
        set(value) {
            _bonusRewardProgress = value
            notifyPropertyChanged(BR.bonusRewardProgress)
        }

    fun getBalanceWithCurrency(): String {
        return "$".plus("").plus(String.format(Locale.ENGLISH, "%.4f", userBalanceDouble))
    }

    fun getUserCoinBalance(): String {
        return String.format(Locale.ENGLISH, "%.2f", coinBalance)
    }

    fun getUserAvtar(): Int? {
        return StaticData.getAvatarImage(photo)?.icon ?: StaticData.getAvatarList()[0].icon
    }

    fun getDisplayName(): String? {
        if (_username.isNotNullOrEmpty()) {
            return _username
        }
        return name
    }

    companion object {
        const val LOGIN_GOOGLE = "google"
        const val LOGIN_FACEBOOK = "facebook"
    }
}