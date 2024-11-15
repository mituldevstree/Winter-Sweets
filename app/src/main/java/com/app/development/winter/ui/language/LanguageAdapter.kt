package com.app.development.winter.ui.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.development.winter.databinding.RowLanguageBinding
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.show
import com.app.development.winter.ui.language.model.Language

class LanguageAdapter(
    private var mArrayList: ArrayList<Language>, private val mListener: BottomSheetLanguage,
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    var mSelected: Language? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowLanguageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mArrayList[position]
        holder.mBinding.language = data
        if (position == 0) {
            holder.mBinding.lblAutoTranslated.invisible()
        } else {
            holder.mBinding.lblAutoTranslated.show()
        }
        holder.mBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    fun getItems(): ArrayList<Language> {
        return mArrayList
    }

    fun updateItems(myList: ArrayList<Language>) {
        mArrayList = myList
        notifyItemRangeChanged(0, mArrayList.size)
    }

    fun filterItem(filter: String?, items: ArrayList<Language>?) {
        val filteredList = items?.filter { item -> item.name.equals(filter) }
        updateItems(ArrayList(filteredList!!))
    }

    inner class ViewHolder(val mBinding: RowLanguageBinding) :
        RecyclerView.ViewHolder(mBinding.root), View.OnClickListener {
        override fun onClick(view: View) {
            view.hapticFeedbackEnabled()
            val position = bindingAdapterPosition
            view.post {
                setSelectedItem(mArrayList[position].name)
                mListener.onItemClick(
                    view, position, IItemViewListener.CLICK, mArrayList[position]
                )
            }
        }

        init {
            mBinding.root.setOnClickListener(this)
        }
    }

    fun setSelectedItem(name: String?) {
        for (country in mArrayList) {
            country.selectedCountry = (country.name == name)
            if (country.selectedCountry) {
                mSelected = country
            }
        }
    }
}