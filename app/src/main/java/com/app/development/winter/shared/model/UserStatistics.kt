package com.app.development.winter.shared.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Keep
@Parcelize
data class UserStatistics(
    @SerializedName("userBalanceDouble") var userBalanceDouble: Float = 0f,
    @SerializedName("totalTimeSpentInSec") var totalTimeSpentInSec: Long = System.currentTimeMillis(),
    @SerializedName("credits") var creditsToReturn: String = "",
    @SerializedName("GCminingSpeedPerHour") var avgEarningRate: String = "",
) : BaseObservable(), Parcelable {
    fun getBalanceWithCurrency(): String {
        return "$".plus("").plus(String.format(Locale.ENGLISH, "%.4f", userBalanceDouble))
    }
}
