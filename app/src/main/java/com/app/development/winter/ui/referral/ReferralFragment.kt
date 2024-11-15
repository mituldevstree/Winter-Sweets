package com.app.development.winter.ui.referral

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.FragmentReferralBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.viewbase.BaseFragment
import com.app.development.winter.shared.extension.isFragmentAdded
import com.app.development.winter.shared.extension.lifecycleOwner
import com.app.development.winter.shared.extension.show
import com.givvy.invitefriends.builder.ReferralLibManager
import com.givvy.invitefriends.builder.ReferralLibManager.referralEventObserver
import com.givvy.invitefriends.shared.model.LoadingState
import com.givvy.invitefriends.shared.model.ReferralUiState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReferralFragment : BaseFragment<FragmentReferralBinding>(FragmentReferralBinding::inflate) {

    private var isLoaded: Boolean = false
    private var referralFragmentInstance: Fragment? = null
    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return null
    }

    override fun initUI() {
        showProgressBar()
    }

    override fun initDATA() {
        activity?.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.window_background
            )
        )
        referralFragmentInstance?.let { it1 -> loadFragment(it1) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeReferralEvents()
    }

    /**
     * Lib event observer
     */
    private fun observeReferralEvents() {
        lifecycleScope.launch {
            referralEventObserver.collectLatest {
                when (it) {
                    is ReferralUiState.OnReferralProgramInitialized -> {
                        Log.e("ReferralLib", "LibInitialized")
                        MainScope().launch {
                            /*   mBinding?.btnLoad?.isEnabled = true
                               mBinding?.btnLoad?.text = getString(R.string.load)*/
                            requestToLoadReferralFragment()
                        }
                    }

                    is ReferralUiState.OnReferralProgramFailure -> {
                        Log.e("ReferralLib", "LibFailed")
                        MainScope().launch {
                            hideProgressBar()
                        }
                    }

                    is ReferralUiState.OnReferralEarningUpdates -> {
                        Log.e("ReferralLib", "EarningUpdate")
                        Toast.makeText(
                            requireContext(),
                            "Reward earned ${it.earningInfo.earnCredits}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onRefreshUserInfo()
                    }

                    is ReferralUiState.ShowReferralDialog -> {

                    }

                    is ReferralUiState.OnReferralUIAvailable -> {
                        Log.e("ReferralLib", "OnUiAvailable")
                        when (it.loadingState) {
                            LoadingState.COMPLETED -> {
                                if (it.fragment != null) {
                                    referralFragmentInstance = it.fragment
                                    referralFragmentInstance?.let { it1 -> loadFragment(it1) }
                                }
                            }

                            LoadingState.ERROR -> {

                            }

                            else -> {

                            }
                        }
                    }

                    else -> {
                        Log.e("ReferralLib", "Else")
                    }
                }
            }
        }
    }

    /**
     * Method to trigger lib view.
     */
    private fun requestToLoadReferralFragment() {
        requireContext().lifecycleOwner()?.let {
            ReferralLibManager.getReferralFragment(
                lifecycleOwner = it,
                sessionUserID = LocalDataHelper.getUserDetail()?.id ?: "",
                sessionUserName = LocalDataHelper.getUserDetail()?.name ?: "",
                loadingView = null
            )
        }
    }

    private fun loadFragment(fragment: Fragment) {
        if (isFragmentAdded() && mBinding != null) {
            hideProgressBar()
            isLoaded = true
            if (childFragmentManager.fragments.isNotEmpty())
                childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            childFragmentManager.beginTransaction()
                .replace(R.id.layoutInviteContainer, fragment, tag)
                .commitAllowingStateLoss()
            mBinding?.root?.postDelayed({
                mBinding?.layoutInviteContainer?.show()
            }, 250)
        }
    }
}