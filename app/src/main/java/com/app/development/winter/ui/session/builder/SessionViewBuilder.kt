package com.app.development.winter.ui.session.builder

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import com.app.development.winter.R
import com.app.development.winter.application.Controller.Companion.navigationBarHeight
import com.app.development.winter.application.Controller.Companion.statusBarHeight
import com.app.development.winter.databinding.ActivitySessionBinding
import com.app.development.winter.databinding.DialogEndSessionBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.extension.fromHtml
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.show
import com.app.development.winter.shared.extension.toPx
import com.app.development.winter.shared.views.stepprogress.StageStepBar
import com.app.development.winter.ui.session.gameView.GameListener
import com.app.development.winter.ui.session.gameView.GameView
import com.app.development.winter.ui.session.gameView.gameobjects.GameStatus
import com.app.development.winter.ui.session.model.SessionData
import com.app.development.winter.ui.session.service.AdvanceBaseFloatingViewService.Companion.isNavigationBarActionTriggered
import com.app.development.winter.ui.session.state.SessionUiState
import com.app.development.winter.utility.BindingAdaptersUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class SessionViewBuilder : View.OnClickListener, GameListener {
    private var syncTime: Long = 0
    private var mContext: Context? = null
    private var mBinding: ActivitySessionBinding? = null
    private var mListeners: SessionActionListeners? = null
    private var lifecycleScope: LifecycleCoroutineScope? = null
    private var makeTransparentCounter = 0
    private var mSessionData: SessionData? = null
    private var mTimerJob: Job? = null
    private var hasSessionOnGoing = true
    private var mGameView: GameView? = null

    fun initView(
        context: Context?,
        lifecycleScope: LifecycleCoroutineScope,
        binding: ActivitySessionBinding?,
    ): SessionViewBuilder {
        this.mContext = context
        this.mBinding = binding
        this.lifecycleScope = lifecycleScope
        return this
    }

    private fun setSessionInfo(sessionData: SessionData?) {
        sessionData?.let { info ->
            mSessionData = info
            mBinding?.sessionData = mSessionData
            manageTimer()
            setLevelProgress(info)
        }
    }

    private fun setLevelProgress(data: SessionData?) {
        data?.let { info ->
            val leftTime = 20 - (info.currentEarningsData.earningPercentForACoin.div(5))
            mBinding?.stageStepBar?.setCurrentState(StageStepBar.State(leftTime, 0))
        }
    }

    fun getSyncTime(): String {
        return syncTime.toString()
    }

    fun getSessionData(): SessionData? {
        return mSessionData
    }

    fun build(): SessionViewBuilder {
        init()
        return this
    }

    private fun init() {
        mBinding?.layoutDialog?.root?.invisible()
        mBinding?.layoutToolbar?.clickListener = this
        mBinding?.clickListener = this
        makeOverlayTransparent()
        if (LocalDataHelper.isOnProduction().not()) {
            mBinding?.lottieHomeAnimation?.hide()
            mBinding?.lottieHomeAnimation?.pauseAnimation()
            mBinding?.layoutContent?.background =
                ContextCompat.getDrawable(mContext!!, R.drawable.game_background)
        }
        if (statusBarHeight > 0) {
            mBinding?.guidelineTop?.setGuidelineBegin(statusBarHeight)
        }
        if (navigationBarHeight > 0) {
            mBinding?.guidelineBottom?.setGuidelineEnd(navigationBarHeight)
        }

        val dialogPadding = mBinding?.layoutDialog?.layoutParent?.paddingStart ?: 0
        mBinding?.layoutDialog?.layoutParent?.setPaddingRelative(
            dialogPadding, statusBarHeight, dialogPadding, navigationBarHeight
        )
    }

    fun renderSessionUiState(state: SessionUiState) {
        when (state.loadingState.first) {
            AdvanceBaseViewModel.LoadingType.EMPTY_SATE -> {

            }

            AdvanceBaseViewModel.LoadingType.GET_ONGOING_SESSION, AdvanceBaseViewModel.LoadingType.START_NEW_SESSION -> {
                when (state.loadingState.second) {
                    AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                        showLoadingView(true)
                    }

                    AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                        showLoadingView(false)
                        readyToStart()
                        state.activeSession?.let { info ->
                            if (!info.sessionId.isNullOrEmpty()) {
                                setSessionInfo(state.activeSession)
                            }
                        }
                    }

                    else -> {
                        showLoadingView(false)
                        hideProgressBar()
                        showAlertDialog(
                            icon = R.drawable.app_logo,
                            title = mContext?.getString(R.string.error),
                            message = state.errorMessage,
                            positiveButtonText = mContext?.getString(R.string.ok)
                        )
                    }
                }
            }

            AdvanceBaseViewModel.LoadingType.SYNC_FOCUS_TIME -> {
                when (state.loadingState.second) {
                    AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                        setSessionInfo(state.syncSession)
                    }

                    else -> {}
                }
            }

            AdvanceBaseViewModel.LoadingType.SESSION_END -> {
                when (state.loadingState.second) {
                    AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                        mBinding?.layoutDialog?.progressView?.show()
                    }

                    AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                        isNavigationBarActionTriggered = false
                        LocalDataHelper.haveActiveSession = false
                        mBinding?.layoutDialog?.progressView?.hide()
                        cancelTimer()
                        showRewardDialog()
                    }

                    AdvanceBaseViewModel.LoadingState.ERROR -> {
                        cancelTimer()
                        mListeners?.onSessionAction(SessionActionListeners.SessionAction.STOP_CYCLE)
                    }

                    else -> {
                        mBinding?.layoutDialog?.progressView?.hide()
                    }
                }
            }

            else -> {
                if (hasSessionOnGoing.not()) return

                showLoadingView(false)
                hideProgressBar()
                showAlertDialog(
                    icon = R.drawable.app_logo,
                    title = mContext?.getString(R.string.error),
                    message = state.errorMessage,
                    positiveButtonText = mContext?.getString(R.string.ok)
                )
            }
        }
    }

    private fun showLoadingView(isShow: Boolean) {
        if (isShow) {
            mBinding?.lottieAnimationView?.resumeAnimation()
            mBinding?.layoutAppLoaderView?.show()
        } else {
            mBinding?.lottieAnimationView?.pauseAnimation()
            mBinding?.layoutAppLoaderView?.invisible()
        }
    }

    private fun readyToStart() {
        if (LocalDataHelper.isOnProduction()) {
            mBinding?.mainContainer?.isVisible = true
            mBinding?.gameContainer?.isVisible = false
        } else {
            mBinding?.gameContainer?.isVisible = true
            mBinding?.mainContainer?.isVisible = false

            mGameView = mContext?.let { GameView(it, null) }
            mGameView?.setGameConfig {
                showOverlayBox = false
                showLineSeparator = true
                lineSeparatorStartColor = Color.parseColor("#838D9C")
                lineSeparatorEndColor = Color.parseColor("#101317")
                initialPlayerLives = 1
                coinsNeededForSpeedIncrease = 8
                enableSmoothMovement = true
                enableLaneChangeRotation = false
                isSoundMute = false
                initialGameSpeed = 10f
                incrementFactor = 0.15f
            }
            mBinding?.gameView?.addView(mGameView)
            mGameView?.setGameListener(this)
            mGameView?.startGame()
        }
    }

    override fun onGameStateChange(
        currentState: GameStatus,
        totalScore: Int,
        hasUserQuit: Boolean
    ) {
        if (currentState == GameStatus.ENDED && LocalDataHelper.isOnProduction().not()) {
            lifecycleScope?.launch {
                showLoadingView(true)
                mListeners?.onSessionAction(
                    SessionActionListeners.SessionAction.SESSION_END,
                    syncTime
                )
                cancelTimer()
            }
        }
    }

    private fun manageTimer() {
        mTimerJob?.cancel()
        mTimerJob = lifecycleScope?.launch {
            mSessionData?.let { info ->
                info.totalSpentSessionTime += 1L
                syncTime = syncTime.plus(1)
                if (syncTime >= (if (LocalDataHelper.isOnProduction()) 30 else 15)) {
                    mListeners?.onSessionAction(
                        SessionActionListeners.SessionAction.SYNC_TIME, syncTime
                    )
                    syncTime = 0
                }
                //info.notifyChange()
                delay(1000)
                manageTimer()
            }
        }
    }

    private fun cancelTimer() {
        lifecycleScope?.launch {
            mTimerJob?.cancel()
            delay(500)
            syncTime = 0
        }
    }


    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding?.btnMoveBack -> {
                mGameView?.moveCharacterToRight()
            }

            mBinding?.btnMoveForward -> {
                mGameView?.moveCharacterToLeft()
            }

            mBinding?.layoutToolbar?.btnClose -> {
                showEndSessionDialog()
            }

            mBinding?.btnMakeTransparent -> {
                makeOverlayTransparent()
            }

            mBinding?.btnSessionInfo -> {
                mBinding?.apply {
                    val isProfileShow = layoutSessionInfo.isVisible
                    val animateRotation = 180f
                    animateRotation(btnSessionInfo, animateRotation)
                    layoutSessionInfo.isVisible = isProfileShow == false
                    val param =
                        constGameSessionDetails.layoutParams as? ConstraintLayout.LayoutParams
                    if (layoutSessionInfo.isVisible) {
                        param?.marginStart = mContext?.let { (0).toPx(context = it) } ?: -10
                    } else {
                        param?.marginStart = mContext?.let { (-15).toPx(context = it) } ?: -10
                    }
                    constGameSessionDetails.layoutParams = param
                }
            }

        }
    }

    private fun animateRotation(view: View, rotationDegree: Float) {
        val rotationAnimator =
            ObjectAnimator.ofFloat(view, "rotation", view.rotation, view.rotation + rotationDegree)
        rotationAnimator.duration = 500
        rotationAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                view.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator) {
                view.isEnabled = true
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        rotationAnimator.start()
    }

    private fun makeOverlayTransparent() {
        if (LocalDataHelper.getUserDetail()?.canUseTransparentOverlay == true && LocalDataHelper.isOnProduction()) {
            mBinding?.btnMakeTransparent?.show()
            if (makeTransparentCounter > 2) {
                makeTransparentCounter = 1
                LocalDataHelper.setOverlayTransparent(!LocalDataHelper.isOverlayTransparent())
            } else {
                makeTransparentCounter += 1
            }

            if (LocalDataHelper.isOverlayTransparent()) {
                mBinding?.root?.alpha = 0.80f
            } else {
                mBinding?.root?.alpha = 1.0f
            }
        } else {
            LocalDataHelper.setOverlayTransparent(false)
            mBinding?.root?.alpha = 1.0f
        }
    }

    fun setListener(
        listener: SessionActionListeners?
    ): SessionViewBuilder {
        this.mListeners = listener
        return this
    }

    fun showDailyLimitReachedDialog() {
        showAlertDialog(icon = R.drawable.app_logo,
            title = mContext?.getString(R.string.daily_earning_limit_reached),
            message = mContext?.getString(R.string.you_have_reached_the_daily_earning_limit_in_this_app),
            positiveButtonText = mContext?.getString(R.string.continue_),
            isShowClose = false,
            onCloseCallback = {
                mListeners?.onSessionAction(
                    SessionActionListeners.SessionAction.SESSION_END,
                    syncTime
                )
            })
    }

    private fun showRewardDialog() {
        hasSessionOnGoing = false
        cancelTimer()
        showAlertDialog(icon = R.drawable.icon_reward,
            title = mContext?.getString(com.givvy.invitefriends.R.string.congrats),
            earningData = mSessionData?.currentEarningsData?.getBalanceWithCurrency(),
            subTitle = mContext?.getString(R.string.session_ended_title),
            message = mContext?.getString(R.string.session_ended_message),
            positiveButtonText = mContext?.getString(R.string.reward),
            onPositiveCallback = {
                mListeners?.onSessionAction(SessionActionListeners.SessionAction.STOP_CYCLE)
            })
    }

    fun showEndSessionDialog() {
        mGameView?.pauseGame()
        showAlertDialog(
            icon = R.drawable.icon_end_session,
            title = mContext?.getString(R.string.ending_session),
            subTitle = mContext?.getString(R.string.you_are_so_close),
            message = mContext?.getString(R.string.session_end_message),
            positiveButtonText = mContext?.getString(R.string.continue_),
            isShowClose = true,
            isCloseDialog = false,
            onPositiveCallback = {
                mGameView?.clearGameView()
                cancelTimer()
                mListeners?.onSessionAction(
                    SessionActionListeners.SessionAction.SESSION_END,
                    syncTime
                )
            }, onCloseCallback = {
                isNavigationBarActionTriggered = false
                mGameView?.resumeGame()
            })
        hideEndDialogInCaseOfNavigationBarAction()
    }

    private fun hideEndDialogInCaseOfNavigationBarAction() {
        if (isNavigationBarActionTriggered) {
            lifecycleScope?.launch {
                delay(5000)
                if (isNavigationBarActionTriggered) {
                    isNavigationBarActionTriggered = false
                    hideDialog()
                }
            }
        }
    }

    private fun showAlertDialog(
        icon: Int? = R.drawable.app_logo,
        title: String? = "",
        subTitle: String? = "",
        message: String? = "",
        earningData: String? = "",
        positiveButtonText: String? = "",
        isShowClose: Boolean = false,
        isCloseDialog: Boolean = true,
        onPositiveCallback: (() -> Unit)? = null,
        onCloseCallback: (() -> Unit)? = null,
    ): DialogEndSessionBinding? {
        mBinding?.layoutDialog?.root?.show()
        mBinding?.layoutDialog?.let { dialog ->
            BindingAdaptersUtil.setBouncingEffect(dialog.imageViewIcon, true)
            dialog.imageViewIcon.show()
            dialog.layoutToolbar.showClose = isShowClose
            dialog.layoutToolbar.appTitle = "\n".plus(title)
            if (earningData.isNullOrEmpty()) dialog.txtEarning.hide() else dialog.txtEarning.show()
            if (message.isNullOrEmpty()) dialog.txtMessage.hide() else dialog.txtMessage.show()
            if (subTitle.isNullOrEmpty()) dialog.txtSubTitle.hide() else dialog.txtSubTitle.show()
            if (icon != null) dialog.imageViewIcon.show() else dialog.imageViewIcon.hide()

            dialog.txtSubTitle.text = subTitle
            dialog.txtMessage.text = message.fromHtml()
            dialog.txtEarning.text = earningData
            dialog.imageViewIcon.setImageResource(icon ?: 0)
            dialog.buttonPositive.text = positiveButtonText
            dialog.buttonPositive.setOnClickListener {
                it.hapticFeedbackEnabled()
                if (isCloseDialog) {
                    hideDialog()
                }
                onPositiveCallback?.invoke()
            }

            dialog.layoutToolbar.btnClose.setOnClickListener {
                it.hapticFeedbackEnabled()
                hideDialog()
                onCloseCallback?.invoke()
            }
        }
        return mBinding?.layoutDialog
    }

    private fun hideDialog() {
        mBinding?.layoutDialog?.root?.invisible()
        lifecycleScope?.launch {
            delay(500)
            mBinding?.layoutDialog?.imageViewIcon?.animate()?.cancel()
            mBinding?.layoutDialog?.imageViewIcon?.clearAnimation()
        }
    }

    private fun showProgressBar() {
    }

    private fun hideProgressBar() {
    }
}