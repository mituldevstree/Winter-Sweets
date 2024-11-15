package com.app.development.winter.ui.leaderboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.development.winter.databinding.RowLeaderBoardUserBinding
import com.app.development.winter.ui.leaderboard.model.LeaderBoardUser

class LeaderboardUserAdapter(private var mArrayList: MutableList<LeaderBoardUser>) :
    RecyclerView.Adapter<LeaderboardUserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowLeaderBoardUserBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mArrayList[position]
        holder.mBinding.user = data
        holder.mBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    fun getItems(): MutableList<LeaderBoardUser> {
        return mArrayList
    }

    fun updateItems(myList: MutableList<LeaderBoardUser>) {
        mArrayList = myList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mBinding: RowLeaderBoardUserBinding) :
        RecyclerView.ViewHolder(mBinding.root)

}