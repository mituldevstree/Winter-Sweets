package com.app.development.winter.shared.network.data

interface ResponseCallback<T> {
    fun onSuccess(response: T)
    fun onError(message: String, code: Int, t: Throwable? = null)
}