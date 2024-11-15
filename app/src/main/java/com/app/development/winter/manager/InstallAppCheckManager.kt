package com.app.development.winter.manager

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.app.development.winter.shared.extension.getClassName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object InstallAppCheckManager {

    private val TAG: String = InstallAppCheckManager.getClassName()

    fun getAllInstalledPackage(callback: (Boolean) -> Unit) {
        val pm: PackageManager = com.app.development.winter.application.Controller.instance.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (packageInfo in packages) {
            if (packageInfo.packageName == "com.appearnings.givvyautoclicker") {
                return callback.invoke(true)
            }
        }
    }

    suspend fun getAdvertisementId(
        context: Context,
        callback: (AdvertisingIdClient.Info?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                callback.invoke(AdvertisingIdClient.getAdvertisingIdInfo(context))
            } catch (e: Exception) {
                Log.e(
                    "MY_APP_TAG",
                    "Failed to connect to Advertising ID provider.-> ${e.localizedMessage}"
                )
                callback.invoke(null)
            }
        }
    }
}