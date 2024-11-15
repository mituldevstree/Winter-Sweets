package com.app.development.winter.ui.notification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.development.winter.databinding.RowNotificationBinding
import com.app.development.winter.shared.extension.fromHtml
import com.app.development.winter.ui.notification.adapter.diff.NotificationDiffUtilCallback
import com.app.development.winter.ui.notification.model.Notification
import com.app.development.winter.utility.DateTimeUtil

class NotificationAdapter() :
    PagingDataAdapter<Notification, NotificationAdapter.ViewHolder>(NotificationDiffUtilCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            RowNotificationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)
    }

    inner class ViewHolder(val binding: RowNotificationBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        override fun onClick(view: View) {
        }

        fun bind(data: Notification?) {
            binding.notification = data
            binding.txtDesc.text = data?.body?.fromHtml()
            manageDateTimeView(bindingAdapterPosition, data, binding.txtTime) { isShowLayout ->
                binding.sectionHeader.isVisible = isShowLayout
            }
            binding.executePendingBindings()
        }

        private fun manageDateTimeView(
            position: Int,
            data: Notification?,
            textView: AppCompatTextView,
            callback: (isShowLayout: Boolean) -> Unit
        ) {
            textView.text = DateTimeUtil.getRelativeDateTime(textView.context, data?.date)
            if (position > 0) {
                val prevItem = snapshot()[position - 1]
                prevItem?.date?.let { date ->
                    if (data?.checkMessageDateIsBigger(date)?.not() == true) {
                        callback.invoke(true)
                    } else {
                        callback.invoke(false)
                    }
                }
            } else {
                callback.invoke(true)
            }
        }

        init {
            binding.root.setOnClickListener(this)
        }
    }
}