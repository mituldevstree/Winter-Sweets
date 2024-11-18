package com.app.development.winter.ui.withdraw


import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.FragmentWithdrawPageBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.manager.WalletLibManager
import com.app.development.winter.shared.base.viewbase.BaseFragment
import com.app.development.winter.shared.extension.handleVisualOverlaps
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.isFragmentAdded
import com.app.development.winter.shared.extension.lifecycleOwner
import com.app.development.winter.shared.extension.show
import com.givvy.facetec.lib.FaceTecLibBuilder
import com.givvy.withdrawfunds.shared.model.LoadingState
import com.givvy.withdrawfunds.shared.model.WalletLibEventState
import com.givvy.withdrawfunds.shared.model.WalletLibUiState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WithdrawFragment :
    BaseFragment<FragmentWithdrawPageBinding>(FragmentWithdrawPageBinding::inflate) {

    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLibObservers()
    }

    override fun initUI() {
        mBinding?.root?.handleVisualOverlaps(true, Gravity.TOP)
        mBinding?.layoutError?.root?.hide()
        WalletLibManager.getWithdrawFragment()?.let { it1 -> loadFragment(it1) }
    }

    override fun initDATA() {

    }

    /**
     * Lib observers
     */
    private fun initLibObservers() {
        observeWithdrawInitState()
        observeWithdrawEventState()
    }

    /**
     * Lib wallet lib initialization state observer
     */
    private fun observeWithdrawInitState() {
        lifecycleScope.launch {
            com.givvy.withdrawfunds.builder.WalletLibInitializer.observerWalletInitializationEvent().collectLatest {
                when (it) {
                    is WalletLibUiState.OnWalletLibInitialized -> {
                        mBinding?.layoutError?.root?.hide()
                        MainScope().launch {
                            mBinding?.loadingLayout?.root?.show()
                            requestToLoadWithdrawFragment()
                        }
                    }

                    is WalletLibUiState.OnWalletLibInitializedFailure -> {
                        mBinding?.loadingLayout?.root?.show()
                        mBinding?.layoutError?.root?.show()
                        Log.e("LibInit", "failed")
                        mBinding?.layoutError?.txtErrorMessage?.text = it.errorMessage
                        mBinding?.layoutError?.ivErrorIcon?.setImageResource(R.drawable.app_logo)
                    }

                    is WalletLibUiState.OnWalletLibUIAvailable -> {
                        when (it.loadingState) {
                            LoadingState.COMPLETED -> {
                                mBinding?.layoutError?.root?.hide()
                                Log.e("LibInit", "UiAvailable")
                                if (it.fragment != null) {
                                    WalletLibManager.saveWithdrawFragmentInstance(it.fragment!!)
                                    loadFragment(WalletLibManager.getWithdrawFragment()!!)
                                }

                            }

                            LoadingState.ERROR -> {
                                Log.e("LibInit", "NotAvailable")
                                mBinding?.layoutError?.root?.show()
                                mBinding?.layoutError?.txtErrorMessage?.text =
                                    getString(R.string.failed_to_load)
                                mBinding?.layoutError?.ivErrorIcon?.setImageResource(R.drawable.app_logo)
                            }

                            else -> {
                                Log.e("LibInit", "UiLoading")
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
    private fun observeWithdrawEventState() {
        lifecycleScope.launch {
            com.givvy.withdrawfunds.builder.WalletLibInitializer.observerWalletLibEvents(lifecycleScope)
                .collectLatest {
                    when (it) {
                        is WalletLibEventState.OnWalletEarningUpdates -> {
                            Log.e("OnEarning", "Update received")
                            mBase?.onUserInfoUpdate()
                        }

                        is WalletLibEventState.OnWalletLibNeedFaceVerification -> {
                            startFaceTecVerification()

                        }

                        is WalletLibEventState.OnLibExitTrigger -> {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }

                        else -> {

                        }
                    }
                }
        }
    }

    /**
     * Method to trigger lib view.
     */
    private fun requestToLoadWithdrawFragment() {
        requireContext().lifecycleOwner()?.let {
            com.givvy.withdrawfunds.builder.WalletLibInitializer.getWithdrawUiFragmentInstance(
                lifecycleOwner = it,
                sessionUserID = LocalDataHelper.getUserDetail()?.id ?: "",
                sessionUserEmailId = LocalDataHelper.getUserDetail()?.email ?: "",
                sessionAuthCode = LocalDataHelper.getAuthCode()
            )
        }
    }

    private fun loadFragment(fragment: Fragment) {
        if (isFragmentAdded() && mBinding != null) {
            mBinding?.loadingLayout?.root?.hide()
            if (childFragmentManager.fragments.isNotEmpty())
                childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            childFragmentManager.beginTransaction()
                .replace(R.id.layoutWithdrawContainer, fragment, tag)
                .commitAllowingStateLoss()
            mBinding?.root?.postDelayed({
                mBinding?.layoutWithdrawContainer?.show()
            }, 250)
        }
    }

    companion object {
        const val TAG_NAME = "AllOffersFragment"
    }

    private fun startFaceTecVerification() {
        MainScope().launch {
            if (FaceTecLibBuilder.isFaceTecInitialized) {
                mMonetizationBase?.startFaceTechVerification(onSuccess = {
                    com.givvy.withdrawfunds.builder.WalletLibInitializer.onFaceTechVerificationComplete()
                }, onFailure = {
                    com.givvy.withdrawfunds.builder.WalletLibInitializer.onFaceTechVerificationFailed()
                })
            } else {
                mMonetizationBase?.initFaceTec { wasInitialized ->
                    if (wasInitialized) {
                        startFaceTecVerification()
                    } else {
                        com.givvy.withdrawfunds.builder.WalletLibInitializer.onFaceTechVerificationFailed()
                    }
                }
            }
        }
    }
}