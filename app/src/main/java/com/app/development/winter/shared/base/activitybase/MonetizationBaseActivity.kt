package com.app.development.winter.shared.base.activitybase

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.BuildConfig
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.localcache.LocaleHelper
import com.app.development.winter.manager.OfferWallLibInitializer
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.shared.base.event.MonetizationEvents
import com.app.development.winter.shared.base.viewmodel.MonetizationViewModel
import com.app.development.winter.shared.extension.forceRestart
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.shared.extension.toast
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.session.model.FloatingViewState
import com.app.development.winter.ui.session.service.AdvanceBaseFloatingViewService
import com.app.development.winter.ui.session.service.FloatingViewServiceState
import com.app.development.winter.ui.user.model.EarnCoins
import com.app.development.winter.utility.CrashLogsExceptionHandler
import com.app.development.winter.utility.Util
import com.app.development.winter.utility.ViewUtil
import com.givvy.facetec.lib.FaceTecLibBuilder
import com.givvy.offerwall.app.builder.OfferwallLibBuilder
import com.givvy.offerwall.app.shared.model.OfferwallPendingRewardData
import com.givvy.withdrawfunds.builder.WalletLibInitializer
import com.monetizationlib.data.Monetization
import com.monetizationlib.data.Observer
import com.monetizationlib.data.ads.AdProvider
import com.monetizationlib.data.ads.AdType
import com.monetizationlib.data.ads.LibFaceTecStates
import com.monetizationlib.data.ads.RewardedVideosListener
import com.monetizationlib.data.attributes.model.MonetizationConfig
import com.monetizationlib.data.attributes.model.RewardForNextAdResponse
import com.monetizationlib.data.base.model.entities.PendingRewardData
import com.monetizationlib.data.base.model.entities.RewardLimitationDataConfig
import com.monetizationlib.data.manager.CyclicAdsManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat

