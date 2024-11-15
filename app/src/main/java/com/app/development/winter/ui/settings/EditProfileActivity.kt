package com.app.development.winter.ui.settings

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
import com.app.development.winter.shared.extension.forceRestart
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.shared.extension.toast
import com.app.development.winter.shared.model.User
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.views.carouselpager.CarouselViewPagerAdapter
import com.app.development.winter.shared.views.carouselpager.InfinitePagerAdapter
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditProfileActivity :
    ToolBarBaseActivity<ActivityWelcomeBinding, UserEvents, UserState, UserViewModel>(
        ActivityWelcomeBinding::inflate, UserViewModel::class.java
    ), View.OnClickListener {

    private var isUserChangeLanguage = false
    private var mPagerAdapter: CarouselViewPagerAdapter? = null

    override fun initDATA() {
        mBinding?.layoutToolbar?.appTitle = getString(R.string.edit_profile)
        mBinding?.layoutToolbar?.showBack = true
    }

    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return mBinding?.layoutToolbar
    }

    override fun initUI() {
        mBinding?.btnSend?.text = getString(R.string.save)
        mBinding?.clickListener = this
        mBinding?.user = LocalDataHelper.getUserDetail()
        mBinding?.country = LocalDataHelper.getUserDetail()?.country
        mBinding?.language = LocaleHelper.getLanguage()

        setAvatarView()
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

    override fun render(state: UserState) {
        lifecycleScope.launch {
            when (state.loadingState.second) {
                AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.UPDATE_USER_PROFILE -> {
                            showLoader(true)
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.UPDATE_USER_PROFILE -> {
                            showLoader(false)
                            LocalDataHelper.getUserDetail()?.let { user ->
                                user.name = state.userInfo?.name
                                user.email = state.userInfo?.email
                                user.photo = state.userInfo?.photo
                                user.country = mBinding?.country
                                LocalDataHelper.setUserDetail(user)
                            }
                            if (isUserChangeLanguage) {
                                LocaleHelper.setLanguage(mBinding?.language)
                                lifecycleScope.launch {
                                    delay(500)
                                    forceRestart()
                                }
                            } else {
                                openSuccessDialog()
                            }
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.ERROR -> {
                    showLoader(false)
                    showAlertDialog(title = getString(R.string.error),
                        message = state.errorMessage,
                        imageRes = R.drawable.app_logo,
                        positiveRes = R.string.ok,
                        positiveClick = {
                            finish()
                        },
                        onCancel = {},
                        onDismiss = {})
                }

                else -> {

                }
            }
        }
    }

    private fun showLoader(isShow: Boolean) {
        if (isShow) {
            mBinding?.progressView?.show()
            mBinding?.btnSend?.isEnabled = false
            mBinding?.btnSend?.text = getString(R.string.save)
        } else {
            mBinding?.progressView?.invisible()
            mBinding?.btnSend?.isEnabled = true
            mBinding?.btnSend?.text = getString(R.string.save)
        }
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding?.btnSend -> {
                if (isValidate()) {
                    val user = LocalDataHelper.getUserDetail()
                    user?.name = mBinding?.edtUserName?.text.toString()
                    user?.country = mBinding?.country
                    user?.photo =
                        mPagerAdapter?.getSelectedPhoto(mBinding?.avatarCarousel?.currentItem ?: 0)
                    editProfile(user)
                }
            }

            mBinding?.edtCountry -> {
                showCountryDialog(mBinding?.country) {
                    mBinding?.country = it
                }
            }

            mBinding?.edtLanguage -> {
                showLanguageDialog(mBinding?.language) {
                    mBinding?.language = it
                    isUserChangeLanguage =
                        LocaleHelper.getLanguage().name.equals(it.name, true).not()
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

    private fun editProfile(user: User?) {
        mViewModel?.getUserUiState()?.handleEvent(UserEvents.UpdateUserProfile(user))
    }

    private fun uploadImage(imageUrl: String) {
        mViewModel?.getUserUiState()?.handleEvent(UserEvents.UploadUserProfilePhoto(imageUrl))
    }

    private fun isValidate(): Boolean {
        var isValid = true
        if (mBinding?.edtUserName?.text.toString().trim().isEmpty()) {
            toast(getString(R.string.please_enter_your_name))
            isValid = false
        } else if (mBinding?.user?.country.isNullOrEmpty()) {
            toast(getString(R.string.please_select_your_country))
            isValid = false
        } else if (mBinding?.language == null) {
            isValid = false
        }
        return isValid
    }

    private fun openSuccessDialog() {
        showAlertDialog(title = getString(R.string.edit_profile),
            message = getString(R.string.your_profile_was_updated_successfully),
            cancelable = false,
            positiveRes = R.string.ok,
            positiveClick = {
                if (isUserChangeLanguage) {
                    LocaleHelper.setLanguage(mBinding?.language)
                    lifecycleScope.launch {
                        delay(500)
                        forceRestart()
                    }
                } else finish()
            },
            onCancel = {},
            onDismiss = {})
    }
}