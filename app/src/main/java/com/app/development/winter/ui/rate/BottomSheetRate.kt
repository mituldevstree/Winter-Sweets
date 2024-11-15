package com.app.development.winter.ui.rate

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.app.development.winter.R
import com.app.development.winter.databinding.BottomSheetRateBinding
import com.app.development.winter.shared.bottomsheetbase.BaseBottomSheetFragment
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.show
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BottomSheetRate : BaseBottomSheetFragment<BottomSheetRateBinding>(
    BottomSheetRateBinding::inflate, true, true
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
                managePlayStoreRating()
            }

            mBinding.btnNo -> {
                if (mBinding.btnNo.text == getString(R.string.no)) {
                    mButtonClick?.invoke(MODE_FEEDBACK)
                    dismiss()
                } else {
                    mButtonClick?.invoke(MODE_RATING)
                    dismiss()
                }
            }

            mBinding.layoutToolbar.btnClose -> {
                mButtonClick?.invoke(MODE_CANCEL)
                dismiss()
            }
        }
    }

    private fun managePlayStoreRating() {
        mBinding.btnYes.invisible()
        mBinding.ivStars.show()

        mBinding.btnNo.text = getString(R.string.rate_us)
        mBinding.txtMessageTitle.text = getString(R.string.google_play_rate_title)
        mBinding.txtMessage.text = getString(R.string.google_play_rate_message)
    }

    companion object {
        fun newInstance(dialogButtonClick: (Int) -> Unit): BottomSheetRate {
            val fragment = BottomSheetRate()
            fragment.mButtonClick = dialogButtonClick
            return fragment
        }

        const val RATING_MODE = "rating_mode"
        const val MODE_RATING = 1
        const val MODE_FEEDBACK = 2
        const val MODE_CANCEL = 3
    }

    override fun onItemClick(view: View?, position: Int?, actionType: Int?, vararg objects: Any?) {
        lifecycleScope.launch {
            delay(500)
            onClick(mBinding.btnNo)
        }
    }
}