package com.app.development.winter.ui.session.service

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Binder
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.application.Controller
import com.app.development.winter.databinding.LayoutUserStatisticsBinding
import com.app.development.winter.ui.session.builder.SessionActionListeners
import com.app.development.winter.ui.session.builder.UserStatisticsViewBuilder
import com.app.development.winter.ui.session.event.SessionEvent
import com.app.development.winter.ui.session.state.SessionUiState
import com.app.development.winter.ui.session.viewmodel.SessionConfigViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserStatisticsOverlayService :
    AdvanceBaseFloatingViewService<LayoutUserStatisticsBinding, SessionEvent, SessionUiState, SessionConfigViewModel>(
        LayoutUserStatisticsBinding::inflate, SessionConfigViewModel::class.java
    ), SessionActionListeners {
    inner class LocalBinder : Binder() {
        fun getService(): UserStatisticsOverlayService = this@UserStatisticsOverlayService
    }

    var mUserStatisticsViewBuilder: UserStatisticsViewBuilder? = null
    private val binder = LocalBinder()
    override fun initDATA() {
        mUserStatisticsViewBuilder =
            UserStatisticsViewBuilder().setContext(this, lifecycleScope, mBinding).setListener(this)
                .build()
    }

    override fun initUI() {
        mViewModel.getSessionUiState().handleEvent(SessionEvent.RequestUserStatistics("0"))
        setFloatingTouchListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setFloatingTouchListener() {
        mWindowParams?.let { windowParam ->
            mBinding?.root?.setOnTouchListener(object : OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                private var initialTouchTime = System.currentTimeMillis()
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = windowParam.x
                            initialY = windowParam.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            initialTouchTime = System.currentTimeMillis()

                        }

                        MotionEvent.ACTION_UP -> {
                            val x = initialX + (event.rawX - initialTouchX).toInt()
                            val y = initialY + (event.rawY - initialTouchY).toInt()
                            checkAndUpdate(x, y, v.resources)
                        }

                        MotionEvent.ACTION_MOVE -> {
                            windowParam.x = initialX + (event.rawX - initialTouchX).toInt()
                            windowParam.y = initialY + (event.rawY - initialTouchY).toInt()
                            mWindowManager?.updateViewLayout(mBinding?.root, windowParam)
                        }
                    }
                    return false
                }
            })
        }
    }

    private fun checkAndUpdate(x: Int, y: Int, resources: Resources) {
        mWindowParams?.let { windowParam ->
            val deviceWidth = resources.displayMetrics.widthPixels
            val deviceHeight = resources.displayMetrics.heightPixels

            val viewWidth = mBinding?.root?.width ?: 0
            val viewHeight = mBinding?.root?.height ?: 0

            if (x > (deviceWidth / 2).minus(viewWidth / 2)) {
                windowParam.x = deviceWidth.minus(viewWidth)
            } else {
                windowParam.x = 0
            }
            if (y < Controller.statusBarHeight) {
                windowParam.y = Controller.statusBarHeight
            } else if ((y.plus(viewHeight / 2)) > deviceHeight) {
                windowParam.y = deviceHeight.minus(viewHeight)
            } else {
                windowParam.y = y
            }
            mWindowManager?.updateViewLayout(mBinding?.root, windowParam)
        }
    }

    override fun onBackPressed() {
        onSessionAction(SessionActionListeners.SessionAction.SESSION_END)
    }

    override fun render(state: SessionUiState) {
        mUserStatisticsViewBuilder?.renderUiState(state)
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
                endCycle()
            }

            SessionActionListeners.SessionAction.SYNC_TIME -> {
                mViewModel.getSessionUiState()
                    .handleEvent(SessionEvent.RequestUserStatistics(value.toString()))
            }

            else -> {

            }
        }
    }
}
