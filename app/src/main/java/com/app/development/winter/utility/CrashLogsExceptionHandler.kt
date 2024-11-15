package com.app.development.winter.utility

import android.util.Log
import com.app.development.winter.BuildConfig
import com.app.development.winter.localcache.LocalDataHelper
import com.monetizationlib.data.attributes.model.AttributesNetworkFacade

class CrashLogsExceptionHandler : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e("tag_throwable", "uncaughtException: " + throwable.message)

        val deviceVersionDetails =
            "Version name: ${BuildConfig.VERSION_NAME}</br>Version code: ${BuildConfig.VERSION_CODE} </br>"
        // Create crash report
        val crashDetails = """
            Version details : $deviceVersionDetails
            Exception: ${throwable.localizedMessage}
            Stack trace: ${Log.getStackTraceString(throwable)}
        """.trimIndent()

        Log.e("CRASH-LOGS", "Crash occurred: $crashDetails", throwable)

        // Log crash to your server (this is just a sample, adapt it for your server)
        AttributesNetworkFacade.sendErrorMessage(
            userId = LocalDataHelper.getUserDetail()?.id ?: "",
            packageName = BuildConfig.APPLICATION_ID,
            message = crashDetails,
            success = { Log.e("CRASH-LOGS", "Success") },
            failure = { Log.e("CRASH-LOGS", "Failure") })

        // Call the default handler to handle the exception
        defaultHandler?.uncaughtException(thread, throwable)
    }

}