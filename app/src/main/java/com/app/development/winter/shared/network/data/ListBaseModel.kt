package com.app.development.winter.shared.network.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
open class ListBaseModel<T>(@SerializedName("result") var data: MutableList<T>?) :
    BaseModel(0, "Something went wrong", "Something went wrong") {
    @SerializedName("current_page")
    val currentPage: Int = 0

    @SerializedName("total_records")
    val totalRecords: Int = 0

    @SerializedName("total_page")
    val totalPage: Int = 0

    class Error<T>(errorCode: Int, errorMessage: String, data: MutableList<T>?) :
        ListBaseModel<T>(data) {
        init {
            message = errorMessage
            code = errorCode
        }
    }
}
