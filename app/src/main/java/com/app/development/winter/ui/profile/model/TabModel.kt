package com.app.development.winter.ui.profile.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import com.app.development.winter.BR

@Keep
@Parcelize
data class TabModel(
    @SerializedName("name") var name: String,
    @SerializedName("number") var number: Int,
    @SerializedName("selected") var _selected: Boolean,
) : BaseObservable(), Parcelable {

    var selectedTab: Boolean
        @Bindable get() = _selected
        set(value) {
            _selected = value
            notifyPropertyChanged(BR.selectedTab)
        }
}