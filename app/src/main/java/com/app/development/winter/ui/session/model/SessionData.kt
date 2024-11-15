package com.app.development.winter.ui.session.model

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.development.winter.BR
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Keep
@Parcelize
data class SessionData(
    @SerializedName("_id") val sessionId: String? = null,
    @SerializedName("focusedTimeInSec") var focusedTimeInSec: Long = 0L,
    @SerializedName("currentLevel") var currentLevel: String = "1",
    @SerializedName("status") val status: SessionState? = null,
    @SerializedName("userId") val userId: String,
    @SerializedName("currentEarningsData") val currentEarningsData: CurrentEarningsData,
    @SerializedName("percentageLeftForNextLevel") var percentageLeftForNextLevel: Double = 0.0,
) : Parcelable, BaseObservable() {
    fun getLeftPercentage(): String {
        return "$percentageLeftForNextLevel%"
    }

    var totalSpentSessionTime: Long
        @Bindable get() = focusedTimeInSec
        set(value) {
            focusedTimeInSec = value
            notifyPropertyChanged(BR.totalSpentSessionTime)
        }

    @Keep
    @Parcelize
    data class CurrentEarningsData(
        @SerializedName("earningInCredits") val earningInCredits: Int,
        @SerializedName("earningInGivvyCoins") val earningInGivvyCoins: String? = null,
        @SerializedName("earningInUSD") var _earningInUSD: String? = null,
        @SerializedName("earnedCharityPoints") var charityPoint: String? = null,
        @SerializedName("earningInUserCurrency") val earningInUserCurrency: String,
        @SerializedName("earningPercentage") var _earningPercentage: String? = null,
        @SerializedName("GCminingSpeedPerHour") var _gCminingSpeedPerMin: String? = null,
        @SerializedName("shouldShowBigReward") val shouldShowBigReward: Boolean,
        @SerializedName("userCredits") var userCredits: Double = 0.0,
        @SerializedName("userCreditsInUSD") var userCreditsInUSD: Float = 0f,
        @SerializedName("percentageLeftToCompleteGrowth") var earningPercentForACoin: Int = 0,
    ) : Parcelable, BaseObservable() {
        private var earningPercentage: String?
            @Bindable get() = _earningPercentage
            set(value) {
                _earningPercentage = value
                notifyPropertyChanged(BR.earningPercentage)
            }
        var gCminingSpeedPerMin: String?
            @Bindable get() = _gCminingSpeedPerMin
            set(value) {
                _gCminingSpeedPerMin = value
                notifyPropertyChanged(BR.gCminingSpeedPerMin)
            }

        fun getPercentage(): String {
            return "$earningPercentage%"
        }

        fun getLeftGrowthPercentage(): String {
            return "$earningPercentForACoin%"
        }

        @SuppressLint("DefaultLocale")
        fun getGcmSpeed(): String {
            return "${String.format("%.2f", gCminingSpeedPerMin?.toFloat())} GC/H"
        }

        fun getBalanceWithCurrency(): String {
            var amount = 0.0f
            _earningInUSD?.let { value ->
                if (value.isNotEmpty() && value != "null") {
                    amount = value.toFloat()
                }
            }
            return String.format(Locale.ENGLISH, "$%.4f", amount)
        }
    }
}

enum class SessionState(val value: String) {
    @SerializedName("")
    NONE(""),

    @SerializedName("in_progress")
    IN_PROGRESS("in_progress"),

    @SerializedName("completed")
    COMPLETED("completed")
}
