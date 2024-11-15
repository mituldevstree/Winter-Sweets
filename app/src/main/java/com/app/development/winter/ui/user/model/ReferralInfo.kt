package com.app.development.winter.ui.user.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.development.winter.BR
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ReferralInfo(
    @SerializedName("title") var title: String?,
    @SerializedName("name") var description: String?,
    @SerializedName("image") var image: Int?,
) : BaseObservable(), Parcelable {
    @SerializedName("selected")
    @Bindable
    private var selected: Boolean = false

    fun setSelected(selected: Boolean) {
        this.selected = selected
        notifyPropertyChanged(BR.selected)
    }

    fun getSelected(): Boolean {
        return selected
    }

    fun isTitleEmpty(): Boolean {
        return title.isNullOrEmpty()
    }
    fun isImageEmpty(): Boolean {
        return image == null || image == 0
    }
}