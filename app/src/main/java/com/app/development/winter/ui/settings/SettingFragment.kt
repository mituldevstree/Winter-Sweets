package com.app.development.winter.ui.settings

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.FragmentSettingsBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.localcache.LocaleHelper
import com.app.development.winter.localcache.StaticData
import com.app.development.winter.manager.WalletLibManager
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.activitybase.NavigationBaseActivity.Companion.PROFILE_EDITED
import com.app.development.winter.shared.base.activitybase.NavigationBaseActivity.Companion.REQUEST_TUTORIAL
import com.app.development.winter.shared.base.activitybase.NavigationBaseActivity.Companion.RESULT_KEY
import com.app.development.winter.shared.base.activitybase.NavigationBaseActivity.Companion.RESULT_TYPE
import com.app.development.winter.shared.base.viewbase.AdvancedBaseFragment
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.forceRestart
import com.app.development.winter.shared.extension.handleVisualOverlaps
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.delete.BottomSheetDeleteAccount
import com.app.development.winter.ui.settings.model.Setting
import com.app.development.winter.ui.settings.model.Setting.SettingType
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingFragment :
    AdvancedBaseFragment<FragmentSettingsBinding, UserEvents, UserState, UserViewModel>(
        FragmentSettingsBinding::inflate, UserViewModel::class.java
    ), IItemViewListener {

    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return mBinding?.layoutToolbar
    }

    override fun initUI() {
        mBinding?.user = LocalDataHelper.getUserDetail()
    }

    override fun initDATA() {
        mBinding?.layoutToolbar?.toolbar?.handleVisualOverlaps(true, Gravity.TOP)
        setSettingAdapter(StaticData.getSettings(requireContext()))
        observeFragmentResult()
    }

    override fun render(state: UserState) {
        if (state.loadingState.first == ApiEndpoints.DELETE_USER) {
            when (state.loadingState.second) {
                AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                    showProgressBar()
                }

                AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                    hideProgressBar()
                    lifecycleScope.launch {
                        delay(300)
                        showAlertDialog(imageRes = R.drawable.app_logo,
                            title = getString(R.string.app_name),
                            cancelable = false,
                            message = state.deleteUser?.message
                                ?: getString(R.string.delete_account_desc),
                            positiveRes = R.string.ok,
                            onCancel = {
                                LocalDataHelper.logout()
                                LocalDataHelper.clearUserPreference()
                                activity?.finishAffinity()
                            })
                    }
                }

                AdvanceBaseViewModel.LoadingState.ERROR -> {
                    hideProgressBar()
                    showAlertDialog(title = getString(R.string.delete_account),
                        message = state.errorMessage,
                        imageRes = R.drawable.app_logo,
                        positiveRes = R.string.ok,
                        cancelable = false,
                        positiveClick = {
                            //findNavController().navigateUp()
                            LocalDataHelper.logout()
                            LocalDataHelper.clearUserPreference()
                            activity?.finishAffinity()
                        })
                }

                else -> {

                }
            }
        }
    }

    private fun observeFragmentResult() {
        setFragmentResultListener(RESULT_KEY) { key, bundle ->
            if (key == RESULT_KEY) {
                when (bundle.getString(RESULT_TYPE)) {
                    PROFILE_EDITED -> {
                        mNavigationBase?.onUserInfoUpdate()
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private fun setSettingAdapter(list: ArrayList<Setting>) {
        if (mBinding?.adapterSetting == null) {
            mBinding?.adapterSetting = SettingAdapter(list, this)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onItemClick(view: View?, position: Int?, actionType: Int?, vararg objects: Any?) {
        view?.postDelayed({
            val data = objects[0] as Setting
            view.hapticFeedbackEnabled()
            when (data.type) {
                SettingType.LANGUAGE -> {
                    mNavigationBase?.showLanguageDialog(LocaleHelper.getLanguage()) {
                        LocaleHelper.setLanguage(it)
                        mNavigationBase?.openSplashActivity()
                    }
                }

                SettingType.PROFILE -> {
                    mNavigationBase?.openEditProfileActivity()
                }

                SettingType.POLICY -> {
                    mNavigationBase?.openPrivacyPolicyLink()
                }

                SettingType.TERMS -> {
                    mNavigationBase?.openTermsConditionLink()
                }

                SettingType.CONTACT_US -> {
                    WalletLibManager.openContactUsFlow(requireContext())
                }

                SettingType.GET_SPECIAL_REWARD -> {
                    WalletLibManager.openBonusFlow(childFragmentManager) { wasCodeProcessed ->
                        if (wasCodeProcessed) {
                            if (LocalDataHelper.isOnProduction().not()) {
                                requireContext().forceRestart()
                            } else {
                                mBase?.onUserInfoUpdate()
                            }
                        } else {
                            //where eveer what eveer
                        }
                    }
                }

                SettingType.TUTORIAL -> {
                    mNavigationBase?.openTutorialActivity(REQUEST_TUTORIAL)
                }

                SettingType.DELETE -> {
                    BottomSheetDeleteAccount.newInstance {
                        when (it) {
                            BottomSheetDeleteAccount.MODE_DELETE -> {
                                mViewModel.getUserUiState().handleEvent(UserEvents.DeleteUser)
                            }

                            BottomSheetDeleteAccount.MODE_CANCEL -> {
                            }

                            else -> {
                            }
                        }
                    }.show(childFragmentManager)
                }

                SettingType.XP_OVERLAY -> {
                    mNavigationBase?.showUserStatistics()
                }

                SettingType.REFERRAL_CODE -> {

                }
            }
        }, 20)
    }
}