package com.app.development.winter.shared.network.data

import com.app.development.winter.shared.network.ApiEndpoints
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response

object ParserHelper {
    fun baseError(error: ResponseBody?): BaseModel {
        return try {
            Gson().fromJson(error!!.charStream(), BaseModel::class.java)
        } catch (e: Exception) {
            BaseModel(0, ResponseHandler.handleErrorResponse(e))
        }
    }

    fun <T> baseError(response: Response<T>): BaseModel {
        return try {
            return if (response.code() == ApiEndpoints.RESPONSE_OK || response.code() == ApiEndpoints.RESPONSE_CREATED) {
                val baseModel = Gson().fromJson(response.body().toString(), BaseModel::class.java)
                baseModel.code = response.code()
                baseModel
            } else {
                val error = Gson().fromJson(response.errorBody()?.string(), BaseModel::class.java)
                error.code = response.code()
                error.message = error.message
                error
            }
        } catch (e: Exception) {
            BaseModel(response.code(), ResponseHandler.handleErrorResponse(e))
        }
    }
}