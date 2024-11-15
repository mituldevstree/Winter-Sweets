package com.app.development.winter.ui.session.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.databinding.ActivitySessionBinding
import com.app.development.winter.ui.session.builder.SessionActionListeners
import com.app.development.winter.ui.session.builder.SessionViewBuilder
import com.app.development.winter.ui.session.event.SessionEvent
import com.app.development.winter.ui.session.state.SessionUiState
import com.app.development.winter.ui.session.viewmodel.SessionConfigViewModel
import kotlinx.coroutines.launch

class SessionOverlayService :
    AdvanceBaseFloatingViewService<ActivitySessionBinding, SessionEvent, SessionUiState, SessionConfigViewModel>(
        ActivitySessionBinding::inflate, SessionConfigViewModel::class.java
    ), SessionActionListeners {
    inner class LocalBinder : Binder() {
        fun getService(): SessionOverlayService = this@SessionOverlayService
    }

    private val binder = LocalBinder()
    private var mSessionBuilder: SessionViewBuilder? = null
    override fun initDATA() {
        mSessionBuilder =
            SessionViewBuilder().initView(this, lifecycleScope, mBinding).setListener(this).build()
        mViewModel.getSessionUiState()
            .handleEvent(SessionEvent.RequestOngoingSessionInfo(true))
    }

    override fun initUI() {
        startCycle()
    }

    override fun onBackPressed() {

    }

    override fun showEndSession() {
        mSessionBuilder?.showEndSessionDialog()
    }

    override fun dailyLimitReached() {
        mSessionBuilder?.showDailyLimitReachedDialog()
    }

    override fun syncDataBeforeForceRestart() {
        lifecycleScope.launch {
            mViewModel.getSessionUiState()
                .handleEvent(SessionEvent.RequestSyncFocusTime(mSessionBuilder?.getSyncTime()))
        }
    }

    override fun render(state: SessionUiState) {
        mSessionBuilder?.renderSessionUiState(state)
    }

    override fun onBind(intent: Intent): IBinder {
        stopForeground(STOP_FOREGROUND_REMOVE) // <- remove notification
        super.onBind(intent)
        return binder
    }

    override fun onRebind(intent: Intent?) {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        val notification = createNotification()
        triggerForegroundService(notification)
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return super.onStartCommand(null, flags, startId)
        val notification = createNotification()
        triggerForegroundService(notification)
        return when (intent.action) {
            ACTION_SHOW_WIDGET -> {
                if (isServiceRunning.not()) setUpWindowManager()
                START_STICKY_COMPATIBILITY
            }

            else -> {
                stopService()
                START_STICKY_COMPATIBILITY
            }
        }
    }

    override fun onSessionAction(action: SessionActionListeners.SessionAction?, value: Any?) {
        when (action) {
            SessionActionListeners.SessionAction.SESSION_END -> {
                lifecycleScope.launch {
                    mViewModel.getSessionUiState()
                        .handleEvent(SessionEvent.RequestEndSession(value.toString()))
                }
            }

            SessionActionListeners.SessionAction.SYNC_TIME -> {
                mViewModel.getSessionUiState()
                    .handleEvent(SessionEvent.RequestSyncFocusTime(value.toString()))
            }

            SessionActionListeners.SessionAction.STOP_CYCLE -> {
                endCycle()
            }

            else -> {

            }
        }
    }
}
