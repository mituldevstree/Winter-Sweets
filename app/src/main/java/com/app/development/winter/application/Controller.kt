package com.app.development.winter.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.PeriodicWorkRequest
import com.app.development.winter.manager.MemoryReleaseHelper
import com.app.development.winter.manager.OfferWallLibInitializer
import com.app.development.winter.manager.ReferralLibInitializer
import com.app.development.winter.manager.WalletLibManager
import com.app.development.winter.shared.extension.isScreenOffWithLockButton
import com.app.development.winter.ui.session.model.FloatingViewState
import com.app.development.winter.ui.session.service.AdvanceBaseFloatingViewService.Companion.isServiceRunning
import com.app.development.winter.ui.session.service.FloatingViewServiceState
import com.app.development.winter.utility.CrashLogsExceptionHandler
import com.app.development.winter.utility.ViewUtil
import com.monetizationlib.data.Monetization
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File

class Controller : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        Thread.setDefaultUncaughtExceptionHandler(CrashLogsExceptionHandler())
        Monetization.onApplicationCreated(this)
        registerActivityLifecycleCallbacks(this)
        instance = this
        ReferralLibInitializer.init()
        OfferWallLibInitializer.init()
        WalletLibManager.initialize()
        observeApplicationState()
        // Lets clear the cache and files only once per session. There were some issues with the ads and missing(deleted) resources
        GlobalScope.launch {
            MemoryReleaseHelper.processAndDeleteFromAlFolder(filesDir)
            MemoryReleaseHelper.clearAppCache(this@Controller)
        }
    }

    companion object {
        lateinit var instance: Controller
        var needToShowRatingFlow = true
        var appInForeground: Boolean = false
        var isCyclicAdsClicked: Boolean = false
        var isAutoClickerInstalled: Boolean = false
        var foregroundActivity: Activity? = null
        var windowHeight: Int = 0
        var statusBarHeight: Int = 0
        var navigationBarHeight: Int = 0
        var wasMonetizationInitialized: Boolean = false
        var notificationWorkerInstance: PeriodicWorkRequest? = null
        var foregroundView: View? = null
        val floatingViewState = MutableSharedFlow<FloatingViewServiceState?>(
            replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        fun setSystemBarHeight(activity: Activity) {
            if (windowHeight == 0) {
                windowHeight = ViewUtil.getDeviceHeight(activity)
            }
            if (statusBarHeight == 0) {
                statusBarHeight = ViewUtil.getStatusBarHeight(activity)
            }
            if (navigationBarHeight == 0) {
                navigationBarHeight = ViewUtil.getNavigationBarHeight(activity)
            }
        }
    }

    private fun observeApplicationState() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
    }

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        ViewUtil.printLog("CheckCycleAds", "Foreground:${event.name}")
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                appInForeground = true
                isCyclicAdsClicked = false
            }

            Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                if (appInForeground) {
                    appInForeground = false
                    if (isServiceRunning.not()) return@LifecycleEventObserver
                    if (!isServiceRunning || isCyclicAdsClicked) return@LifecycleEventObserver
                    //if (Settings.canDrawOverlays(this)) applicationContext.forceRestart()

                    GlobalScope.launch {
                        if (!isScreenOffWithLockButton()) {
                            floatingViewState.emit(FloatingViewServiceState(floatingViewState = FloatingViewState.ACTION_NAVIGATION_BAR))
                        }
                    }
                }
            }

            else -> {}
        }
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        setSystemBarHeight(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        ViewUtil.printLog("CheckCycleAds", "BaseApplication->${activity.localClassName}")
        foregroundActivity = activity
        foregroundView = activity.window.decorView
    }

    override fun onActivityResumed(p0: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {
        foregroundActivity = activity
        foregroundView = activity.window.decorView
    }

    override fun onActivityStopped(p0: Activity) {

    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onActivityDestroyed(p0: Activity) {

    }
}