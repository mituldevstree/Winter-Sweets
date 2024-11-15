package com.app.development.winter.shared.network.data

import android.accounts.NetworkErrorException
import android.util.MalformedJsonException
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.HttpsURLConnection

object ResponseHandler {

    fun handleErrorResponse(error: Throwable): String {
        return Controller.foregroundActivity?.let { context ->
            when (error) {
                is SocketTimeoutException -> context.getString(R.string.error_socket_timeout)
                is HttpException -> {
                    when (error.code()) {
                        HttpsURLConnection.HTTP_UNAUTHORIZED -> context.getString(R.string.error_unauthorised_user)
                        HttpsURLConnection.HTTP_FORBIDDEN -> context.getString(R.string.error_forbidden)
                        HttpsURLConnection.HTTP_INTERNAL_ERROR -> context.getString(R.string.error_internal_error)
                        HttpsURLConnection.HTTP_BAD_REQUEST -> context.getString(R.string.error_bad_request)
                        HttpsURLConnection.HTTP_UNAVAILABLE -> context.getString(R.string.error_unavailable)
                        else -> error.getLocalizedMessage()
                    }
                }

                is JsonSyntaxException, is MalformedJsonException -> context.getString(R.string.error_unavailable)
                is NetworkErrorException, is IOException -> context.getString(R.string.error_network)
                else -> error.message.toString()
            }
        } ?: error.message.toString()
    }

    fun getErrorMessage(code: Int): String {
        return Controller.foregroundActivity?.let { context ->
            when (code) {
                HttpsURLConnection.HTTP_UNAUTHORIZED -> context.getString(R.string.error_unauthorised_user)
                HttpsURLConnection.HTTP_FORBIDDEN -> context.getString(R.string.error_forbidden)
                HttpsURLConnection.HTTP_INTERNAL_ERROR -> context.getString(R.string.error_internal_error)
                HttpsURLConnection.HTTP_BAD_REQUEST -> context.getString(R.string.error_bad_request)
                HttpsURLConnection.HTTP_UNAVAILABLE -> context.getString(R.string.error_unavailable)
                else -> context.getString(R.string.error_something_went_wrong)
            }
        } ?: "Something went wrong, please check after sometime."
    }

    fun <T> handleNormalResponse(response: Response<T>, callback: ResponseCallback<T>?) {
        if (callback == null) return
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) callback.onSuccess(body)
            else callback.onError(response.message(), response.code())
        } else {
            val message = response.message().takeIf { response.code() == 200 }
                ?: ParserHelper.baseError(response.errorBody()).message
            callback.onError(message, response.code())
        }
    }

    fun <T> handleResponse(
        response: Response<ObjectBaseModel<T>>,
        callback: ResponseCallback<ObjectBaseModel<T>>?,
    ) {
        if (callback == null) return
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) callback.onSuccess(body)
            else callback.onError(response.message(), response.code())
        } else {
            val message = response.message().takeIf { response.code() == 200 }
                ?: ParserHelper.baseError(response.errorBody()).message
            callback.onError(message, response.code())
        }
    }

    /*fun <T> handleListResponse(
        response: Response<ListBaseModel<T>?>,
        callback: ResponseCallback<ListBaseModel<T>>?,
    ) {
        if (callback == null) return
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) callback.onSuccess(body)
            else callback.onError(response.message(), response.code())
        } else {
            val message = response.message().takeIf { response.code() == 200 }
                ?: ParserHelper.baseError(response.errorBody()).message
            callback.onError(message, response.code())
        }
    }*/
}