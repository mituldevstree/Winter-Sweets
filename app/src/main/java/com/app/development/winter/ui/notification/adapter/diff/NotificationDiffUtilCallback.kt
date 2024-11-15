package com.app.development.winter.ui.notification.adapter.diff

import androidx.recyclerview.widget.DiffUtil
import com.app.development.winter.ui.notification.model.Notification

class NotificationDiffUtilCallback : DiffUtil.ItemCallback<Notification>() {

    override fun areItemsTheSame(
        oldItem: Notification,
        newItem: Notification,
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Notification,
        newItem: Notification,
    ): Boolean {
        return oldItem.id == newItem.id
    }
}