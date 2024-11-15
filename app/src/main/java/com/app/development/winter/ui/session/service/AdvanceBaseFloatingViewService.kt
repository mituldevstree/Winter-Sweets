package com.app.development.winter.ui.session.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.BuildConfig
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.localcache.LocaleHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.shared.callback.IViewRenderer
import com.app.development.winter.shared.extension.justTry
import com.app.development.winter.ui.home.HomeActivity
import com.app.development.winter.ui.session.model.FloatingViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class AdvanceBaseFloatingViewService<VDB : ViewDataBinding, INTENT : ViewIntent, STATE : ViewState, VM : AdvanceBaseViewModel<INTENT, STATE>>(
    val bindingFactory: (LayoutInflater) -> VDB,
    private val modelClass: Class<VM>,
) : LifecycleService(), ViewModelStoreOwner, IViewRenderer<STATE> {

    var mWindowManager: WindowManager? = null
    var mWindowParams: WindowManager.LayoutParams? = null

    private var makeTransparentCounter = 1
    private var _binding: VDB? = null
    protected val mBinding get() = _binding
    override val viewModelStore: ViewModelStore
        get() = ViewModelStore()

    protected val mViewModel: VM by lazy {
        ViewModelProvider(this)[modelClass.kotlin.java]
    }

    abstract fun initDATA()
    abstract fun initUI()
    abstract fun onBackPressed()
    open fun showEndSession() {}
    open fun dailyLimitReached() {}
    open fun syncDataBeforeForceRestart() {}
    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        triggerForegroundService(notification)
        manageUiFlowState()
    }

    override fun attachBaseContext(base: Context?) {
        base?.let { super.attachBaseContext(LocaleHelper.onAttach(it)) }
    }

    private fun manageUiFlowState() {
        lifecycleScope.launch {
            mViewModel._state.collectLatest {
                render(it)
            }
        }
        lifecycleScope.launch {
            Controller.floatingViewState.collectLatest {
                when (it?.floatingViewState) {
                    FloatingViewState.ACTION_NAVIGATION_BAR -> {
                        isNavigationBarActionTriggered = true
                        showEndSession()
                    }

                    FloatingViewState.ON_BACK_PRESSED -> {
                        showEndSession()
                    }

                    FloatingViewState.STOP_SERVICE -> {
                        lifecycleScope.launch {
                            syncDataBeforeForceRestart()
                            delay(1000)
                            endCycle()
                        }
                    }

                    FloatingViewState.STOP_STATS_VIEW -> {
                        onBackPressed()
                    }

                    FloatingViewState.LIMIT_REACHED -> {
                        dailyLimitReached()
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun triggerForegroundService(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this@AdvanceBaseFloatingViewService,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        lifecycleScope.launch {
            delay(1000)
            wasStartForegroundTriggered = true
        }
    }


    fun setUpWindowManager() {
        isServiceRunning = true
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        initWidgetView()
    }

    private fun initWidgetView() {
        _binding = bindingFactory(LayoutInflater.from(this))
        mWindowManager?.addView(mBinding?.root, getWindowParams())
        initUI()
        initDATA()
    }


    private fun getWindowParams(screenOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT): ViewGroup.LayoutParams {
        val params = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        params.format = PixelFormat.TRANSPARENT
        params.gravity = Gravity.START or Gravity.TOP
        params.flags =
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or WindowManager.LayoutParams.FLAG_FULLSCREEN

        params.width = WindowManager.LayoutParams.MATCH_PARENT
        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            params.height =
                if (Controller.windowHeight != 0) Controller.windowHeight else WindowManager.LayoutParams.MATCH_PARENT
        } else {
            params.height = WindowManager.LayoutParams.MATCH_PARENT
        }

        if (Controller.windowHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            params.y = Controller.statusBarHeight
        }

        params.screenOrientation = screenOrientation
        mWindowParams = params
        return params
    }

    fun makeOrientationLandscape() {
        mWindowManager?.updateViewLayout(
            mBinding?.root,
            getWindowParams(screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        )
    }

    fun makeOrientationPortrait() {
        mWindowManager?.updateViewLayout(
            mBinding?.root,
            getWindowParams(screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        )
    }

    fun muteSound(context: Context) {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND
        )
    }

    private fun removeOverlayView() {
        isServiceRunning = false
        isNavigationBarActionTriggered = false
        mBinding?.root?.let { view ->
            if (mWindowManager != null && view.windowToken != null && view.parent != null && view.isShown) {
                justTry { mWindowManager?.removeView(mBinding?.root) }
            }
        }
    }

    /* Create the notification for this service allowing it to be set as foreground service.
   *
   * @param scenarioName the name to de displayed in the notification title
   *
   * @return the newly created notification.
   */
    fun createNotification(): Notification {
        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW
            ).setName(getString(R.string.app_name)).build()
        )
        val message =
            if (LocalDataHelper.isOnProduction()) getString(R.string.overlay_message) else getString(
                R.string.show_user_statistics_message
            )
        val intent = Intent(this, SessionOverlayService::class.java)
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            ).setSmallIcon(R.drawable.ic_push_notification)
            .setCategory(Notification.CATEGORY_SERVICE).setOngoing(true).setLocalOnly(true).build()

    }

    companion object {
        /** The identifier for the foreground notification of this service. */
        const val NOTIFICATION_ID = 205

        /** The channel identifier for the foreground notification of this service. */
        const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID
        var isServiceRunning: Boolean = false
        var isNavigationBarActionTriggered: Boolean = false
        const val ACTION_SHOW_WIDGET = "show_overlay_view"
        var wasStartForegroundTriggered: Boolean = false
    }

    fun startCycle() {
        lifecycleScope.launch {
            Controller.floatingViewState.emit(
                FloatingViewServiceState(
                    floatingViewState = FloatingViewState.CYCLE_ADS_STARTED
                )
            )
        }
    }

    /*
    * Working
    * */
    fun endCycle() {
        lifecycleScope.launch {
            Controller.floatingViewState.emit(
                FloatingViewServiceState(
                    floatingViewState = FloatingViewState.CYCLE_ADS_ENDED
                )
            )
        }
        stopService()
    }

    fun minimizeTheApp() {
        lifecycleScope.launch {
            Controller.floatingViewState.emit(
                FloatingViewServiceState(
                    floatingViewState = FloatingViewState.MINIMIZE_APP
                )
            )
            stopService()
        }

        /** This condition should be used for the close the ads**/
        Controller.foregroundActivity?.let { context ->
            if ((context is HomeActivity).not()) {
                context.finish()
            }
        }
    }

    /**
     * End service lifecycle
     */
    fun stopService() {
        if (wasStartForegroundTriggered) {
            removeOverlayView()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
            wasStartForegroundTriggered = false
            isServiceRunning = false
            stopSelf()
        }
    }
 }