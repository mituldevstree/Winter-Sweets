package com.app.development.winter.ui.profile

import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.formatter.ValueFormatter
import com.app.development.winter.R
import com.app.development.winter.databinding.FragmentProfileBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.viewbase.AdvancedBaseFragment
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.handleVisualOverlaps
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.setBarChartProperties
import com.app.development.winter.shared.extension.show
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.profile.event.ProfileEvent
import com.app.development.winter.ui.profile.model.ProfileEarnings
import com.app.development.winter.ui.profile.model.TabModel
import com.app.development.winter.ui.profile.state.ProfileEarningState
import com.app.development.winter.ui.profile.viewmodel.ProfileViewModel
import java.text.DecimalFormat

class ProfileFragment :
    AdvancedBaseFragment<FragmentProfileBinding, ProfileEvent, ProfileEarningState, ProfileViewModel>(
        FragmentProfileBinding::inflate, ProfileViewModel::class.java
    ), View.OnClickListener, IItemViewListener {
    private var mSelectedType: ProfileEarningState.Companion.EarningFilterType =
        ProfileEarningState.Companion.EarningFilterType.DAILY
    private var isChartLoadedWithAnim = true
    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return mBinding?.layoutToolbar
    }

    override fun initUI() {
    }

    override fun initDATA() {
        mBinding?.layoutToolbar?.toolbar?.handleVisualOverlaps(true, Gravity.TOP)
        mViewModel.getProfileState().handleEvent(
            ProfileEvent.RequestGetEarnings(
                type = ProfileEarningState.Companion.EarningFilterType.DAILY
            )
        )
        mBinding?.user = LocalDataHelper.getUserDetail()
        mBinding?.listener = this
        setTabLayout()

        mBinding?.earningBarChart?.alpha = 0f
        mBinding?.earningBarChart?.post {
            previousStates?.let { setChartData(it) }
            mBinding?.earningBarChart?.alpha = 1.0f
        }
    }

    private fun setTabLayout() {
        val items: ArrayList<TabModel> = ArrayList()
        items.add(TabModel(getString(R.string.daily), 1, true))
        items.add(TabModel(getString(R.string.weekly), 2, false))
        items.add(TabModel(getString(R.string.monthly), 3, false))
        mBinding?.adapter = TabAdapter(items, this)
    }

    override fun render(state: ProfileEarningState) {
        if (state.loadingState.first == ApiEndpoints.GET_PROFILE_EARNINGS) {
            when (state.loadingState.second) {
                AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                    mBinding?.layoutLoadingView?.defaultLoader?.show()
                    mBinding?.earningBarChart?.invisible()
                }

                AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                    if (state.selectedType == mSelectedType) {
                        mBinding?.layoutLoadingView?.defaultLoader?.hide()
                        mBinding?.earningBarChart?.show()
                        setChartData(state)
                    }
                }

                AdvanceBaseViewModel.LoadingState.ERROR -> {
                    mBinding?.layoutLoadingView?.defaultLoader?.hide()
                    mBinding?.earningBarChart?.show()
                }

                AdvanceBaseViewModel.LoadingState.RELOAD -> {
                    setChartData(state)
                }

                else -> {}
            }
        }

    }

    private var previousStates: ProfileEarningState? = null
    private fun setChartData(state: ProfileEarningState) {
        previousStates = state
        when (state.selectedType) {
            ProfileEarningState.Companion.EarningFilterType.DAILY -> {
                setDailyData(state.daily)
            }

            ProfileEarningState.Companion.EarningFilterType.WEEKLY -> {
                setWeeklyData(state.weekly)
            }

            else -> {
                setMonthly(state.monthly)
            }
        }
        isChartLoadedWithAnim = false
    }

    override fun onItemClick(view: View?, position: Int?, actionType: Int?, vararg objects: Any?) {
        isChartLoadedWithAnim = true
        when (position) {
            0 -> mSelectedType = ProfileEarningState.Companion.EarningFilterType.DAILY
            1 -> mSelectedType = ProfileEarningState.Companion.EarningFilterType.WEEKLY
            2 -> mSelectedType = ProfileEarningState.Companion.EarningFilterType.MONTHLY
        }
        mViewModel.getProfileState().handleEvent(ProfileEvent.RequestGetEarnings(mSelectedType))
    }

    private fun setDailyData(daily: MutableList<ProfileEarnings>?) {
        if (daily.isNullOrEmpty().not()) {
            mBinding?.earningBarChart?.setRadius(3)
            mBinding?.earningBarChart?.setBarChartProperties(
                daily,
                isChartLoadedWithAnim,
                8f,
                object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return DecimalFormat("#,##,###").format(value)
                    }
                })
        }
    }

    private fun setWeeklyData(weekly: MutableList<ProfileEarnings>?) {
        if (weekly.isNullOrEmpty().not()) {
            mBinding?.earningBarChart?.setRadius(10)
            mBinding?.earningBarChart?.setBarChartProperties(
                weekly,
                isChartLoadedWithAnim,
                8f,
                object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return DecimalFormat("#,##,###").format(value)
                    }
                })
        }
    }

    private fun setMonthly(monthly: MutableList<ProfileEarnings>?) {
        if (monthly.isNullOrEmpty().not()) {
            mBinding?.earningBarChart?.setRadius(5)
            mBinding?.earningBarChart?.setBarChartProperties(
                monthly,
                isChartLoadedWithAnim,
                8f,
                object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return DecimalFormat("#,##,###").format(value)
                    }
                })
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding?.btnWithdraw -> {
                mNavigationBase?.openWithdrawFragment()
            }
        }
    }
}

