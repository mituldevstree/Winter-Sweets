package com.app.development.winter.ui.country.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.development.winter.BR
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Country(
    @SerializedName("name") var name: String? = null,
    @SerializedName("code") var code: String? = null,
    @SerializedName("selected") private var selected: Boolean,
) : BaseObservable(), Parcelable {
    var selectedLanguage: Boolean
        @Bindable get() = selected
        set(value) {
            selected = value
            notifyPropertyChanged(BR.selectedLanguage)
        }
}