package com.app.development.winter.shared.network.data

import android.util.Log
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.data.ResponseHandler.getErrorMessage
import com.app.development.winter.shared.network.data.ResponseHandler.handleErrorResponse
import retrofit2.Response

abstract class BaseApiResponse {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<ObjectBaseModel<T>>): ObjectBaseModel<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    return it
                }
            }
            return error(response.code(), getErrorMessage(response.code()))
        } catch (e: Exception) {
            Log.e("Exception", e.message.toString())
            return error(ApiEndpoints.RESPONSE_ERROR, handleErrorResponse(e))
        }
    }


    private fun <T> error(errorCode: Int, errorMessage: String): ObjectBaseModel<T> =
        ObjectBaseModel.Error(errorCode, errorMessage, null)


    suspend fun <T> safeApiCallList(apiCall: suspend () -> Response<ListBaseModel<T>>): ListBaseModel<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    return it
                }
            }
            return listError(response.code(), getErrorMessage(response.code()))
        } catch (e: Exception) {
            Log.e("Exception", e.message.toString())
            return listError(ApiEndpoints.RESPONSE_ERROR, handleErrorResponse(e))
        }
    }

    private fun <T> listError(errorCode: Int, errorMessage: String): ListBaseModel<T> =
        ListBaseModel.Error(errorCode, errorMessage, null)
}