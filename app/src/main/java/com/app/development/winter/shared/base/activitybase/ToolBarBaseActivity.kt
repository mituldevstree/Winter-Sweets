package com.app.development.winter.shared.base.activitybase

import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import com.app.development.winter.R
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import com.monetizationlib.data.Monetization

abstract class ToolBarBaseActivity<VDB : ViewDataBinding, INTENT : ViewIntent, STATE : ViewState, VM : AdvanceBaseViewModel<INTENT, STATE>>(
    bindingFactory: (LayoutInflater) -> VDB,
    modelClass: Class<VM>,
) : NavigationBaseActivity<VDB, INTENT, STATE, VM>(bindingFactory, modelClass) {
    private var mToolbarBinding: LayoutToolbarBinding? = null

    var currentNavController: NavController? = null
    private val userViewModel: UserViewModel by viewModels()
    var shouldShowDownloadFeature: Boolean = false
    var shouldShowDebuggerOption: Boolean = false
    abstract fun getToolBarBinding(): LayoutToolbarBinding?

    open fun hideBottomNavigation() {}
    open fun onDownloadFeatureTriggeredByUser() {}
    override fun setContentView(view: View?) {
        super.setContentView(view)
        initToolBarView(view)

    }

    private fun initToolBarView(view: View?) {
        view?.let {
            mToolbarBinding = getToolBarBinding()
            setToolBarListeners(mToolbarBinding)
        }
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        mToolbarBinding?.appTitle = title.toString()
    }

    override fun setTitle(titleId: Int) {
        super.setTitle(titleId)
        mToolbarBinding?.appTitle = getString(titleId)
    }

    fun captureBackPressState() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onSuperBackPressed()
                }
            })
    }

    open fun onSuperBackPressed() {}

    fun setToolBarListeners(mToolbarBinding: LayoutToolbarBinding?) {
        val toolbarListeners = View.OnClickListener {
            it?.hapticFeedbackEnabled()
            when (it) {
                mToolbarBinding?.btnBack, mToolbarBinding?.btnClose -> {
                    if (currentNavController != null) {
                        currentNavController?.navigateUp()
                    } else {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }

                mToolbarBinding?.userImage, mToolbarBinding?.viewProfileBg -> {
                    onTabChangeRequest(R.id.navWithdraw)
                }

                mToolbarBinding?.btnNotification -> {
                    openNotificationActivity()
                }

                mToolbarBinding?.btnSettings -> {
                    openSettingsFragment()
                }

                mToolbarBinding?.btnSwitch -> {
                    onSwitchClicked(mToolbarBinding?.btnSwitch?.isChecked)
                }

                mToolbarBinding?.btnDownloadFeature -> {
                    onDownloadFeatureTriggeredByUser()
                }

                mToolbarBinding?.btnAdDebugger -> {
                    Monetization.openAdLoaderDebugger()
                }
            }
        }
        mToolbarBinding?.clickListener = toolbarListeners
    }

    open fun onSwitchClicked(checked: Boolean?) {
        checked?.let { LocalDataHelper.isNotificationOn = it }
    }

    override fun onUserInfoUpdate() {
        super.onUserInfoUpdate()
        userViewModel.getUser { }
    }
}