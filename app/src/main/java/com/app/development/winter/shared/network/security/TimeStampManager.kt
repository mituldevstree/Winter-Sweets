package com.app.development.winter.shared.network.security

object TimeStampManager {

    var backendTimeStamp: Long? = null
    var completedTimeStamp: Long? = null

    fun getTimeStampDiff(): Long {
        completedTimeStamp?.let { completedTimeStampWrapped ->
            val diff = System.currentTimeMillis() - completedTimeStampWrapped
            return backendTimeStamp?.plus(diff) ?: -1
        }
        return backendTimeStamp ?: -1
    }
}
