package com.app.development.winter.ui.notification.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import com.app.development.winter.shared.extension.fromHtml
import com.app.development.winter.shared.extension.isNotNullOrEmpty
import com.app.development.winter.utility.DateTimeUtil
import com.app.development.winter.utility.DateTimeUtil.DDMMYY
import com.app.development.winter.utility.ViewUtil
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Notification(
    @SerializedName("id") var id: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("body") var body: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("notificationType") var notificationType: String? = null,
    @SerializedName("deepLinkType") var deepLinkType: String? = null,
    @SerializedName("date") var date: Long = 0L,
    @SerializedName("coinValue") var coinValue: String = "",
    @SerializedName("isNew") var isNew: Boolean = false
) : Parcelable, BaseObservable() {

    fun getMessage(): String {
        return body?.fromHtml().toString()
    }

    fun getFormattedDate(context: Context): String {
        return ViewUtil.getTimeAgo(context, date, "dd/MM/yyyy").orEmpty()
    }

    fun getFormattedTime(): String? {
        return ViewUtil.getFormattedDate(date, "hh:mm aa")
    }

    fun getCoinValueOrTime(): String {
        return "$$coinValue".takeIf { coinValue.isNotNullOrEmpty() } ?: ""
    }

    fun checkMessageDateIsBigger(previousDate: Long): Boolean {
        return DateTimeUtil.getFormattedDateTime(date, DDMMYY) == DateTimeUtil.getFormattedDateTime(
            previousDate, DDMMYY
        )
    }
}