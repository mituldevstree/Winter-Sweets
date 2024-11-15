package com.app.development.winter.shared.views.carouselpager

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.app.development.winter.R
import kotlin.math.abs

class CarouselViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var mOnItemClickListener: OnItemClickListener? = null
    private var mScaleFactor = 0.5f
    private var mItemSpace = 1.75f
    private var mHasAlphaOnItem = true

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    init {
        readRadiusAttr(context, attrs)
        setup()
    }

    private fun readRadiusAttr(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CarouselView, 0, 0)
        try {
            setScaleFactor(a.getFloat(R.styleable.CarouselView_scaleFactor, 0.5f))
            setItemSpace(a.getFloat(R.styleable.CarouselView_itemSpace, 1.75f))
            setAlphaOnItem(a.getBoolean(R.styleable.CarouselView_hasAlphaOnItem, true))
        } finally {
            a.recycle()
        }
    }

    fun setScaleFactor(scaleFactor: Float) {
        mScaleFactor = scaleFactor
    }

    fun setItemSpace(itemSpace: Float) {
        mItemSpace = itemSpace
    }

    fun setAlphaOnItem(hasAlphaOnItem: Boolean) {
        mHasAlphaOnItem = hasAlphaOnItem
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setup() {
        val tapGestureDetector = GestureDetector(context, TapGestureListener())
        setOnTouchListener { v, event ->
            tapGestureDetector.onTouchEvent(event!!)
            false
        }
        setPageTransformer(true, GalleryTransformer(mScaleFactor, mItemSpace, mHasAlphaOnItem))
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        super.setAdapter(adapter)
        // offset first element so that we can scroll to the left
        currentItem = 0
    }

    override fun setCurrentItem(item: Int) {
        // offset the current item to ensure there is space to scroll
        setCurrentItem(item, false)
    }

    fun animatePreviousPage() {
        super.setCurrentItem(super.getCurrentItem() + 1, true)
    }

    fun animateNextPage() {
        super.setCurrentItem(super.getCurrentItem() - 1, true)
    }

    override fun setCurrentItem(index: Int, smoothScroll: Boolean) {
        var item = index
        if (adapter != null) {
            if (adapter?.count == 0) {
                super.setCurrentItem(item, smoothScroll)
                return
            }
            item = getOffsetAmount() + item % (adapter?.count ?: 0)
        }
        super.setCurrentItem(item, smoothScroll)
    }

    override fun getCurrentItem(): Int {
        if (adapter == null) return super.getCurrentItem()
        if (adapter?.count == 0) {
            return super.getCurrentItem()
        }
        val position = super.getCurrentItem()
        return if (adapter is InfinitePagerAdapter) {
            val infAdapter = adapter as InfinitePagerAdapter?
            // Return the actual item position in the data backing InfinitePagerAdapter
            position % (infAdapter?.getRealCount() ?: 0)
        } else {
            super.getCurrentItem()
        }
    }

    fun getRealCurrentItem(): Int {
        return super.getCurrentItem()
    }

    fun setRealCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(item, smoothScroll)
    }

    fun setRealCurrentItem(item: Int) {
        super.setCurrentItem(item)
    }

    private fun getOffsetAmount(): Int {
        if (adapter == null) return 0
        if (adapter?.count == 0) {
            return 0
        }
        return if (adapter is InfinitePagerAdapter) {
            val infAdapter = adapter as InfinitePagerAdapter?
            // allow for 100 back cycles from the beginning
            // should be enough to create an illusion of infinity
            // warning: scrolling to very high values (1,000,000+) results in
            // strange drawing behaviour
            (infAdapter?.getRealCount() ?: 0) * 1000
        } else {
            0
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        mOnItemClickListener = onItemClickListener
    }


    private inner class TapGestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (mOnItemClickListener != null) {
                mOnItemClickListener?.onItemClick(currentItem)
            }
            return true
        }
    }

    private inner class GalleryTransformer(
        private val scale: Float, private val itemSpace: Float, private val isAppliedAlpha: Boolean
    ) : PageTransformer {
        override fun transformPage(view: View, position: Float) {
            val scaleValue = 1 - abs(position) * scale
            view.scaleX = scaleValue
            view.scaleY = scaleValue

            //if (isAppliedAlpha) {
            view.alpha = scaleValue
            //}
            view.pivotX =
                view.width * (1f - position - if (position > 0) itemSpace else -(itemSpace)) * scale
            view.pivotY = view.height / 2f
            view.elevation = 1f - abs(position)
        }
    }
}