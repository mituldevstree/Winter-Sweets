package com.space.words.app.ui.country

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.app.development.winter.databinding.RowCountryBinding
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.ui.country.model.Country

class CountryAdapter(
    private var mArrayList: ArrayList<Country>, private val mListener: IItemViewListener?
) : RecyclerView.Adapter<CountryAdapter.ViewHolder>(), Filterable {
    var mSelected: Country? = null
    var mFilteredArrayList: ArrayList<Country> = mArrayList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowCountryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mFilteredArrayList[position]
        holder.mBinding.country = data
        holder.mBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mFilteredArrayList.size
    }

    fun getItems(): ArrayList<Country> {
        return mFilteredArrayList
    }

    fun updateItems(myList: ArrayList<Country>) {
        mFilteredArrayList = myList
        notifyDataSetChanged()
    }

    fun filterItem(filter: String?, items: ArrayList<Country>?) {
        val filteredList = items?.filter { item -> item.name?.equals(filter) == true }
        updateItems(ArrayList(filteredList!!))
    }

    inner class ViewHolder(val mBinding: RowCountryBinding) :
        RecyclerView.ViewHolder(mBinding.root), View.OnClickListener {
        override fun onClick(view: View) {
            view.post {
                val position = bindingAdapterPosition
                setSelectedItem(mFilteredArrayList[position].name)
                mListener?.onItemClick(
                    view, position, IItemViewListener.CLICK, mFilteredArrayList[position]
                )
            }
        }

        init {
            mBinding.root.setOnClickListener(this)
        }
    }

    fun setSelectedItem(name: String?) {
        for (country in mFilteredArrayList) {
            country.selectedLanguage = (country.name == name)
            if (country.selectedLanguage) {
                mSelected = country
            }
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = mutableListOf<Country>()
            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(mArrayList)
            } else {
                for (item in mArrayList) {
                    if (item.name?.lowercase()
                            ?.startsWith(constraint.toString().lowercase()) == true
                    ) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            updateItems((filterResults?.values as MutableList<Country>) as ArrayList<Country>)
        }
    }
}