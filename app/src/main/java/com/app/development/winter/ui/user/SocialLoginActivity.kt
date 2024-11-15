package com.app.development.winter.ui.user

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.ActivitySocialLoginBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.activitybase.NavigationBaseActivity
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.makeLinksWithFont
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import kotlinx.coroutines.launch


class SocialLoginActivity :
    NavigationBaseActivity<ActivitySocialLoginBinding, UserEvents, UserState, UserViewModel>(
        ActivitySocialLoginBinding::inflate, UserViewModel::class.java
    ), View.OnClickListener {
    private var mCounterSkipButton = 0

    override fun initDATA() {
        LocalDataHelper.logout()
    }

    override fun initUI() {
        mBinding?.clickListener = this
        setTermsConditionClickable()
    }

    private fun setTermsConditionClickable() {
        mBinding?.txtTermsCondition?.makeLinksWithFont(
            fontRes = R.font.montserrat_extra_bold,
            color = R.color.colorPrimary,
            links = arrayOf(Pair(getString(R.string.terms_and_conditions), View.OnClickListener {
                openTermsConditionLink()
            }), Pair(getString(R.string.privacy_policy), View.OnClickListener {
                openPrivacyPolicyLink()
            }))
        )
    }

    override fun render(state: UserState) {
        lifecycleScope.launch {
            when (state.loadingState.second) {
                AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.SOCIAL_LOGIN, ApiEndpoints.HIDE_LOGIN -> {
                            showProgress(true)
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.SOCIAL_LOGIN, ApiEndpoints.HIDE_LOGIN -> {
                            showProgress(false)
                            LocalDataHelper.isNotificationOn = true
                            LocalDataHelper.setFirstLogin(true)
                            openWelComeActivity()
                        }

                        ApiEndpoints.REGISTER_DEVICE -> {
                            openWelComeActivity()
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.ERROR -> {
                    if (isFinishing || isDestroyed) return@launch
                    runOnUiThread {
                        showProgress(false)
                        showAlertDialog(title = getString(R.string.failed_to_authenticate_user),
                            message = state.errorMessage,
                            imageRes = R.drawable.app_logo,
                            positiveRes = R.string.ok,
                            onCancel = {},
                            onDismiss = {})
                    }
                }

                else -> {

                }
            }
        }
    }

    private fun showProgress(isShow: Boolean) {
        runOnUiThread {
            if (isShow) {
                mBinding?.progressView?.show()
            } else {
                mBinding?.progressView?.invisible()
            }
            mBinding?.btnLogin?.isEnabled = !isShow
        }
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding?.btnLogin -> {
                showProgress(true)
            }

            mBinding?.btnSkip -> {
                if (mCounterSkipButton > 1) {
                    mViewModel?.getUserUiState()?.handleEvent(UserEvents.HiddenLogin)
                } else {
                    mCounterSkipButton++
                }
            }
        }
    }

}