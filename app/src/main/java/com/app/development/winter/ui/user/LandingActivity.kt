package com.app.development.winter.ui.user

import android.view.Gravity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.application.Controller.Companion.isAutoClickerInstalled
import com.app.development.winter.databinding.ActivityLandingBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.manager.InstallAppCheckManager
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.activitybase.NavigationBaseActivity
import com.app.development.winter.shared.extension.forceRestart
import com.app.development.winter.shared.extension.handleVisualOverlaps
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.makeColor
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import com.app.development.winter.utility.Util
import com.app.development.winter.utility.ViewUtil.openAppInPlayStore
import com.givvy.invitefriends.builder.ReferralLibManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LandingActivity :
    NavigationBaseActivity<ActivityLandingBinding, UserEvents, UserState, UserViewModel>(
        ActivityLandingBinding::inflate, UserViewModel::class.java
    ) {

    override fun onStart() {
        super.onStart()
        ReferralLibManager.getReferralCodeFromLink(data = intent)
    }

    override fun initDATA() {
        lifecycleScope.launch {
            delay(1000)
            mViewModel?.getUserUiState()?.handleEvent(UserEvents.RequestAppConfig)
        }
    }

    override fun initUI() {
        WindowCompat.setDecorFitsSystemWindows(window,false)
        LocalDataHelper.clearUserPreference()
        InstallAppCheckManager.getAllInstalledPackage { isPackageInstalled ->
            isAutoClickerInstalled = isPackageInstalled
        }

        mBinding?.txtSubTitle?.makeColor(
            getString(R.string.wild_companions), color = R.color.colorTertiary
        )
    }

    private fun startApp() {
        lifecycleScope.launch {
            delay(100)
            sendFcmTokenToServer()
            val user = LocalDataHelper.getUserDetail()
            /*if ((user?.needToMakeSocialLogin == true || LocalDataHelper.getUserDetail()?.externalUserID.isNullOrEmpty()) *//*&& LocalDataHelper.isOnProduction()*//*) {
                openLoginActivity()
            } else */if (user?.country.isNullOrEmpty()) {
            openWelComeActivity()
        } else {
            openHomeActivity()
        }
        }
    }

    override fun render(state: UserState) {
        lifecycleScope.launch {
            when (state.loadingState.second) {
                AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.VERSION_CHECK -> {
                            showProgress(true)
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.REGISTER_DEVICE -> {
                            if (LocalDataHelper.isOnProduction()) {
                                showProgress(false)
                                startApp()
                            } else {
                                ReferralLibManager.run {
                                    setReferralUser(LocalDataHelper.getUserDetail()?.id,
                                        deviceID = Util.getUniquePsuedoID(),
                                        onReferralInitProcessingComplete = { value, isSuccess ->
                                            if (isSuccess == true && value.isNullOrEmpty()) {
                                                showProgress(false)
                                                startApp()
                                            }
                                        },
                                        onReferralLinkEstablished = { value, isSuccess ->
                                            LocalDataHelper.setFirstLogin(true)
                                            mViewModel?.getUserUiState()
                                                ?.handleEvent(UserEvents.RequestAppConfig)
                                        },
                                        onRentAppUserMigrated = { s, data, success ->
                                            if (success == true) {
                                                data?.let {
                                                    if (it.cycleAdsForRentApp) {
                                                        userIdRentApp = null
                                                        LocalDataHelper.getUserDetail()?.apply {
                                                            id = it.id
                                                            cycleAdsForRentApp =
                                                                it.cycleAdsForRentApp
                                                        }
                                                        recreate()
                                                    }
                                                }
                                            }
                                        })
                                }
                            }
                        }

                        ApiEndpoints.VERSION_CHECK -> {
                            showForceUpdateDialog()
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.ERROR -> {
                    if (LocalDataHelper.haveActiveSession) {
                        forceRestart()
                    } else {
                        showAlertDialog(title = getString(R.string.error),
                            message = state.errorMessage,
                            imageRes = R.drawable.app_logo,
                            positiveRes = R.string.ok,
                            positiveClick = {
                                forceRestart()
                            },
                            cancelable = false,
                            onCancel = {
                                finishAffinity()
                            },
                            onDismiss = { })
                    }

                }

                else -> {

                }
            }
        }
    }

    private fun showProgress(isShow: Boolean) {
        if (isShow) {
            mBinding?.progressView?.show()
        } else {
            mBinding?.progressView?.invisible()
        }
    }

    private fun sendFcmTokenToServer() {/*getFcmToken { fcmToken, isSuccess ->
            if (isSuccess) {
                mViewModel?.getUserUiState()?.handleEvent(UserEvents.SendFcmToken(fcmToken))
            }
        }*/
    }

    private fun showForceUpdateDialog() {
        LocalDataHelper.getAppConfig()?.let { config ->
            when {
                config.isForceUpdate -> {
                    showAlertDialog(title = getString(R.string.update_title),
                        message = getString(R.string.update_force_update_message),
                        cancelable = false,
                        positiveRes = R.string.update,
                        positiveClick = {
                            config.androidUrl?.openAppInPlayStore(this@LandingActivity)
                            this@LandingActivity.finishAffinity()
                        },
                        onCancel = {},
                        onDismiss = {})
                }

                config.hasUpdate -> {
                    showAlertDialog(title = getString(R.string.update_title),
                        message = getString(R.string.update_force_update_message),
                        cancelable = false,
                        positiveRes = R.string.update,
                        showClose = true,
                        positiveClick = {
                            config.androidUrl?.openAppInPlayStore(this@LandingActivity)
                            this@LandingActivity.finishAffinity()
                        },
                        onCancel = {
                            mViewModel?.getUserUiState()?.handleEvent(UserEvents.RegisterDevice)
                        },
                        onDismiss = {})
                }

                else -> {
                    mViewModel?.getUserUiState()?.handleEvent(UserEvents.RegisterDevice)
                }
            }
        }
    }
}