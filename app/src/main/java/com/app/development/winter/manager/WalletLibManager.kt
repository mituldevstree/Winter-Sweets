package com.app.development.winter.manager

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ProcessLifecycleOwner
import com.app.development.winter.BuildConfig
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.localcache.LocaleHelper
import com.givvy.withdrawfunds.builder.WalletLibInitializer

object WalletLibManager {

    private var wasInitialized: Boolean = false

    private var mWithdrawFragmentInstance: Fragment? = null

    fun initialize() {
        Log.e("ConfigCall", "InitRequest")
        WalletLibInitializer.initializeWithdrawLib(
            lifecycleOwner = ProcessLifecycleOwner.get(),
            applicationContext = Controller.instance.applicationContext,
            applicationId = BuildConfig.APPLICATION_ID,
            facebookAppId = BuildConfig.FACEBOOK_APP_ID.toString(),
            languageCode = LocaleHelper.getLanguage().locale,
            languageName = LocaleHelper.getLanguage().name,
            needToEnableLogs = true,
            onSuccessCallback = {
                wasInitialized = true
                Log.e("ConfigCall", "Built successfully")
            },
            onFailureCallback = {
                wasInitialized = false
                Log.e("ConfigCall", "FailedToBuild")
            }
        )
    }


    fun wasLibInitialized(): Boolean {
        return wasInitialized
    }

    fun resetLib() {
        wasInitialized = false
    }

    fun saveWithdrawFragmentInstance(fragment: Fragment) {
        mWithdrawFragmentInstance = fragment
    }

    /*
    * send updated app local
     */
    fun updateWithdrawLibLocal(
        languageCode: String,
        languageName: String,
        loadingView: ViewGroup?
    ) {
        WalletLibInitializer.updateWalletLibLocal(languageCode, languageName, loadingView)
    }

    fun getWithdrawFragment(): Fragment? {
        return mWithdrawFragmentInstance
    }

    fun openBonusFlow(
        fragmentManager: FragmentManager,
        callback: (wasCodeProcessed: Boolean) -> Unit
    ) {
        WalletLibInitializer.triggerBonusFlow(
            sessionUserID = LocalDataHelper.getUserDetail()?.id ?: "",
            sessionUserEmailId = LocalDataHelper.getUserDetail()?.email ?: "",
            applicationId = BuildConfig.APPLICATION_ID,
            languageCode = LocaleHelper.getLanguage().locale,
            languageName = LocaleHelper.getLanguage().name,
            fragmentManager = fragmentManager
        ) { onCodeProcessed: Boolean ->
            callback.invoke(onCodeProcessed)
        }
    }

    fun openContactUsFlow(context: Context) {
        WalletLibInitializer.showContactUs(
            sessionUserName = LocalDataHelper.getUserDetail()?.name ?: "",
            sessionUserEmailId = LocalDataHelper.getUserDetail()?.email ?: "",
            baseAppName = context.getString(R.string.app_name),
            context = context,
        )
    }
}
