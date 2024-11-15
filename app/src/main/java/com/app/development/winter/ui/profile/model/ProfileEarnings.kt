package com.app.development.winter.ui.profile.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ProfileEarnings(
    @SerializedName("number") val scale: String = "0",
    @SerializedName("earningInCredits") val earningInCredits: Float = 0f
): Parcelable
