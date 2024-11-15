package com.app.development.winter.ui.session.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.utility.DateTimeUtil
import kotlinx.parcelize.Parcelize
import java.text.DecimalFormat

@Keep
@Parcelize
data class CycleAdsSessionDetail(
    var mSessionData: SessionData? = null,
    var wasSessionStarted: Boolean = false,
    var wasSessionPause: Boolean = false,
    var sessionEarning: Long = 0L,
    var startSessionTime: Long = 0L
) : Parcelable {
    var wasSessionEnabled
        get() = wasSessionStarted
        set(value) {
            wasSessionStarted = value
        }

    fun getTotalSessionTime(): String {
        val diff = System.currentTimeMillis() - startSessionTime
        return DateTimeUtil.formatTime(diff)
    }

    fun getTotalSessionEarning(): String {
        return sessionEarning.toString()
    }

    fun getTotalSessionEarningUsd(): String {
        LocalDataHelper.getUserConfig()?.coinsForCentExchangeRate?.let { rate ->
            if (sessionEarning > 0) {
                val earning = ((sessionEarning.toDouble() / rate) / 100)
                return "$" + DecimalFormat("##.######").format(earning)
            }
        }
        return "$0.00"
    }
}


