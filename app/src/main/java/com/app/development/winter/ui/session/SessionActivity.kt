package com.app.development.winter.ui.session

import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.ActivitySessionBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.shared.base.activitybase.ToolBarBaseActivity
import com.app.development.winter.ui.session.builder.SessionActionListeners
import com.app.development.winter.ui.session.builder.SessionViewBuilder
import com.app.development.winter.ui.session.event.SessionEvent
import com.app.development.winter.ui.session.state.SessionUiState
import com.app.development.winter.ui.session.viewmodel.SessionConfigViewModel
import kotlinx.coroutines.launch

class SessionActivity :
    ToolBarBaseActivity<ActivitySessionBinding, SessionEvent, SessionUiState, SessionConfigViewModel>(
        ActivitySessionBinding::inflate, SessionConfigViewModel::class.java
    ), SessionActionListeners {
    private var mSessionBuilder: SessionViewBuilder? = null

    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return null
    }

    override fun initDATA() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this@SessionActivity,
                R.drawable.game_background
            )
        )
        mSessionBuilder = SessionViewBuilder().initView(
            context = this, lifecycleScope = lifecycleScope, binding = mBinding
        ).setListener(this).build()
        mViewModel?.getSessionUiState()?.handleEvent(SessionEvent.RequestOngoingSessionInfo(true))
    }

    override fun initUI() {
        captureBackPressState()
    }

    override fun render(state: SessionUiState) {
        mSessionBuilder?.renderSessionUiState(state)
    }

    override fun onSessionAction(action: SessionActionListeners.SessionAction?, value: Any?) {
        when (action) {

            SessionActionListeners.SessionAction.SESSION_END -> {
                lifecycleScope.launch {
                    mViewModel?.getSessionUiState()
                        ?.handleEvent(SessionEvent.RequestEndSession(value.toString()))
                }
            }

            SessionActionListeners.SessionAction.STOP_CYCLE -> {
                finish()
            }

            SessionActionListeners.SessionAction.SYNC_TIME -> {
                mViewModel?.getSessionUiState()
                    ?.handleEvent(SessionEvent.RequestSyncFocusTime(value.toString()))
            }

            else -> {

            }
        }
    }

    override fun onSuperBackPressed() {
        mSessionBuilder?.showEndSessionDialog()
    }
}