package com.app.development.winter.shared.views.carouselpager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.PagerAdapter
import com.app.development.winter.R
import com.app.development.winter.shared.extension.loadImage
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.app.development.winter.shared.views.carouselpager.model.AvatarModel

class CarouselViewPagerAdapter(private val mDataList: MutableList<AvatarModel>) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any =
        LayoutInflater.from(container.context).inflate(R.layout.row_avatar, container, false)
            .also { layout ->
                val imageView = layout.findViewById<AppCompatImageView>(R.id.ivUser)
                val circularProgressIndicator =
                    layout.findViewById<CircularProgressIndicator>(R.id.progress)
                circularProgressIndicator.hide()
                if (mDataList[position].icon == 0) {
                    val url = mDataList[position].id ?: ""
                    if (url.contains("http:") || url.contains("https:")) {
                        imageView.loadImage(mDataList[position].id, 0)
                    }
                } else {
                    imageView.setImageResource(mDataList[position].icon ?: 0)
                }
                container.addView(layout)
            }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }

    override fun getCount(): Int {
        return mDataList.size
    }

    fun getSelectedItem(index: Int): AvatarModel {
        return mDataList[index]
    }

    fun getSelectedPhoto(index: Int): String? {
        return mDataList[index].id
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view === o
    }
}