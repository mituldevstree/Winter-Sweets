package com.app.development.winter.ui.user.model

import android.os.Parcelable
import android.text.Spanned
import androidx.annotation.Keep
import com.app.development.winter.utility.Util
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Intro(
    @SerializedName("title") var title: String,
    @SerializedName("subTitle") var subTitle: String,
    @SerializedName("image") var image: String,
    @SerializedName("imageResource") var imageResource: Int,
    @SerializedName("description") var description: String,
    var index: Int = -1,
) : Parcelable {
    fun getTutorialDescription(): Spanned {
        val htmlAsString: String = description
        return Util.fromHTML(htmlAsString)
    }
}