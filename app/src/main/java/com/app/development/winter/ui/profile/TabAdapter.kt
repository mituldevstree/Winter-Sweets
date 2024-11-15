package com.app.development.winter.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.development.winter.databinding.RowTabBinding
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.hapticFeedbackEnabled
import com.app.development.winter.ui.profile.model.TabModel

class TabAdapter(
    private var mArrayList: ArrayList<TabModel>, private val mListener: IItemViewListener?
) : RecyclerView.Adapter<TabAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mArrayList[position]
        holder.mBinding.tabModel = data
        holder.mBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    fun getItems(): ArrayList<TabModel> {
        return mArrayList
    }

    fun updateItems(myList: ArrayList<TabModel>) {
        mArrayList = myList
        notifyItemRangeChanged(0, mArrayList.size)
    }

    inner class ViewHolder(val mBinding: RowTabBinding) : RecyclerView.ViewHolder(mBinding.root),
        View.OnClickListener {
        override fun onClick(view: View) {
            view.hapticFeedbackEnabled()
            val position = bindingAdapterPosition
            val item = mArrayList[position]
            if (!item.selectedTab) {
                view.post {
                    setSelectedItem(item.name)
                    mListener?.onItemClick(view, position, IItemViewListener.CLICK, item)
                }
            }
        }

        init {
            mBinding.root.setOnClickListener(this)
        }
    }

    fun setSelectedItem(name: String?) {
        mArrayList.forEach { item ->
            item.selectedTab = (item.name.equals(name, true))
        }
    }
}