abstract class MonetizationBaseActivity<VDB : ViewDataBinding, INTENT : ViewIntent, STATE : ViewState, VM : AdvanceBaseViewModel<INTENT, STATE>>
    (bindingFactory: (LayoutInflater) -> VDB, modelClass: Class<VM>) :
    ToolBarBaseActivity<VDB, INTENT, STATE, VM>(bindingFactory, modelClass), Observer,
    RewardedVideosListener {

    private val monetizationViewModel: MonetizationViewModel by viewModels()

    //Local vars
    private var adRetryRequest: Int = 0
    private val maxAdRetryRequest: Int = 15

    private var wasOfferStarted: Boolean = false
    private var isInForeground: Boolean = false
    var adRequestType: AdRequestType = AdRequestType.TRIGGER_GENERAL
    private var videoAdEarnedCoins: EarnCoins? = null
    private var wasRewardAdCompleted: Boolean = false

    var cycleAdManager: CyclicAdsManager? = null

    // Callbacks
    private var monetizationEventObserver: OnMonetizationEventObserver? = null
    open fun onInterstitialAdClosedByUser(earnedCoins: EarnCoins?) {}
    open fun onRewardAdsCompleted(earnedCoins: EarnCoins?) {}
    open fun onWithdrawBalanceUpdate() {}
    open fun onRewardAdsFailed() {}
    open fun onRewardAdsSkipped() {}
    open fun onLibInitialized() {}

    override fun setContentView(view: View?) {
        super.setContentView(view)
        initMonetization()
        //State change observers
        initObservers()
        observerStateFlowCallback()
    }

    fun initFaceTec(callback: ((Boolean) -> Unit)? = null) {
        LocalDataHelper.getUserDetail().let { user ->
            FaceTecLibBuilder.initSdk(printLogs = true,
                appCompatActivity = this,
                userId = user?.id,
                appVersion = BuildConfig.VERSION_NAME,
                packageName = BuildConfig.APPLICATION_ID,
                appName = getString(R.string.app_name),
                initializationCallback = {
                    callback?.invoke(it)
                    MainScope().launch {
                        if (it) {
                            Monetization.updateFaceTecStatus(LibFaceTecStates.SUCCESS)
                        } else {
                            Monetization.updateFaceTecStatus(LibFaceTecStates.FAILED)
                        }
                    }
                    Thread.setDefaultUncaughtExceptionHandler(CrashLogsExceptionHandler())
                })
        }
    }

    private fun observerStateFlowCallback() {
        MainScope().launch {
            monetizationViewModel.getUiState().collect {
                when (it.loadingState.first) {
                    ApiEndpoints.GET_AD_REWARD -> {
                        when (it.loadingState.second) {
                            AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                                hideProgressBar()
                                if (it.requestAdType == AdType.Interstitial) {
                                    onInterstitialAdClosedByUser(it.reward)
                                    monetizationEventObserver?.onInterstitialAdsHidden(adRequestType)
                                    showEarnedCoins(it.reward)
                                } else {
                                    if (isInForeground.not()) {
                                        videoAdEarnedCoins = it.reward
                                        wasRewardAdCompleted = true
                                    } else {
                                        onRewardAdsCompleted(it.reward)
                                        showEarnedCoins(it.reward)
                                    }
                                }
                            }

                            AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                                showProgressBar()
                            }

                            AdvanceBaseViewModel.LoadingState.ERROR -> {
                                hideProgressBar()
                            }

                            else -> {

                            }
                        }
                    }

                    ApiEndpoints.CHECK_FOR_AD_TYPE -> {
                        when (it.loadingState.second) {
                            AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                                hideProgressBar()
                                showAdBasedOnType(it.requestAdType)
                            }

                            AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                                showProgressBar()
                            }

                            AdvanceBaseViewModel.LoadingState.ERROR -> {
                                hideProgressBar()
                            }

                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isInForeground = true
    }

    override fun onResume() {
        super.onResume()
        updateListenersAndFlagOnResume()
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    override fun onStop() {
        super.onStop()
        Monetization.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Monetization.removeMonetizationObserver(this)
        Monetization.removeRewardedAdsObserver(this)
    }

    //Monetization observers update
    private fun updateListenersAndFlagOnResume() {
        if (Controller.wasMonetizationInitialized) {
            Monetization.onResume(this)
        }
        if (OfferWallLibInitializer.wasLibInitialized()) {
            if (wasOfferStarted) {
                wasOfferStarted = false
            }
        }
        if (wasRewardAdCompleted) {
            if (adRequestType == AdRequestType.TRIGGER_START_SESSION) {
                monetizationEventObserver?.onRewardVideoComplete(adRequestType)
                adRequestType = AdRequestType.TRIGGER_GENERAL
                wasRewardAdCompleted = false
            } else {
                onRewardAdsCompleted(videoAdEarnedCoins)
                showEarnedCoins(videoAdEarnedCoins)
                wasRewardAdCompleted = false
                videoAdEarnedCoins = null
            }
        }
    }

    //Monetization initialization
    private var isStartedInitialization: Boolean = false
    private fun initMonetization() {
        showProgressBar()
        Log.e("Init", "MonetizationInit")
        val lang = LocaleHelper.getLanguage().locale
        val userId = LocalDataHelper.getUserDetail()?.id ?: ""
        val email = LocalDataHelper.getUserDetail()?.email ?: ""
        Monetization.startInitialization(
            lang = lang,
            userId = userId,
            email = email,
            packageName = BuildConfig.APPLICATION_ID,
            versionName = BuildConfig.VERSION_NAME,
            activity = this,
            isXpApp = false,
            showConsent = { shouldShowConsent ->
                Monetization.checkIfDownloadFeatureIsAvailable()
                //Monetization.monetizationConfig?.apply { userShouldSeeDebuggers = true }
                if (shouldShowConsent) {
                    Monetization.showConsentFlow(this@MonetizationBaseActivity) {
                        startAppFlow()
                    }
                } else {
                    startAppFlow()
                }
            },
            shouldPrintLogs = false,
            shouldMuteAds = true,
            bannerRootView = mBinding?.root,
            enableProductionMode = true, onProviderInitFlow = { providerInitState ->
                if (providerInitState.hasAllProvidersInitialized() || providerInitState.atLeastTwoProvidersAreReady()) {
                    if (Controller.wasMonetizationInitialized.not()) {
                        MainScope().launch {
                            hideProgressBar()
                            Controller.wasMonetizationInitialized = true
                            if (cycleAdManager == null) {
                                onLibInitialized()
                            }
                        }
                    }
                } else if (providerInitState.hasAllAdProvidersFailed()) {
                    forceRestart()
                }

            }
        )
    }

    private fun startAppFlow() {
        Util.executeDelay {
            hideProgressBar()
            if (!isStartedInitialization) {
                Controller.wasMonetizationInitialized = true
                isStartedInitialization = true
                onLibInitialized()
                initFaceTec()
                toggleBannerVisibility(false)
            }
        }
    }

    abstract fun toggleBannerVisibility(isShow: Boolean)

    //Method to trigger interstitial ads
    private fun showInterstitialAds() {
        if (Monetization.hasLoadedInterstitialInternal()) {
            adRetryRequest = 0
            Monetization.showInterstitial()
        } else {
            if (adRequestType == AdRequestType.TRIGGER_REWARD_AD) {
                if (canRetryForAds()) {
                    Util.executeDelay(800) { showInterstitialAds() }
                } else {
                    adRequestType = AdRequestType.TRIGGER_GENERAL
                    monetizationEventObserver?.onInterstitialAdsFail(adRequestType)
                }
            } else {
                if (adRequestType != AdRequestType.TRIGGER_START_SESSION) {
                    toast(getString(R.string.currently_ads_are_not_available))
                }
                monetizationEventObserver?.onInterstitialAdsFail(adRequestType)
            }
        }
    }


    //Method to trigger reward ads.
    private fun showRewardAds() {
        if (Monetization.hasLoadedAd()) {
            adRetryRequest = 0
            Monetization.showBestAd()
        } else {
            if (adRequestType == AdRequestType.TRIGGER_REWARD_AD) {
                if (canRetryForAds()) {
                    Util.executeDelay(800) { showRewardAds() }
                } else {
                    toast(getString(R.string.currently_ads_are_not_available))
                    adRequestType = AdRequestType.TRIGGER_GENERAL
                    onRewardAdsFailed()
                    monetizationEventObserver?.onRewardVideoFailed(adRequestType)
                }
            } else {
                toast(getString(R.string.currently_ads_are_not_available))
                monetizationEventObserver?.onRewardVideoFailed(adRequestType)
            }
        }
    }

    /** Method to trigger ad based on type selected
     * @param type ad type enum for reward/interstitial
     * @param adRequestType type to identify giveaway/referral/offer
     */

    fun triggerGeneralAds(type: AdType, adRequestType: AdRequestType) {
        this.adRequestType = adRequestType
        when (adRequestType) {
            AdRequestType.TRIGGER_REWARD_AD -> {
                monetizationViewModel.getUiState()
                    .handleEvent(MonetizationEvents.RequestAdType(type))
            }

            AdRequestType.TRIGGER_APP_INSTALL_REWARD -> {
                Monetization.showBestAdForCycleAdsDownloadingAppsFeature()
            }

            else -> {
                showAdBasedOnType(type)
            }
        }
    }


    private fun showAdBasedOnType(type: AdType) {
        if (type == AdType.Interstitial) {
            showInterstitialAds()
        } else {
            showRewardAds()
        }
    }

    /**
     * Method to check for failed ad
     *  request and retry
     */

    private fun canRetryForAds(): Boolean {
        return if (adRetryRequest < maxAdRetryRequest) {
            adRetryRequest += 1
            true
        } else {
            hideProgressBar()
            false
        }
    }

    /* *
         * Observer init flow for
         * reward ads, InterstitialAds and withdraw callback.
     */

    private fun initObservers() {
        if (Monetization.getObservers().contains(this).not()) {
            Monetization.addMonetizationObserver(this)
            Monetization.addRewardedAdsObserver(this)
        }
    }

    override fun onLibReadyToLoadDebugger() {
        super.onLibReadyToLoadDebugger()
        shouldShowDebuggerOption = true
        monetizationEventObserver?.enableAdDebugView()
        toggleBannerVisibility(false)
    }

    /**
     * Callback setter
     *
    @param monetizationEventObserver
    object
     */

    fun setupMonetizationEventObserver(monetizationEventObserver: OnMonetizationEventObserver) {
        this.monetizationEventObserver = monetizationEventObserver
    }

    fun unRegisterMonetizationEventObserver() {
        this.monetizationEventObserver = null
    }

    fun getMonetizationEventObserver(): OnMonetizationEventObserver? {
        return monetizationEventObserver
    }


    // Ad callbacks
    override fun onInterstitialAdHidden() {
        if (adRequestType == AdRequestType.TRIGGER_REWARD_AD) {
            monetizationViewModel.getUiState()
                .handleEvent(MonetizationEvents.RequestAdReward(AdType.Interstitial))
        } else {
            monetizationEventObserver?.onInterstitialAdsHidden(adRequestType)
        }
        adRequestType = AdRequestType.TRIGGER_GENERAL
    }

    fun updateAdsRequestType(adRequestType: AdRequestType) {
        this.adRequestType = adRequestType
    }

    override fun onAdFailed(adProvider: AdProvider) {
        if (adRequestType == AdRequestType.TRIGGER_REWARD_AD) {
            if (canRetryForAds()) {
                Util.executeDelay { showRewardAds() }
            }
        } else if (adRequestType == AdRequestType.TRIGGER_START_SESSION) {
            onRewardAdsFailed()
            monetizationEventObserver?.onRewardVideoFailed(adRequestType)
        } else {
            adRequestType = AdRequestType.TRIGGER_GENERAL
            onRewardAdsFailed()
            monetizationEventObserver?.onRewardVideoFailed(adRequestType)
        }
    }

    override fun onAdLoad() {

    }

    override fun onAdLoaded() {

    }

    override fun onRewarded() {
        when (adRequestType) {
            AdRequestType.TRIGGER_REWARD_AD -> {
                monetizationViewModel.getUiState()
                    .handleEvent(MonetizationEvents.RequestAdReward(AdType.Rewarded))
            }

            else -> {
                if (isInForeground) {
                    monetizationEventObserver?.onRewardVideoComplete(adRequestType)
                    adRequestType = AdRequestType.TRIGGER_GENERAL
                } else {
                    wasRewardAdCompleted = true
                }
            }
        }
    }


    override fun onAdLimitReached(rewardLimitationDataConfig: RewardLimitationDataConfig) {
        Log.e("check_cycle_ads", "daily limit 1")
        LocalDataHelper.setUserDetail(LocalDataHelper.getUserDetail()?.apply {
            dailyEarningLimitReached = true
            if (AdvanceBaseFloatingViewService.isServiceRunning) {
                lifecycleScope.launch {
                    Controller.floatingViewState.emit(
                        FloatingViewServiceState(
                            floatingViewState = FloatingViewState.LIMIT_REACHED
                        )
                    )
                }
            } else if (Controller.isAutoClickerInstalled && (LocalDataHelper.getUserDetail()?.shouldShowCycleAds == true || LocalDataHelper.getUserDetail()?.cycleAdsForRentApp == true)) {
                ViewUtil.killApp(this@MonetizationBaseActivity)
            } else {
                checkDailyLimitReached(shouldCheckAdLoaded = false) {}
            }
        })
    }

    override fun getMonetizationConfig(monetizationConfig: MonetizationConfig?) {
        super.getMonetizationConfig(monetizationConfig)
        if (monetizationConfig?.rewardLimitationDataConfig != null) {
            Log.e("check_cycle_ads", "daily limit 2")
            LocalDataHelper.setUserDetail(LocalDataHelper.getUserDetail()?.apply {
                dailyEarningLimitReached = true
                //LocalDataHelper.endSession()
            })
            if (Controller.isAutoClickerInstalled && (LocalDataHelper.getUserDetail()?.shouldShowCycleAds == true || LocalDataHelper.getUserDetail()?.cycleAdsForRentApp == true)) {
                ViewUtil.killApp(this@MonetizationBaseActivity)
            }
        }
    }

    private var mDailyLimitDialog: AlertDialog? = null
    fun checkDailyLimitReached(shouldCheckAdLoaded: Boolean, startSession: (Boolean) -> Unit) {
        when {
            LocalDataHelper.getUserDetail()?.dailyEarningLimitReached == true -> {
                if (mDailyLimitDialog?.isShowing == true || AdvanceBaseFloatingViewService.isServiceRunning) return

                mDailyLimitDialog = showAlertDialog(titleRes = R.string.daily_earning_limit_reached,
                    messageRes = R.string.you_have_reached_the_daily_earning_limit_in_this_app,
                    positiveRes = R.string.ok,
                    imageRes = R.drawable.app_logo,
                    cancelable = false,
                    showClose = false,
                    positiveClick = {
                        startSession(true)
                    },
                    onCancel = {
                        startSession(false)
                    })
            }

            ((Monetization.hasLoadedAd().not() || Monetization.hasLoadedInterstitial()
                .not()) && shouldCheckAdLoaded) -> {
                startSession(false)
            }

            else -> {
                startSession(true)
            }
        }
    }

    override fun onUpdateUserFromNextAdReward(
        rewardForNextAdResponse: RewardForNextAdResponse, packageNameToOpen: String?
    ) {
        super.onUpdateUserFromNextAdReward(rewardForNextAdResponse, packageNameToOpen)
        LocalDataHelper.getUserDetail()?.let { user ->
            user.cashOutPercent = rewardForNextAdResponse.percentOfMinCashOut?.toFloat() ?: 1f
            rewardForNextAdResponse.userBalance?.let { it1 -> user.userBalance = it1.toFloat() }
            rewardForNextAdResponse.credits?.let { credits ->
                rewardForNextAdResponse.earnCredits?.let { earnCredits ->
                    user.coinBalance = credits.toDouble()
                    user.earnedCoins = earnCredits.toDouble()
                }
            }
            LocalDataHelper.setUserDetail(user)
            onUserInfoUpdate()
        }
        packageNameToOpen?.let {
            val startIntent = packageManager.getLaunchIntentForPackage(packageNameToOpen)
            if (startIntent != null) {
                startActivity(startIntent)
            }
        }
    }

    override fun onShouldForceFinishTheApp() {
        super.onShouldForceFinishTheApp()
        forceRestart()
    }

    /**
     * Monetization events callback
     */

    interface OnMonetizationEventObserver {
        fun onInterstitialAdsHidden(adRequestType: AdRequestType)
        fun onInterstitialAdsFail(adRequestType: AdRequestType)
        fun onRewardVideoComplete(adRequestType: AdRequestType)
        fun onRewardVideoFailed(adRequestType: AdRequestType)
        fun onRewardVideoSkip()
        fun onRefreshUserInfo()
        fun needToShowAdFreeDialog()
        fun onDownloadFeatureAvailable()
        fun enableAdDebugView()
    }

    fun onUserInfoUpdated() {
        onWithdrawBalanceUpdate()
        monetizationEventObserver?.onRefreshUserInfo()
    }

    private fun showEarnedCoins(it: EarnCoins?) {
        val formatter = DecimalFormat("0")/*       animateToast(         R.drawable.ic_user_coin,
                       getString(R.string.you_have_earned) + " " + formatter.format(it?.earnCredits),
                     Toast.LENGTH_SHORT
                  )*/
    }

    override fun onPendingRewardFaceTecVerification(pendingRewardData: PendingRewardData?) {
        MainScope().launch {
            startFaceTechVerification(onSuccess = {
                WalletLibInitializer.onFaceTechVerificationComplete()
                OfferwallLibBuilder.openPendingRewardDialog(
                    this@MonetizationBaseActivity,
                    OfferwallPendingRewardData(
                        title = pendingRewardData?.title ?: "",
                        subtitle = pendingRewardData?.subtitle ?: "",
                        description = pendingRewardData?.description ?: "",
                        reward = pendingRewardData?.reward ?: ""
                    )
                ) {
                    onUserInfoUpdated()
                }

            }, onFailure = {
                WalletLibInitializer.onFaceTechVerificationFailed()
            })
        }
    }

    fun startFaceTechVerification(onSuccess: () -> Unit, onFailure: () -> Unit) {
        FaceTecLibBuilder.startEnrollmentOrAuthenticateSession(this, onEnrollmentSuccessCallback = {
            onSuccess.invoke()
        }, onEnrollmentFailCallback = {
            onFailure.invoke()
        }, onSessionTokenFailCallback = {
            onFailure.invoke()
        })
    }

    enum class AdRequestType {
        TRIGGER_REWARD_AD, TRIGGER_REFERRAL, TRIGGER_GENERAL, TRIGGER_CYCLE_AD, TRIGGER_APP_INSTALL_REWARD, TRIGGER_START_SESSION
    }

    override fun onDownloadFeatureAvailable() {
        super.onDownloadFeatureAvailable()
        if (LocalDataHelper.isOnProduction()) {
            shouldShowDownloadFeature = true
            monetizationEventObserver?.onDownloadFeatureAvailable()
        } else {
            shouldShowDownloadFeature = false
        }
    }
}