package com.app.development.winter.ui.language

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import com.app.development.winter.databinding.BottomSheetLanguageBinding
import com.app.development.winter.localcache.LocaleHelper
import com.app.development.winter.shared.bottomsheetbase.BaseBottomSheetFragment
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.ui.language.model.Language

class BottomSheetLanguage : BaseBottomSheetFragment<BottomSheetLanguageBinding>(
    BottomSheetLanguageBinding::inflate, true, true
), IItemViewListener, View.OnClickListener {

    private var mListeners: ((Language?) -> Unit)? = null
    private var mSelectedLanguage: Language? = null

    override fun init() {
        super.init()
        initUi()
    }

    private fun initUi() {
        mBinding.clickListener = this
        setLanguageSelectionAdapter(Language.getLanguagesList())
    }

    private fun setLanguageSelectionAdapter(list: ArrayList<Language>) {
        if (mBinding.adapter == null) {
            mBinding.adapter = LanguageAdapter(list, this)
        } else {
            mBinding.adapter?.updateItems(list)
        }
        mBinding.adapter?.setSelectedItem(
            mSelectedLanguage?.name ?: LocaleHelper.getLanguage().name
        )
    }

    companion object {
        val REQUEST_KEY: String = "change_language"
        val LANGUAGE: String = "language"
        fun newInstance(
            selectedLanguage: Language?, dialogButtonClick: (Language?) -> Unit
        ): BottomSheetLanguage {
            val fragment = BottomSheetLanguage()
            fragment.mSelectedLanguage = selectedLanguage
            fragment.mListeners = dialogButtonClick
            return fragment
        }
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding.layoutToolbar.btnBack -> {
                dismiss()
            }
        }
    }

    override fun onItemClick(view: View?, position: Int?, actionType: Int?, vararg objects: Any?) {
        val data = objects[0] as Language?
        data?.let { language ->
            if (mListeners != null) {
                mListeners?.invoke(language)
            } else {
                val bundle = Bundle()
                bundle.putParcelable(LANGUAGE, data)
                setFragmentResult(REQUEST_KEY, bundle)
            }
        }
        dismiss()
    }
}