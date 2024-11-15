package com.app.development.winter.manager


import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.app.development.winter.BuildConfig
import com.app.development.winter.localcache.LocaleHelper
import com.givvy.invitefriends.builder.ReferralLibManager

object ReferralLibInitializer {

    private var wasInitialized: Boolean = false


    fun init(onSetupCompleted: (() -> Unit)? = null, onSetupFailure: (() -> Unit)? = null) {

        val langCode = LocaleHelper.getLanguage().locale
        val langName = LocaleHelper.getLanguage().name
        ReferralLibManager.initializeReferralLib(
            lifecycleOwner = ProcessLifecycleOwner.get(),
            applicationContext = com.app.development.winter.application.Controller.instance.applicationContext,
            applicationId = BuildConfig.APPLICATION_ID,
            versionName = BuildConfig.VERSION_NAME,
            languageCode = langCode,
            languageName = langName,
            needToEnableLogs = true,
            onSuccessCallback = {
                wasInitialized = true
                onSetupCompleted?.invoke()
                Log.e("ConfigCall", "Built successfully")
            },
            onFailureCallback = {
                wasInitialized = false
                onSetupFailure?.invoke()
                Log.e("ConfigCall", "FailedToBuild")
            }
        )
    }

    fun onConfigurationChange(/*newConfig: Configuration*/) {

        wasInitialized = false
        Log.e("AppConfig", "Changed")
        val langCode = LocaleHelper.getLanguage().locale
        val langName = LocaleHelper.getLanguage().name
        ReferralLibManager.updateReferralLibLocal(
            languageCode = langCode,
            languageName = langName,
            null
        )

        WalletLibManager.updateWithdrawLibLocal(
            languageCode = langCode,
            languageName = langName,
            loadingView = null
        )
        OfferWallLibInitializer.updateAppLibLocal()
    }

    fun wasLibInitialized(): Boolean {
        return wasInitialized
    }
}