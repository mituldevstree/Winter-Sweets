package com.app.development.winter.shared.network.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
open class BaseModel(
    @SerializedName("statusCode") var code: Int = 0,
    @SerializedName("statusDesc") var errorType: String = "",
    @SerializedName("statusText") var message: String = ""
) {
    @SerializedName("status")
    val success: Boolean = false
    val token: String = ""
}