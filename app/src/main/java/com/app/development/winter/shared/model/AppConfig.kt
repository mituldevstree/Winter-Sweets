package com.app.development.winter.shared.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AppConfig(
    @SerializedName("version") var versionName: String? = null,
    @SerializedName("hasUpdate") var hasUpdate: Boolean = false,
    @SerializedName("forceUpdate") var isForceUpdate: Boolean = false,
    @SerializedName("isForGiwy") var isForGiwy: Boolean = false,
    @SerializedName("isOnProduction") var isOnProduction: Boolean = false,
    @SerializedName("androidUrl") var androidUrl: String? = null,
    @SerializedName("date") var date: Long? = 0L
) : BaseObservable(), Parcelable {}