package com.app.development.winter.ui.settings.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Setting(
    var type: SettingType,
    @SerializedName("value")
    var value: String? = null,
    var title: String? = null,
    var desc: String? = null,
    var icon: Int? = null
) : Parcelable {
    @Keep
    enum class SettingType() {
        LANGUAGE,
        PROFILE,
        POLICY,
        TERMS,
        CONTACT_US,
        DELETE,
        TUTORIAL,
        GET_SPECIAL_REWARD,
        XP_OVERLAY,
        REFERRAL_CODE,
    }
}