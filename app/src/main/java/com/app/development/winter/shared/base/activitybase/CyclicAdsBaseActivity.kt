package com.app.development.winter.shared.base.activitybase

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.application.Controller.Companion.floatingViewState
import com.app.development.winter.application.Controller.Companion.isCyclicAdsClicked
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.manager.InstallAppCheckManager
import com.app.development.winter.manager.OfferWallLibInitializer
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.shared.extension.clickOnRandomPosOfTheScreen
import com.app.development.winter.shared.extension.forceRestart
import com.app.development.winter.shared.extension.hasOverLayPermissionGranted
import com.app.development.winter.shared.extension.hasPostNotificationPermissionGranted
import com.app.development.winter.shared.extension.isNotAdsActivity
import com.app.development.winter.shared.extension.show
import com.app.development.winter.shared.extension.toast
import com.app.development.winter.ui.session.event.SessionEvent
import com.app.development.winter.ui.session.model.FloatingViewState
import com.app.development.winter.ui.session.service.AdvanceBaseFloatingViewService
import com.app.development.winter.ui.session.service.AdvanceBaseFloatingViewService.Companion.isServiceRunning
import com.app.development.winter.ui.session.service.FloatingViewServiceState
import com.app.development.winter.ui.session.service.SessionOverlayService
import com.app.development.winter.ui.session.service.UserStatisticsOverlayService
import com.app.development.winter.ui.session.viewmodel.SessionConfigViewModel
import com.app.development.winter.utility.ViewUtil
import com.app.development.winter.utility.ViewUtil.restartApp
import com.givvy.facetec.network.security.AES.toJson
import com.monetizationlib.data.Monetization
import com.monetizationlib.data.Observer
import com.monetizationlib.data.base.view.utility.StepProcessingError
import com.monetizationlib.data.manager.CyclicAdsManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class CyclicAdsBaseActivity<
        VDB : ViewDataBinding,
        INTENT : ViewIntent,
        STATE : ViewState,
        VM : AdvanceBaseViewModel<INTENT, STATE>,
        >
    (bindingFactory: (LayoutInflater) -> VDB, modelClass: Class<VM>) :
    MonetizationBaseActivity<VDB, INTENT, STATE, VM>(bindingFactory, modelClass), Observer {
    private val LOG_TAG: String = "CheckCycleAds"

    private var putBackgroundByAds: Boolean = false
    private var isReceiverRegistered: Boolean = false
    private val mSessionViewModel: SessionConfigViewModel by viewModels()
    private var requirePermissionDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeSessionEvents()
        manageFloatingViewState()
    }


    override fun onLibInitialized() {
        if (Controller.wasMonetizationInitialized) {
            triggerNotificationPermission()
            if (cycleAdManager == null) {
                cycleAdManager = CyclicAdsManager().setUpCycleAdConfig(
                    context = this@CyclicAdsBaseActivity,
                    lifecycleScope = lifecycleScope,
                    maxTryForAds = LocalDataHelper.getUserDetail()?.maxTryForAds ?: 20,
                    maxAdLoadAttempts = LocalDataHelper.getUserDetail()?.maxAdLoadAttempts
                        ?: 80,
                    nextAdsLoadsWaitTime = 10000, //LocalDataHelper.getUserDetail()?.firstAdsLoadsWaitTime ?: 10000,
                    videoAdsWaitTimeForClickAndOpen = LocalDataHelper.getUserDetail()?.videoAdsWaitTimeForClickAndOpen
                        ?: 10000,
                    waitTimeForTheAdsClick = LocalDataHelper.getUserDetail()?.waitTimeForTheAdsClick
                        ?: 2000,
                    waitTimeForTheReopenApp = LocalDataHelper.getUserDetail()?.waitTimeForTheReopenApp
                        ?: 10000
                ).build()
                checkIfSessionIsActive(LocalDataHelper.haveActiveSession)
                showAppRatingDialog()
                observerCycleAdCallbacks()
                checkIfSessionIsActive(false)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun observerCycleAdCallbacks() {
        lifecycleScope.launch {
            cycleAdManager?.cyclicAdsCallback?.collectLatest {
                ViewUtil.printLog(LOG_TAG, "${it}")
                when (it) {
                    CyclicAdsManager.CheckAdsLoadedCallBack.AdsShown -> {
                        ViewUtil.printLog(LOG_TAG, "BaseApp: AdShown")
                        mSessionViewModel.getSessionUiState()
                            .handleEvent(SessionEvent.RequestRewardForAd)
                    }

                    CyclicAdsManager.CheckAdsLoadedCallBack.ClickOnAd -> {
                        if (this@CyclicAdsBaseActivity.isNotAdsActivity().not()) {
                            MainScope().launch {
                                isCyclicAdsClicked = true
                                ViewUtil.printLog(LOG_TAG, "BaseApp: ClickTriggered")
                                Controller.foregroundView?.clickOnRandomPosOfTheScreen()
                                apiCallForClickOnAds()
                            }

                        }
                    }

                    is CyclicAdsManager.CheckAdsLoadedCallBack.BringAppToFront -> {
                        GlobalScope.launch {
                            delay(500)
                            ViewUtil.printLog(LOG_TAG, "BaseApp:BringAppToFront")
                            restartApp(this@CyclicAdsBaseActivity)
                        }

                    }

                    is CyclicAdsManager.CheckAdsLoadedCallBack.RestartApp -> {
                        GlobalScope.launch {
                            delay(500)
                            ViewUtil.printLog(LOG_TAG, "BaseApp:ForceRestart")
                            forceRestart()
                        }

                    }
                }
            }
        }
    }

    override fun checkAndStartSession() {
        checkIfSessionIsActive(true)
    }

    private fun checkIfSessionIsActive(hasActiveSession: Boolean = false) {
        hideProgressBar()
        if (hasActiveSession) {
            checkDailyLimitReached(shouldCheckAdLoaded = false) { startSession ->
                if (startSession) {
                    checkSessionIsEnabled()
                }
            }
        } else if ((LocalDataHelper.getUserDetail()?.shouldShowCycleAds == true || LocalDataHelper.getUserDetail()?.cycleAdsForRentApp == true) && LocalDataHelper.getUserDetail()?.dailyEarningLimitReached == false) {
            checkDailyLimitReached(shouldCheckAdLoaded = false) { startSession ->
                if (startSession) {
                    loadAutoAds()
                }
            }
        }
    }

    private fun observeSessionEvents() {
        lifecycleScope.launch {
            mSessionViewModel.getSessionUiState().collectLatest { sessionUiState ->
                ViewUtil.printLog(LOG_TAG, "SessionEvent : ${sessionUiState.toJson()}")
                when (sessionUiState.loadingState.first) {
                    AdvanceBaseViewModel.LoadingType.GET_CYCLE_AD_REWARD -> {
                        if (sessionUiState.loadingState.second == AdvanceBaseViewModel.LoadingState.COMPLETED) {
                            onWithdrawBalanceUpdate()
                        }
                    }

                    else -> {

                    }

                }
            }
        }

    }


    private fun updateOverlayInfo(callback: () -> Unit) {
        MainScope().launch { hideProgressBar() }
        if (hasOverLayPermissionGranted()) {
            if (isServiceRunning.not()) {
                showFloatingViewService(callback)
            }
        }
    }


    /**
     * Cyclic ad flow setup.
     */
    private fun checkSessionIsEnabled() {
        if (LocalDataHelper.isOnProduction()) {
            if (hasPostNotificationPermissionGranted()) {
                if (!hasOverLayPermissionGranted()) {
                    requestOverlayPermission { isGranted ->
                        if (isGranted) {
                            showFlowOnOverlayGrant()
                        }
                    }
                } else {
                    showFlowOnOverlayGrant()
                }
            } else {
                triggerNotificationPermission()
            }
        } else {
            openSessionActivity()
        }
    }

    private fun canStartCycleAds(): Boolean {
        return LocalDataHelper.getUserDetail()?.cycleAdsForRentApp == true || LocalDataHelper.getUserDetail()?.shouldShowCycleAds == true && LocalDataHelper.getUserDetail()?.dailyEarningLimitReached == false
    }

    /**
     * Cycle to load ads.
     */
    private fun loadAutoAds() {
        if (cycleAdManager?.canStartCycleAds()?.not() == true) {
            ViewUtil.printLog(LOG_TAG, "SessionStarted")
            cycleAdManager?.setCanStartCycleAds(isServiceRunning || canStartCycleAds())
            cycleAdManager?.startCycleAds()
        }
    }

    private fun apiCallForClickOnAds() {
        InstallAppCheckManager.getAllInstalledPackage { isPackageInstalled ->
            if (isPackageInstalled) {
                mSessionViewModel.userClickOnAd(onSuccessCallback = {
                    ViewUtil.printLog(LOG_TAG, "UserClickTransmitted")
                }, onFailureCallback = {
                    ViewUtil.printLog(LOG_TAG, "UserClickTransmitFailed")
                })
            }
        }
    }

    override fun onAdFailedToShow(shouldForceRestart: Boolean) {
        if (shouldForceRestart) {
            ViewUtil.printLog(LOG_TAG, "OnAdFailedToShowForceRestart")
            if (isServiceRunning) {
                GlobalScope.launch {
                    floatingViewState.emit(
                        FloatingViewServiceState(
                            floatingViewState = FloatingViewState.STOP_SERVICE, null
                        )
                    )
                }
                cycleAdManager?.stopCycleAds()
                forceRestart()
            }
        } else {
            Monetization.monetizationConfig?.shouldUseFairBid = true
            ViewUtil.printLog(LOG_TAG, "OnAdFailedToShowShouldBeHandledInTheLib")
        }
    }

    override fun onDownloadFeatureTriggeredByUser() {
        super.onDownloadFeatureTriggeredByUser()
        openDownloadFeatureFlow(wasTriggeredForSession = false, onCanContinueWithSession = null)
    }

    fun openDownloadFeatureFlow(
        wasTriggeredForSession: Boolean, onCanContinueWithSession: (() -> Unit)?
    ) {
        if (LocalDataHelper.isOnProduction()) {
            mBinding?.root?.findViewById<ConstraintLayout>(R.id.layoutAppLoaderView)
                ?.let { loadingView ->
                    loadingView.show()/*loadingView.findViewById<LottieAnimationView>(R.id.lottieAnimationView).let {
                        LottieAnimationUtil.setLoadingAnimation(it, isPlay = true)
                    }*/
                    Monetization.openDownloadFeature(loadingView = loadingView,
                        onStepProcessingFailure = object : (StepProcessingError) -> Unit {
                            override fun invoke(errorState: StepProcessingError) {
                                when (errorState) {
                                    StepProcessingError.APP_NOT_INSTALLED -> {
                                        toast(errorState.message)
                                    }

                                    StepProcessingError.AD_NOT_AVAILABLE -> {
                                        toast(getString(R.string.currently_ads_are_not_available))
                                    }

                                    StepProcessingError.API_ERROR -> {
                                        toast(errorState.message)
                                        if (wasTriggeredForSession) onCanContinueWithSession?.invoke()
                                    }

                                    StepProcessingError.AD_LIMIT_REACHED -> {
                                        toast(getString(R.string.you_have_reached_the_daily_earning_limit_in_this_app))
                                        if (wasTriggeredForSession) onCanContinueWithSession?.invoke()
                                    }
                                }
                            }
                        },
                        onUserCancel = {
                            onUserInfoUpdate()
                            if (wasTriggeredForSession) {
                                onCanContinueWithSession?.invoke()
                            }

                        },
                        onRewardClaimed = { earnedCredits ->
                            onUserInfoUpdate()
                        })
                }
        } else {
            if (wasTriggeredForSession) {
                onCanContinueWithSession?.invoke()
            }
        }
    }

    private fun showFlowOnOverlayGrant() {
        if (hasOverLayPermissionGranted()) {
            updateOverlayInfo { }
        }
    }

    override fun showUserStatistics() {
        if (hasPostNotificationPermissionGranted()) {
            if (!hasOverLayPermissionGranted()) {
                requestOverlayPermission { isGranted ->
                    if (isGranted) {
                        showUserStatisticsViewService()
                    }
                }
            } else {
                showUserStatisticsViewService()
            }
        } else {
            triggerNotificationPermission()
        }
    }

    private fun showFloatingViewService(callback: (() -> Unit)? = null) {
        Controller.setSystemBarHeight(this)
        val intent = Intent(this@CyclicAdsBaseActivity, SessionOverlayService::class.java)
        intent.action = AdvanceBaseFloatingViewService.ACTION_SHOW_WIDGET
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        callback?.invoke()
    }

    private fun showUserStatisticsViewService(callback: (() -> Unit)? = null) {
        Controller.windowHeight = WindowManager.LayoutParams.WRAP_CONTENT
        val intent = Intent(this@CyclicAdsBaseActivity, UserStatisticsOverlayService::class.java)
        intent.action = AdvanceBaseFloatingViewService.ACTION_SHOW_WIDGET
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        callback?.invoke()
    }

    override fun toggleBannerVisibility(isShow: Boolean) {
        MainScope().launch {
            if (LocalDataHelper.isOnProduction()) {
                Monetization.toggleBannerVisibility(isShow)
            } else {
                Monetization.toggleBannerVisibility(false)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun manageFloatingViewState() {
        GlobalScope.launch {
            floatingViewState.collectLatest {
                if (it != null) {
                    Log.e(LOG_TAG, "SessionState : ${it.floatingViewState}")
                    when (it.floatingViewState) {
                        FloatingViewState.CYCLE_ADS_ENDED -> {
                            toggleBannerVisibility(false)

                            GlobalScope.launch {
                                cycleAdManager?.stopCycleAds()
                                restartApp(this@CyclicAdsBaseActivity)
                                checkAndTriggerSpecialReward()
                            }
                        }

                        FloatingViewState.MINIMIZE_APP -> {
                            floatingViewState.emit(FloatingViewServiceState(floatingViewState = FloatingViewState.EMPTY))
                            toggleBannerVisibility(false)
                            GlobalScope.launch {
                                cycleAdManager?.stopCycleAds()
                                restartApp(this@CyclicAdsBaseActivity)
                                ViewUtil.minimizeApp(this@CyclicAdsBaseActivity)
                            }
                        }

                        FloatingViewState.CYCLE_ADS_STARTED -> {
                            LocalDataHelper.haveActiveSession = true
                            toggleBannerVisibility(true)
                            loadAutoAds()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun checkAndTriggerSpecialReward() {
        if (LocalDataHelper.isOnProduction()) {
            if (LocalDataHelper.getUserConfig()?.specialRewardTriggerIndex == 0) return
            LocalDataHelper.updateSessionCount { count -> count + 1 }
            if (LocalDataHelper.getSessionEnteredCount() % (LocalDataHelper.getUserConfig()?.specialRewardTriggerIndex
                    ?: 1) == 0
            ) {
                lifecycleScope.launch {
                    delay(500)
                    OfferWallLibInitializer.checkAndTriggerSpecialOffer(this@CyclicAdsBaseActivity) { failureMessage ->
                        LocalDataHelper.updateSessionCount { count -> count - 1 }
                    }
                }

            }

        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (isServiceRunning && LocalDataHelper.isOnProduction()) {
                    GlobalScope.launch {
                        floatingViewState.emit(FloatingViewServiceState(floatingViewState = FloatingViewState.ON_BACK_PRESSED))
                    }
                    return false
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
