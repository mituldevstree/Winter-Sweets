package com.app.development.winter.shared.base.viewbase

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.app.development.winter.BuildConfig
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.databinding.LayoutLoadingViewBinding
import com.app.development.winter.localcache.LocaleHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.shared.callback.IViewRenderer
import com.app.development.winter.shared.extension.hasOverlayPermission
import com.app.development.winter.shared.extension.hasPostNotificationPermissionGranted
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.isVisible
import com.app.development.winter.shared.extension.openAppSetting
import com.app.development.winter.shared.extension.show
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.shared.extension.toast
import com.app.development.winter.shared.extension.viewModelProvider
import com.app.development.winter.ui.notification.worker.NotificationWorker
import com.app.development.winter.utility.LottieAnimationUtil
import com.app.development.winter.utility.Util
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

abstract class AdvancedBaseActivity<VDB : ViewDataBinding, INTENT : ViewIntent, STATE : ViewState, VM : AdvanceBaseViewModel<INTENT, STATE>>(
    val bindingFactory: (LayoutInflater) -> VDB,
    private val modelClass: Class<VM>,
) : AppCompatActivity(), IViewRenderer<STATE>, CoroutineScope by CoroutineScope(Dispatchers.Main) {

    protected val TAG: String = this::class.java.simpleName

    private var _binding: VDB? = null
    protected val mBinding get() = _binding
    var mLoadingViewBinding: LayoutLoadingViewBinding? = null

    private lateinit var viewState: STATE
    protected val mState get() = viewState
    protected var mViewModel: VM? = null
    private fun getViewModelClass(): KClass<VM> =
        ((javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>).kotlin

    private var mSnackBar: Snackbar? = null
    private var mOrientation: Int = Configuration.ORIENTATION_PORTRAIT
    var mActivityLauncher: com.app.development.winter.shared.base.BetterActivityResult<Intent, ActivityResult>? =
        null

    open fun toggleOfferBubbleView(isShow: Boolean? = false) {}
    open fun onUserInfoUpdate() {}
    open fun changeBottomNavItem(itemId: Int) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingFactory(layoutInflater)
        setContentView(mBinding?.root)
        mOrientation = resources.configuration.orientation
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        setLoadingView(view)
        initObjects()
        initUI()
        initDATA()
        setVariables()
        lifecycleScope.launch {
            mViewModel?._state?.collectLatest {
                render(it)
            }
        }
    }

    private fun initObjects() {
        mActivityLauncher =
            com.app.development.winter.shared.base.BetterActivityResult.registerActivityForResult(
                this
            )
        mViewModel =
            viewModelProvider(ViewModelProvider.AndroidViewModelFactory(), modelClass.kotlin)
    }

    private fun setLoadingView(view: View?) {
        runOnUiThread {
            view?.let {
                if (it.findViewById<ConstraintLayout>(R.id.layoutAppLoaderView) != null) {
                    mLoadingViewBinding = LayoutLoadingViewBinding.bind(it)
                }
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        base?.let { super.attachBaseContext(LocaleHelper.onAttach(it)) }
    }

    abstract fun initDATA()

    abstract fun initUI()

    open fun setVariables() {}

    open fun showAppRatingDialog() {}

    fun isLandscapeMode(): Boolean = mOrientation == Configuration.ORIENTATION_LANDSCAPE

    fun isPortraitMode(): Boolean = mOrientation == Configuration.ORIENTATION_PORTRAIT

    fun clearFullScreenActivity() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    }

    fun showSnackBar(view: View?, message: String?) {
        if (view == null) return
        if (message.isNullOrEmpty()) return
        if (mSnackBar != null && mSnackBar!!.isShownOrQueued) mSnackBar?.dismiss()
        mSnackBar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        mSnackBar?.show()
    }

    open fun recreateActivity() {
        startActivity(intent)
        finish()
    }

    fun hideKeyBoard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    fun getContainerView(): View? {
        return mBinding?.root
    }

    private var mBackCounter = 0
    open fun appExitAlert() {
        if (mBackCounter == 0) toast(getString(R.string.app_exit_message))
        mBackCounter++
        Util.executeDelay(2500) { mBackCounter = 0 }
        if (mBackCounter > 1) finish()
    }

    open fun triggerNotificationPermission() {
        intimateUserForNotificationPermission()
    }

    private fun intimateUserForNotificationPermission() {
        when {
            hasPostNotificationPermissionGranted() -> {
                Log.e(TAG, "onCreate: PERMISSION GRANTED")
                registerNotificationWorkManager()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                showNotificationPermissionNotGrantedDialog()
            }

            else -> {
                if (Build.VERSION.SDK_INT >= 33) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    registerNotificationWorkManager()
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                showNotificationPermissionNotGrantedDialog()
            } else {
                lifecycleScope.launch {
                    registerNotificationWorkManager()
                    delay(1000)
                }
            }
        }

    fun requestOverlayPermission(onPermissionGranted: (Boolean) -> Unit) {
        hasOverlayPermission(
            shouldShowAgainOnCancel = false,
            shouldShowAgainOnBackWithoutPermission = true,
            mActivityLauncher,
        ) { isGranted ->
            onPermissionGranted.invoke(isGranted)
        }
    }

    private fun showNotificationPermissionNotGrantedDialog() =
        if (isFinishing.not()) showAlertDialog(titleRes = R.string.required_permission,
            messageRes = R.string.notification_permission_message,
            positiveRes = R.string.ok,
            negativeRes = R.string.cancel,
            positiveClick = {
                openAppSetting()
            },
            negativeClick = {
            }).show()
        else {
        }

    fun showProgressBar(lottieFile: Int = 0, message: String? = getString(R.string.loading)) {
        mLoadingViewBinding?.layoutAppLoaderView?.show()
        mLoadingViewBinding?.lottieAnimationView?.let {
            LottieAnimationUtil.setHomePageAnimation(it, autoPlayLottie = true)
        }
        if (message.isNullOrEmpty().not()) {
            mLoadingViewBinding?.txtErrorMessage?.text = message
            mLoadingViewBinding?.txtErrorMessage?.show()
        } else {
            mLoadingViewBinding?.txtErrorMessage?.hide()
        }
    }

    fun isProgressVisible(): Boolean {
        return mLoadingViewBinding?.layoutAppLoaderView?.isVisible() == true
    }

    fun hideProgressBar() {
        mLoadingViewBinding?.lottieAnimationView?.progress = 0f
        mLoadingViewBinding?.lottieAnimationView?.cancelAnimation()
        mLoadingViewBinding?.layoutAppLoaderView?.hide()
    }

    private val mWorkManager by lazy { WorkManager.getInstance(this) }
    private fun registerNotificationWorkManager() {
        if (Controller.notificationWorkerInstance == null) {
            Controller.notificationWorkerInstance =
                NotificationWorker.buildRequest(lastNotificationId = "")
            // Every WorkRequest object has a auto-generated unique ID. You should save it if you need to get information about the work.
            Controller.notificationWorkerInstance?.let { worker ->

                mWorkManager.enqueueUniquePeriodicWork(
                    BuildConfig.APPLICATION_ID,
                    ExistingPeriodicWorkPolicy.KEEP,
                    worker
                )
                mWorkManager.getWorkInfoByIdLiveData(worker.id).observe(this) { info ->
                    info?.let {
                        when (it.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                val lastNotificationId =
                                    it.outputData.getString(NotificationWorker.KEY_LAST_NOTIFICATION_Id)
                                Log.e("Worker", "Success $lastNotificationId")
                            }

                            WorkInfo.State.FAILED -> {
                                Log.e("Worker", "Failed")
                            }

                            WorkInfo.State.RUNNING -> {
                                Log.e("Worker", "Running")
                            }

                            else -> {
                                Log.e("Worker", "${it.state}")
                            }
                        }
                    }
                }
            }
        }
    }
}