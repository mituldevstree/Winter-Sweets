package com.app.development.winter.ui.delete

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.databinding.BottomSheetDeleteAccountBinding
import com.app.development.winter.shared.bottomsheetbase.BaseBottomSheetFragment
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BottomSheetDeleteAccount : BaseBottomSheetFragment<BottomSheetDeleteAccountBinding>(
    BottomSheetDeleteAccountBinding::inflate, true, true
), View.OnClickListener, IItemViewListener {

    private var mButtonClick: ((Int) -> Unit)? = null

    override fun init() {
        super.init()
        initUi()
    }

    private fun initUi() {
        mBinding.clickListener = this
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding.btnYes -> {
                mButtonClick?.invoke(MODE_DELETE)
                dismiss()
            }

            mBinding.layoutToolbar.btnClose -> {
                mButtonClick?.invoke(MODE_CANCEL)
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(dialogButtonClick: (Int) -> Unit): BottomSheetDeleteAccount {
            val fragment = BottomSheetDeleteAccount()
            fragment.mButtonClick = dialogButtonClick
            return fragment
        }

        const val MODE_FEEDBACK = 2
        const val MODE_CANCEL = 3
        const val MODE_DELETE = 4
    }

    override fun onItemClick(view: View?, position: Int?, actionType: Int?, vararg objects: Any?) {
        lifecycleScope.launch {
            delay(500)
            onClick(mBinding.layoutToolbar.btnClose)
        }
    }
}