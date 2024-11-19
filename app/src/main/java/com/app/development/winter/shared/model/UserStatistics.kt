package com.app.development.winter.shared.model

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
data class UserStatistics(
    @SerializedName("userBalanceDouble") var userBalanceDouble: Float = 0f,
    @SerializedName("GCminingSpeedPerHour") var avgEarningRate: String = "",
    @SerializedName("totalTimeSpentInSec") var totalTimeSpentInSec: Long = System.currentTimeMillis(),
    @SerializedName("activeAppUsers") var activeAppUsers: Int = 0,
    @SerializedName("totalReferrals") var totalReferrals: Int = 0,
    @SerializedName("credits") var credits: Double = 0.0,
) : BaseObservable(), Parcelable {
    fun getBalanceWithCurrency(): String {
        return "$".plus("").plus(String.format(Locale.ENGLISH, "%.4f", userBalanceDouble))
    }

    var totalSpentSessionTime: Long
        @Bindable get() = totalTimeSpentInSec
        set(value) {
            totalTimeSpentInSec = value
            notifyPropertyChanged(BR.totalSpentSessionTime)
        }
}
