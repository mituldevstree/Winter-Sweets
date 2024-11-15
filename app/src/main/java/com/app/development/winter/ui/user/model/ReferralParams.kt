package com.app.development.winter.ui.user.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ReferralParams(
    @SerializedName("_id") var id: String? = "",
    @SerializedName("referralId") var referralId: String? = "",
    @SerializedName("appName") var appName: String? = "",
    @SerializedName("params") var params: Params? = null,
    @SerializedName("ipAddress") var ipAddress: String? = "",
    @SerializedName("expireDate") var expireDate: String? = ""
)

@Keep
data class Params(
    @SerializedName("userId") var userId: String? = "",
    @SerializedName("pairingId") var pairingId: String? = "",
    @SerializedName("referralId") var referralId: String? = "",
    @SerializedName("appName") var appName: String? = "",
)
