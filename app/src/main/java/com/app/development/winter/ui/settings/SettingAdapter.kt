package com.app.development.winter.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.development.winter.databinding.RowSettingBinding
import com.app.development.winter.shared.callback.IItemViewListener
import com.app.development.winter.shared.extension.hide
import com.app.development.winter.shared.extension.show
import com.app.development.winter.ui.settings.model.Setting

class SettingAdapter(
    private var mArrayList: ArrayList<Setting>, private val mListener: IItemViewListener?
) : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowSettingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mArrayList[position]
        holder.mBinding.setting = data
        if (mArrayList.size - 1 == position) {
            holder.mBinding.divider.hide()
        } else {
            holder.mBinding.divider.show()
        }
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    inner class ViewHolder(val mBinding: RowSettingBinding) :
        RecyclerView.ViewHolder(mBinding.root), View.OnClickListener {
        override fun onClick(view: View) {
            mListener?.onItemClick(
                view,
                position = bindingAdapterPosition,
                IItemViewListener.CLICK,
                mArrayList[bindingAdapterPosition]
            )
        }

        init {
            mBinding.root.setOnClickListener(this)
        }
    }
}