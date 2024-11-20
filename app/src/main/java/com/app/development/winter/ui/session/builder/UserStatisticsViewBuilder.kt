package com.app.development.winter.ui.session.builder

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import com.app.development.winter.databinding.LayoutUserStatisticsBinding
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.show
import com.app.development.winter.shared.model.UserStatistics
import com.app.development.winter.ui.session.state.SessionUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class UserStatisticsViewBuilder : View.OnClickListener {

    private var mContext: Context? = null
    private var mBinding: LayoutUserStatisticsBinding? = null
    private var lifecycleScope: LifecycleCoroutineScope? = null
    private var mListeners: SessionActionListeners? = null

    private var mTimerJob: Job? = null
    var syncTime: Long = 0

    fun setContext(
        context: Context?,
        lifecycleScope: LifecycleCoroutineScope,
        binding: LayoutUserStatisticsBinding?,
    ): UserStatisticsViewBuilder {
        this.mContext = context
        this.mBinding = binding
        this.lifecycleScope = lifecycleScope
        return this
    }

    fun build(): UserStatisticsViewBuilder {
        init()
        return this
    }

    private fun init() {
        mBinding?.clickListener = this
    }

    fun renderUiState(state: SessionUiState) {
        when (state.loadingState.first) {
            AdvanceBaseViewModel.LoadingType.EMPTY_SATE -> {

            }

            AdvanceBaseViewModel.LoadingType.USER_STATE_DATA -> {
                when (state.loadingState.second) {
                    AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                        showLoadingView(true)
                    }

                    AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                        showLoadingView(false)
                        state.userXpStateData?.let { info ->
                            mBinding?.statistics = info
                            manageTimer(mBinding?.statistics)
                        }
                    }

                    else -> {
                        showLoadingView(false)
                    }
                }
            }

            else -> {
                showLoadingView(false)
            }
        }
    }

    private fun manageTimer(statistics: UserStatistics?) {
        mTimerJob?.cancel()
        mTimerJob = lifecycleScope?.launch {
            statistics?.let { info ->
                info.totalSpentSessionTime += 1L
                syncTime = syncTime.plus(1)
                if (syncTime >= 5) {
                    mListeners?.onSessionAction(
                        SessionActionListeners.SessionAction.SYNC_TIME, syncTime
                    )
                    syncTime = 0
                }
                info.notifyChange()
                delay(1000)
                manageTimer(mBinding?.statistics)
            }
        }
    }

    private fun cancelTimer() {
        mTimerJob?.cancel()
    }

    private fun showLoadingView(isShow: Boolean) {
        if (isShow) {
            mBinding?.tvDataSyncing?.show()
            mBinding?.progressBar?.show()
        } else {
            mBinding?.progressBar?.invisible()
            mBinding?.tvDataSyncing?.invisible()
        }
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding?.btnClose -> {
                cancelTimer()
                mListeners?.onSessionAction(SessionActionListeners.SessionAction.SESSION_END)
            }
        }
    }

    fun setListener(
        listener: SessionActionListeners?
    ): UserStatisticsViewBuilder {
        this.mListeners = listener
        return this
    }
}