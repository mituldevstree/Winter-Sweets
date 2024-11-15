package com.app.development.winter.ui.rate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.development.winter.R
import com.app.development.winter.databinding.BottomSheetFeedbackBinding
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.activitybase.NavigationBaseActivity
import com.app.development.winter.shared.bottomsheetbase.BaseBottomSheetFragment
import com.app.development.winter.shared.extension.getTrimText
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import com.app.development.winter.utility.Validator
import kotlinx.coroutines.launch

class BottomSheetFeedback: BaseBottomSheetFragment<BottomSheetFeedbackBinding>(
    BottomSheetFeedbackBinding::inflate, true, true, false
), View.OnClickListener {

    private var mListeners: ((Boolean) -> Unit)? = null
    private val viewModel: UserViewModel by viewModels()

    override fun init() {
        super.init()
        mBinding.clickListener = this
        lifecycleScope.launch {
            viewModel.getUserUiState().collect { uiState ->
                render(uiState)
            }
        }
    }

    private fun render(state: UserState) {
        lifecycleScope.launch {
            when (state.loadingState.second) {
                AdvanceBaseViewModel.LoadingState.PROCESSING -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.SEND_FEEDBACK -> {
                            showProgress(true)
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.COMPLETED -> {
                    when (state.loadingState.first) {
                        ApiEndpoints.SEND_FEEDBACK -> {
                            showProgress(false)
                            if (mListeners != null) {
                                mListeners?.invoke(true)
                                this@BottomSheetFeedback.dismiss()
                            } else {
                                val bundle = Bundle()
                                bundle.putBoolean(FEEDBACK_GIVEN, true)
                                requireActivity().supportFragmentManager.setFragmentResult(
                                    NavigationBaseActivity.INTENT_KEY, bundle
                                )
                                findNavController().navigateUp()
                            }
                        }
                    }
                }

                AdvanceBaseViewModel.LoadingState.ERROR -> {
                    showProgress(false)
                    showAlertDialog(title = getString(R.string.error),
                        message = state.errorMessage,
                        imageRes = R.drawable.app_logo,
                        positiveRes = R.string.ok,
                        onCancel = {},
                        onDismiss = {})
                }

                else -> {

                }
            }
        }
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding.btnSend -> {
                if (validate()) sendFeedback()
            }

            mBinding.layoutToolbar.btnClose -> {
                mListeners?.invoke(false)
                findNavController().navigateUp()
                this.dismiss()
            }
        }
    }

    private fun showProgress(isShow: Boolean) {
        if (isShow) {
            mBinding.progressView.show()
            mBinding.btnSend.isEnabled = false
            mBinding.btnSend.text = getString(R.string.sending)
        } else {
            mBinding.progressView.invisible()
            mBinding.btnSend.isEnabled = true
            mBinding.btnSend.text = getString(R.string.submit)
        }
    }

    private fun sendFeedback() {
        val name = com.app.development.winter.localcache.LocalDataHelper.getUserDetail()?.name ?: ""
        val email = mBinding.edtEmail.text.toString()
        val message = mBinding.edtMessage.text.toString()

        viewModel.getUserUiState().handleEvent(UserEvents.SendFeedback(name, email, message))
    }

    private fun validate(): Boolean {
        var isValid = true
        if (Validator.isValidEmail(mBinding.edtEmail.getTrimText()).not()) {
            toast(getString(R.string.please_enter_your_email))
            isValid = false
        } else if (Validator.isEmptyFieldValidate(mBinding.edtMessage.text.toString())) {
            toast(getString(R.string.please_enter_your_message))
            isValid = false
        }
        return isValid
    }

    companion object {
        const val FEEDBACK_GIVEN: String = "feedback_given"


        fun newInstance(listener: (Boolean) -> Unit): BottomSheetFeedback {
            val fragment = BottomSheetFeedback()
            fragment.mListeners = listener
            return fragment
        }
    }
}