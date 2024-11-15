package com.app.development.winter.shared.network.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
open class ObjectBaseModel<T>(@SerializedName("result") var data: T?) :
    BaseModel(0, "Something went wrong", "Something went wrong") {
    class Error<T>(errorCode: Int, errorMessage: String, data: T?) : ObjectBaseModel<T>(data) {
        init {
            message = errorMessage
            code = errorCode
        }
    }
}
