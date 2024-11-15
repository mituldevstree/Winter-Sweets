package com.app.development.winter.shared.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SessionInfo(
    @SerializedName("sessionId") var sessionId: String? = null,
    @SerializedName("user") var user: User? = null,
    @SerializedName("config") var userConfig: UserConfig? = null,
//    @SerializedName("subscription") var activeMemberShip: MemberShip? = null
) : BaseObservable(), Parcelable {

}