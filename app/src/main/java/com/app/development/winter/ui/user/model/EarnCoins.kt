package com.app.development.winter.ui.user.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class EarnCoins() : Parcelable {
    @SerializedName("earnCredits")
    var earnCredits: Double = 0.0

    @SerializedName("credits")
    var credits: Double = 0.0

    @SerializedName("percentOfMinCashOut")
    var percentOfMinCashOut: Double = 0.0

    @SerializedName("userBalance")
    var userBalance: Double = 0.0
    var userBalanceDouble: Double = 0.0

    @SerializedName("timestampForDailyGift")
    var timestampForDailyGift: Long = 0L


    constructor(parcel: Parcel) : this() {
        earnCredits = parcel.readDouble()
        credits = parcel.readDouble()
        percentOfMinCashOut = parcel.readDouble()
        userBalance = parcel.readDouble()
        timestampForDailyGift = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(earnCredits)
        parcel.writeDouble(credits)
        parcel.writeDouble(percentOfMinCashOut)
        parcel.writeDouble(userBalance)
        parcel.writeLong(timestampForDailyGift)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EarnCoins> {
        override fun createFromParcel(parcel: Parcel): EarnCoins {
            return EarnCoins(parcel)
        }

        override fun newArray(size: Int): Array<EarnCoins?> {
            return arrayOfNulls(size)
        }
    }

    enum class EarningType {
        @SerializedName("coins")
        COIN,

        @SerializedName("percent")
        PERCENT
    }
}