package com.app.development.winter.manager

import android.util.Log
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.app.development.winter.BuildConfig
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.localcache.LocaleHelper
import com.givvy.offerwall.app.builder.OfferwallLibBuilder

@Keep
object OfferWallLibInitializer {
    private var wasOfferwallInitialized: Boolean = false
    var offerwallLibInstance: Fragment? = null

    fun wasLibInitialized(): Boolean {
        return wasOfferwallInitialized
    }


    fun init() {
        val userId = LocalDataHelper.getUserDetail()?.id ?: ""
        val langCode = LocaleHelper.getLanguage().locale
        val langName = LocaleHelper.getLanguage().name

        OfferwallLibBuilder.initializeOfferLib(lifecycleOwner = ProcessLifecycleOwner.get(),
            applicationContext = Controller.instance,
            applicationId = BuildConfig.APPLICATION_ID,
            baseAppName = Controller.instance.applicationContext.getString(R.string.app_name),
            versionName = BuildConfig.VERSION_NAME,
            baseAppUserId = userId,
            languageCode = langCode,
            languageName = langName,
            needToEnableLogs = false,
            isTestModeEnabled = false,
            baseAppBuildConfigDebug = BuildConfig.DEBUG,
            onSuccessCallback = {
                wasOfferwallInitialized = true
                Log.e("ConfigCall", "Built successfully")
            },
            onFailureCallback = {
                wasOfferwallInitialized = false
                Log.e("ConfigCall", "FailedToBuild")
            })
    }


    /**
     * Method to trigger lib view.
     */
    fun requestToLoadWithdrawFragment(lifecycleOwner: LifecycleOwner) {
        OfferwallLibBuilder.getOfferwallUiFragmentInstance(
            lifecycleOwner = lifecycleOwner,
            sessionUserID = LocalDataHelper.getUserDetail()?.id ?: "",
            sessionUserName = LocalDataHelper.getUserDetail()?.name ?: "",
        )
    }


    fun updateAppLibLocal() {
        val langCode = LocaleHelper.getLanguage().locale
        val langName = LocaleHelper.getLanguage().name

        OfferwallLibBuilder.updateOfferwallLibConfig(
            BuildConfig.APPLICATION_ID,
            langCode,
            langName,
            null
        )
    }

    fun checkAndTriggerSpecialOffer(
        activity: FragmentActivity,
        onFailureCallback: (message: String?) -> Unit
    ) {
        OfferwallLibBuilder.checkIfSpecialRewardIsAvailable(
            sessionUserID = LocalDataHelper.getUserDetail()?.id ?: "",
            sessionUserName = LocalDataHelper.getUserDetail()?.name ?: ""
        ) { isAvailable, isProcessing, unAvailableReason ->
            when {
                isProcessing.not() && isAvailable -> {
                    triggerSpecialReward(activity, onFailureCallback)
                }

                isProcessing.not() && isAvailable.not() -> {
                    Log.e("SpecialReward", unAvailableReason.toString())
                    onFailureCallback.invoke("Special is currently no available.")
                }

                else -> {
                    Log.e("SpecialReward", "Loading")
                }
            }

        }
    }

    private fun triggerSpecialReward(
        activity: FragmentActivity,
        onFailureCallback: (message: String?) -> Unit
    ) {

        OfferwallLibBuilder.getSpecialRewardView(
            activity,
            specialRewardRequestCallback = { fragment, errorMessage ->
                if (fragment != null) {
                    fragment.show(activity.supportFragmentManager, "Special Reward")
                } else {
                    onFailureCallback.invoke(errorMessage)
                    Log.e("SpecialReward", "ErrorMessage:$errorMessage")
                }
            },
            onSpecialRewardProcessCallback = { wasOfferShown, offerFailureReason ->
                if (wasOfferShown.not()) {
                    onFailureCallback.invoke(offerFailureReason)
                }
            })


    }
}
