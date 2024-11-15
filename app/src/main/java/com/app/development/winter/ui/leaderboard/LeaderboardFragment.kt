package com.app.development.winter.ui.leaderboard

import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.FragmentLeaderBoardBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.viewbase.AdvancedBaseFragment
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.show
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.leaderboard.adapter.LeaderboardUserAdapter
import com.app.development.winter.ui.leaderboard.event.LeaderboardEvent
import com.app.development.winter.ui.leaderboard.model.LeaderBoardUser
import com.app.development.winter.ui.leaderboard.state.LeaderboardUiState
import com.app.development.winter.ui.leaderboard.viewmodel.LeaderboardViewModel
import kotlinx.coroutines.launch

class LeaderboardFragment :
    AdvancedBaseFragment<FragmentLeaderBoardBinding, LeaderboardEvent, LeaderboardUiState, LeaderboardViewModel>(
        FragmentLeaderBoardBinding::inflate, LeaderboardViewModel::class.java
    ) {
    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return mBinding?.layoutToolbar
    }


    override fun initUI() {
        activity?.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.window_background
            )
        )
    }

    override fun initDATA() {
        mViewModel.getLeaderboardState().handleEvent(LeaderboardEvent.RequestLeaderboardUsers)
    }

    override fun render(state: LeaderboardUiState) {
        if (isAdded.not()) return
        lifecycleScope.launch {
            when (state.loadingState.first) {
                ApiEndpoints.GET_LEADERBOARD_USERS -> {
                    when (state.loadingState.second) {
                        AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                            hideErrorView()
                            mBinding?.loadingLayout?.root?.show()
                            mBinding?.layoutContent?.hide()
                        }

                        AdvanceBaseViewModel.LoadingState.COMPLETED, AdvanceBaseViewModel.LoadingState.RELOAD -> {
                            mBinding?.loadingLayout?.root?.hide()
                            mBinding?.layoutContent?.show()
                            setToThreeUsers(state.top3Users)
                            setLeaderBoardUsers(state.users)
                            setMyUserData(state.myUserRank)

                            if (mBinding?.rankOne == null) {
                                mBinding?.layoutContent?.hide()
                                setErrorView(
                                    errorMessage = getString(R.string.leaderboard_data_not_available),
                                    errorIcon = R.drawable.lib_leaderboard_page_icon
                                )
                            }
                        }

                        AdvanceBaseViewModel.LoadingState.ERROR -> {
                            mBinding?.loadingLayout?.root?.hide()
                            mBinding?.layoutContent?.hide()
                            if (mBinding?.rankOne == null) {
                                setErrorView(
                                    errorMessage = state.errorMessage,
                                    errorIcon = R.drawable.lib_leaderboard_page_icon
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setMyUserData(myUserRank: LeaderBoardUser?) {

    }

    private fun setLeaderBoardUsers(list: MutableList<LeaderBoardUser>?) {
        list?.let {
            if (mBinding?.adapter == null) {
                mBinding?.adapter = LeaderboardUserAdapter(list)
            } else {
                mBinding?.adapter?.updateItems(list)
            }
        }
    }

    private fun setToThreeUsers(top3Users: MutableList<LeaderBoardUser>?) {
        top3Users?.forEach {
            when (it.rank) {
                1 -> {
                    mBinding?.rankOne = it
                }

                2 -> {
                    mBinding?.rankTwo = it
                }

                else -> {
                    mBinding?.rankThree = it
                }
            }
        }
    }
}

