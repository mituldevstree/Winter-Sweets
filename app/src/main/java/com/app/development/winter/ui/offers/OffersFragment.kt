package com.app.development.winter.ui.offers

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.FragmentOffersBinding
import com.app.development.winter.databinding.LayoutLoadingViewBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.manager.OfferWallLibInitializer
import com.app.development.winter.shared.base.viewbase.BaseFragment
import com.app.development.winter.shared.extension.handleVisualOverlaps
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.isFragmentAdded
import com.app.development.winter.shared.extension.toast
import com.givvy.offerwall.app.builder.OfferwallLibBuilder
import com.givvy.offerwall.app.shared.model.LoadingState
import com.givvy.offerwall.app.shared.model.OfferwallLibEventState
import com.givvy.offerwall.app.shared.model.OfferwallLibState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OffersFragment :
    BaseFragment<FragmentOffersBinding>(FragmentOffersBinding::inflate) {
    private var loadingLayoutBinding: LayoutLoadingViewBinding? = null

    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initProgressView(mBinding?.root)
    }

    override fun initUI() {
        mBinding?.root?.handleVisualOverlaps(true, Gravity.TOP, false)
    }

    override fun initDATA() {
        initListeners()
    }

    private fun initProgressView(view: View?) {
        view?.let {
            if (it.findViewById<ConstraintLayout>(R.id.layoutAppLoaderView) != null) {
                loadingLayoutBinding = LayoutLoadingViewBinding.bind(it)
            }
        }
    }

    /**
     * Lib methods
     */
    private fun initListeners() {
        observeOfferwallLibInitState()
        observeOfferwallEventState()
    }


    /**
     * Lib event observer
     */
    private fun observeOfferwallLibInitState() {
        lifecycleScope.launch {
            OfferwallLibBuilder.observerOfferwallLibInitialization().collectLatest {
                when (it) {
                    is OfferwallLibState.OnOfferwallLibInitialized -> {
                        Log.e("LibInit", "Success")
                        MainScope().launch {
                            OfferWallLibInitializer.requestToLoadWithdrawFragment(viewLifecycleOwner)
                        }
                    }

                    is OfferwallLibState.OnOfferwallLibInitializedFailure -> {
                        loadingLayoutBinding?.root?.hide()
                        toast("Failed to load lib, check config api")
                    }

                    is OfferwallLibState.OnOfferwallLibAvailable -> {
                        when (it.loadingState) {
                            LoadingState.COMPLETED -> {
                                loadingLayoutBinding?.root?.hide()
                                if (it.fragment != null) {
                                    loadFragment(it.fragment!!)
                                }

                            }

                            LoadingState.ERROR -> {
                                loadingLayoutBinding?.root?.hide()
                            }

                            else -> {
                                loadingLayoutBinding?.root?.hide()
                            }
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }

    /**
     * Lib event observer
     */
    private fun observeOfferwallEventState() {
        lifecycleScope.launch {
            OfferwallLibBuilder.observerOfferwallLibEvents(lifecycleScope)
                .collectLatest {
                    when (it) {
                        is OfferwallLibEventState.OnOfferwallEarningUpdates -> {
                            mTooBarBase?.onUserInfoUpdate()
                        }

                        is OfferwallLibEventState.OnOfferwallExitTrigger -> {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }

                        else -> {

                        }
                    }
                }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        if (isFragmentAdded() && mBinding != null) {
            val manager: FragmentManager = childFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            transaction.replace(R.id.layoutOfferContainer, fragment)
            transaction.commitAllowingStateLoss()
        }
    }

}