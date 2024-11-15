package com.app.development.winter.ui.user

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.ActivityWelcomeBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.localcache.LocaleHelper
import com.app.development.winter.localcache.StaticData
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.activitybase.ToolBarBaseActivity
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.shared.extension.toast
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.views.carouselpager.CarouselViewPagerAdapter
import com.app.development.winter.shared.views.carouselpager.InfinitePagerAdapter
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import com.app.development.winter.utility.Util
import kotlinx.coroutines.launch

class WelcomeActivity :
    ToolBarBaseActivity<ActivityWelcomeBinding, UserEvents, UserState, UserViewModel>(
        ActivityWelcomeBinding::inflate, UserViewModel::class.java
    ), View.OnClickListener {

    private var mPagerAdapter: CarouselViewPagerAdapter? = null

    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return mBinding?.layoutToolbar
    }

    override fun initDATA() {
        mBinding?.layoutToolbar?.appTitle = getString(R.string.welcome)
        LocalDataHelper.setFirstLogin(true)
    }

    override fun initUI() {
        mBinding?.clickListener = this
        mBinding?.user = LocalDataHelper.getUserDetail()
        mBinding?.country = Util.getCountryFromLocale(this)
        mBinding?.config = LocalDataHelper.getUserConfig()
        mBinding?.language = LocaleHelper.getLanguage()
        setAvatarView()
    }

    override fun render(state: UserState) {
        lifecycleScope.launch {
            when (state.loadingState.second) {
                AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.ADD_FIRST_TIME_PROFILE -> {
                            showProgress(true)
                        }

                        ApiEndpoints.REGISTER_DEVICE -> {
                            showProgress(true)
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.ADD_FIRST_TIME_PROFILE -> {
                            LocalDataHelper.setUserDetail(state.userInfo)
                            if (LocaleHelper.getLanguage().name != mBinding?.language?.name) {
                                LocaleHelper.setLanguage(mBinding?.language)
                                mViewModel?.getUserUiState()?.handleEvent(UserEvents.RegisterDevice)
                            } else {
                                showProgress(false)
                                intentNavigation()
                            }
                        }

                        ApiEndpoints.REGISTER_DEVICE -> {
                            showProgress(false)
                            intentNavigation()
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.ERROR -> {
                    showProgress(false)

                    showAlertDialog(title = getString(R.string.error),
                        message = state.errorMessage,
                        imageRes = R.drawable.app_logo,
                        positiveRes = R.string.ok,
                        onCancel = { },
                        onDismiss = { })
                }

                else -> {

                }
            }
        }
    }

    private fun setAvatarView() {
        val items = StaticData.getAvatarList()
        CarouselViewPagerAdapter(items).apply { mPagerAdapter = this }.also {
            mBinding?.avatarCarousel?.apply {
                offscreenPageLimit = 3
                adapter = InfinitePagerAdapter(it)
                currentItem = items.indexOfFirst { mBinding?.user?.photo == it.id }
            }
        }
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding?.btnSend -> {
                if (isValidate(true)) {
                    val user = LocalDataHelper.getUserDetail()
                    user?.name = mBinding?.edtUserName?.text.toString()
                    user?.country = mBinding?.country
                    user?.photo =
                        mPagerAdapter?.getSelectedPhoto(mBinding?.avatarCarousel?.currentItem ?: 0)
                    mViewModel?.getUserUiState()?.handleEvent(UserEvents.PopulateUserData(user))
                }
            }

            mBinding?.edtCountry -> {
                showCountryDialog(mBinding?.country) {
                    mBinding?.country = it
                    isValidate()
                }
            }

            mBinding?.edtLanguage -> {
                showLanguageDialog(mBinding?.language) {
                    mBinding?.language = it
                }
            }

            mBinding?.btnNextAvatar -> {
                mBinding?.avatarCarousel?.animatePreviousPage()
            }

            mBinding?.btnPreviousAvatar -> {
                mBinding?.avatarCarousel?.animateNextPage()
            }
        }
    }

    private fun intentNavigation() {
        openTutorialActivity()
//        openGameLanguageActivity()
    }

    private fun isValidate(isShowValidation: Boolean = false): Boolean {
        var isValid = true
        if (mBinding?.edtUserName?.text.toString().trim().isEmpty()) {
            if (isShowValidation) toast(getString(R.string.please_enter_your_name))
            isValid = false
        } else if (mBinding?.country.isNullOrEmpty()) {
            if (isShowValidation) toast(getString(R.string.please_select_your_country))
            isValid = false
        } else if (mBinding?.language == null) {
            isValid = false
        }
        return isValid
    }

    private fun showProgress(isShow: Boolean) {
        if (isShow) {
            mBinding?.progressView?.show()
            mBinding?.btnSend?.isEnabled = false
            mBinding?.btnSend?.text = getString(R.string.sending)
        } else {
            mBinding?.progressView?.invisible()
            mBinding?.btnSend?.isEnabled = true
            mBinding?.btnSend?.text = getString(R.string.next)
        }
    }
}