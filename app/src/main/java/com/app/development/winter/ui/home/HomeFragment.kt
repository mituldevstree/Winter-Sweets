package com.app.development.winter.ui.home

import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.databinding.FragmentHomeBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.activitybase.MonetizationBaseActivity
import com.app.development.winter.shared.base.viewbase.AdvancedBaseFragment
import com.app.development.winter.shared.extension.handleVisualOverlaps
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.ui.session.event.SessionEvent
import com.app.development.winter.ui.session.model.FloatingViewState
import com.app.development.winter.ui.session.service.AdvanceBaseFloatingViewService.Companion.isServiceRunning
import com.app.development.winter.ui.session.service.FloatingViewServiceState
import com.app.development.winter.ui.session.state.SessionUiState
import com.app.development.winter.ui.session.viewmodel.SessionConfigViewModel
import com.monetizationlib.data.ads.AdType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment :
    AdvancedBaseFragment<FragmentHomeBinding, SessionEvent, SessionUiState, SessionConfigViewModel>(
        FragmentHomeBinding::inflate,
        SessionConfigViewModel::class.java,
        isUseParentViewModel = true
    ), View.OnClickListener {

    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return mBinding?.layoutToolbar
    }

    override fun initUI() {
        mBinding?.layoutToolbar?.toolbar?.handleVisualOverlaps(true, Gravity.TOP)
        getToolBarBinding()?.showDownloadFeature = mTooBarBase?.shouldShowDownloadFeature
        getToolBarBinding()?.showAdDebuggerOption = mTooBarBase?.shouldShowDebuggerOption
        mBinding?.clickListener = this
        overlayFlowStates()
        if (isServiceRunning) {
            mBinding?.lottieHomeAnimation?.pauseAnimation()
        }
    }

    override fun initDATA() {
        registerObserverMonetizationEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterObserverMonetizationEvents()
    }

    override fun onPause() {
        super.onPause()
        mBinding?.lottieHomeAnimation?.pauseAnimation()
    }

    override fun onResume() {
        super.onResume()
        if (!isServiceRunning) {
            mBinding?.lottieHomeAnimation?.resumeAnimation()
        }
    }

    override fun render(state: SessionUiState) {
    }

    private fun overlayFlowStates() {
        launch {
            Controller.floatingViewState.collectLatest {
                when (it?.floatingViewState) {
                    FloatingViewState.CYCLE_ADS_STARTED -> {
                        mBinding?.lottieHomeAnimation?.pauseAnimation()
                    }

                    FloatingViewState.CYCLE_ADS_ENDED -> {
                        mBinding?.lottieHomeAnimation?.resumeAnimation()
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding?.btnPlay -> {
                mBinding?.lottieHomeAnimation?.pauseAnimation()
                lifecycleScope.launch {
                    if (isServiceRunning) {
                        Controller.floatingViewState.emit(
                            FloatingViewServiceState(
                                floatingViewState = FloatingViewState.STOP_STATS_VIEW, null
                            )
                        )
                    }
                }
                startSession()
            }
        }
    }


    private fun startSession() {
        lifecycleScope.launch {
            delay(500)
            if (!LocalDataHelper.isOnProduction()) {
                mBinding?.progressView?.show()
                delay(50)
                triggerGeneralAds(
                    adRequestType = MonetizationBaseActivity.AdRequestType.TRIGGER_START_SESSION,
                    type = AdType.Native
                )
            } else {
                mBinding?.lottieHomeAnimation?.pauseAnimation()
                delay(50)
                mCyclicAdsBase?.requestOverlayPermission { isGranted ->
                    if (isGranted) {
                        triggerGeneralAds(
                            adRequestType = MonetizationBaseActivity.AdRequestType.TRIGGER_START_SESSION,
                            type = AdType.Native
                        )
                    } else {
                        mBinding?.lottieHomeAnimation?.resumeAnimation()
                    }
                }
            }
        }
    }

    override fun onInterstitialAdsCompleted(adRequestType: MonetizationBaseActivity.AdRequestType) {
        triggerSession(adRequestType)
    }

    override fun onRewardAdsCompleted(adRequestType: MonetizationBaseActivity.AdRequestType) {
        triggerSession(adRequestType)
    }

    override fun onRewardAdsSkipped() {
        triggerSession(null)
    }

    override fun onInterstitialAdsFail(adRequestType: MonetizationBaseActivity.AdRequestType) {
        triggerSession(adRequestType)
    }

    override fun onInterstitialAdFail(adRequestType: MonetizationBaseActivity.AdRequestType) {
        triggerSession(adRequestType)
    }

    override fun onRewardAdsFailed(adRequestType: MonetizationBaseActivity.AdRequestType) {
        triggerSession(adRequestType)
    }

    override fun onRewardVideoFailed(adRequestType: MonetizationBaseActivity.AdRequestType) {
        triggerSession(adRequestType)
    }

    private fun triggerSession(adRequestType: MonetizationBaseActivity.AdRequestType? = null) {
        mBinding?.progressView?.hide()
        if (adRequestType == MonetizationBaseActivity.AdRequestType.TRIGGER_START_SESSION) {
            mMonetizationBase?.updateAdsRequestType(MonetizationBaseActivity.AdRequestType.TRIGGER_GENERAL)
            lifecycleScope.launch {
                mBinding?.progressView?.hide()
                mCyclicAdsBase?.checkAndStartSession()
            }
        }
    }

    override fun onShowDownloadFeature() {
        getToolBarBinding()?.showDownloadFeature = mTooBarBase?.shouldShowDownloadFeature
    }

    override fun onShowAdDebuggerOption() {
        getToolBarBinding()?.showAdDebuggerOption = mTooBarBase?.shouldShowDebuggerOption
    }
}