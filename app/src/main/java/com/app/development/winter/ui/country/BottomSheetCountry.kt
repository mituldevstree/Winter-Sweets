package com.app.development.winter.ui.country

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.app.development.winter.databinding.BottomSheetCountryBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.bottomsheetbase.BaseBottomSheetFragment
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.show
import com.app.development.winter.ui.country.model.Country
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.space.words.app.ui.country.CountryAdapter

class BottomSheetCountry : BaseBottomSheetFragment<BottomSheetCountryBinding>(
    BottomSheetCountryBinding::inflate, true, true
), IItemViewListener, View.OnClickListener {

    private var mListeners: ((String?) -> Unit)? = null
    private var mSelectedCountry: String = "India"


    override fun init() {
        super.init()
        mBinding.clickListener = this
        setCountryAdapter(getCountryList(LocalDataHelper.getUserConfig()?.countries))

        mBinding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.isNullOrEmpty().not()) mBinding.imgClose.show() else mBinding.imgClose.hide()
                mBinding.adapter?.filter?.filter(p0 ?: "")
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun setCountryAdapter(countryList: ArrayList<Country>) {
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER
        mBinding.list.layoutManager = layoutManager
        mBinding.adapter = CountryAdapter(countryList, this)
        mBinding.adapter?.setSelectedItem(mSelectedCountry)
    }

    private fun getCountryList(countries: ArrayList<String>?): ArrayList<Country> {
        val items: ArrayList<Country> = ArrayList()
        countries?.let {
            for (country in it) {
                if (country == mSelectedCountry) {
                    items.add(Country(country, "", true))
                } else {
                    items.add(Country(country, "", false))
                }
            }
        }
        return items
    }


    companion object {
        fun newInstance(
            selectedCountry: String?, listener: (String?) -> Unit
        ): BottomSheetCountry {
            val fragment = BottomSheetCountry()
            fragment.mSelectedCountry = selectedCountry ?: ""
            fragment.mListeners = listener
            return fragment
        }
    }

    override fun onClick(view: View?) {
        view?.hapticFeedbackEnabled()
        when (view) {
            mBinding.layoutToolbar.btnBack -> {
                dismiss()
            }

            mBinding.imgClose -> {
                mBinding.edtSearch.setText("")
            }
        }
    }

    override fun onItemClick(view: View?, position: Int?, actionType: Int?, vararg objects: Any?) {
        val data = objects[0] as Country?
        data?.let { country ->
            mListeners?.invoke(country.name)
        }
        dismiss()
    }
}