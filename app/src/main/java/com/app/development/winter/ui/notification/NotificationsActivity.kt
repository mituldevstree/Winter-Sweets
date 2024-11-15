package com.app.development.winter.ui.notification

import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.development.winter.R
import com.app.development.winter.databinding.ActivityNotificationBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.activitybase.ToolBarBaseActivity
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.show
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.ui.notification.adapter.NotificationAdapter
import com.app.development.winter.ui.notification.adapter.NotificationLoadStateAdapter
import com.app.development.winter.ui.notification.event.NotificationEvent
import com.app.development.winter.ui.notification.model.Notification
import com.app.development.winter.ui.notification.state.NotificationState
import com.app.development.winter.ui.notification.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

class NotificationsActivity :
    ToolBarBaseActivity<ActivityNotificationBinding, NotificationEvent, NotificationState, NotificationViewModel>(
        ActivityNotificationBinding::inflate, NotificationViewModel::class.java
    ) {

    private lateinit var adapterNotification: NotificationAdapter
    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return mBinding?.layoutToolbar
    }

    override fun initDATA() {
        mBinding?.layoutToolbar?.btnSwitch?.isChecked = LocalDataHelper.isNotificationOn
        mViewModel?.getPagingState()?.handleEvent(NotificationEvent.RequestNotifications)
    }

    override fun initUI() {
        mBinding?.layoutErrorView?.hide()
        setNotificationAdapter()
    }

    override fun render(state: NotificationState) {
        lifecycleScope.launch {
            when (state.loadingState.first) {
                AdvanceBaseViewModel.LoadingType.GET_NOTIFICATION_STATE -> {
                    when (state.loadingState.second) {
                        AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                            mBinding?.loader?.hide()
                            state.notificationList?.let { onNotificationData(it) }
                        }

                        AdvanceBaseViewModel.LoadingState.ERROR -> {
                            mBinding?.loader?.hide()
                            showAlertDialog(
                                title = getString(R.string.error),
                                message = state.errorMessage,
                                imageRes = R.drawable.app_logo,
                                positiveRes = R.string.ok,
                                onCancel = {},
                                onDismiss = {},
                            )
                        }

                        else -> {}
                    }
                }

                else -> {}
            }
        }
    }

    private fun setNotificationAdapter() {
        mBinding?.layoutErrorView?.hide()
        mBinding?.rvItems?.show()
        mBinding?.rvItems?.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            adapterNotification = NotificationAdapter()
            adapter =
                adapterNotification.withLoadStateHeaderAndFooter(footer = NotificationLoadStateAdapter {/*make retry request*/ },
                    header = NotificationLoadStateAdapter {/*make retry request*/ })
            setLoadingState()
        }
    }

    private fun setLoadingState() {
        adapterNotification.addLoadStateListener {
            if (it.source.append !is LoadState.Loading && it.source.prepend !is LoadState.Loading) {
                when (it.source.refresh) {
                    is LoadState.Loading -> {
                        if (adapterNotification.itemCount == 0) {
                            mBinding?.rvItems?.hide()
                            mBinding?.layoutErrorView?.hide()
                            mBinding?.loader?.show()
                        } else {
                            mBinding?.rvItems?.show()
                            mBinding?.layoutErrorView?.hide()
                            mBinding?.loader?.hide()
                        }
                    }

                    is LoadState.Error -> {
                        mBinding?.loader?.hide()
                        mBinding?.rvItems?.hide()
                        mBinding?.layoutErrorView?.show()
                    }

                    else -> {
                        mBinding?.rvItems?.show()
                        mBinding?.layoutErrorView?.hide()
                        mBinding?.loader?.hide()
                    }
                }
            }
        }
    }

    private fun onNotificationData(result: PagingData<Notification>) {
        lifecycleScope.launch {
            adapterNotification.submitData(result)
        }
    }
}