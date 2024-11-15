package com.app.development.winter.shared.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.app.development.winter.R
import com.app.development.winter.shared.extension.invisible
import com.app.development.winter.shared.extension.isVisible
import com.app.development.winter.shared.extension.show
import com.app.development.winter.shared.extension.toPx


class LineProgressBar(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val margin = (0.5f).toPx(context).toInt()
    private var roundCorners = floatArrayOf(2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f)
    private var roundLeftCorners = floatArrayOf(9500f, 9500f, 2f, 2f, 2f, 2f, 9500f, 9500f)
    private var roundRightCorners = floatArrayOf(2f, 2f, 9500f, 9500f, 9500f, 9500f, 2f, 2f)
    private val progressColor = Color.parseColor("#FD5C11")
    private var isPreviousStepCompleted = false

    init {
        init()
    }

    private fun init() {
        if (childCount == 0) {
            orientation = HORIZONTAL
            setSteps(50)
        }
    }

    fun setProgress(progress: Int) {
        val progressValue = (progress / 2)
        if (progressValue >= 0 && progressValue < (childCount)) {
            setPreviousStepProgress(progressValue)
            val progressView = getChildAt(progressValue)
            if (!progressView.isVisible()) {
                progressView.show()
                //setProgressAnimation(progressView) {}
            }
        } else {
            resetProgress()
        }
    }

    private fun resetProgress() {
        (0..<childCount).forEach { index ->
            val view = getChildAt(index)
            view.clearAnimation()
            view.invisible()
        }
    }

    private fun setPreviousStepProgress(progress: Int) {
        if (isPreviousStepCompleted.not()) {
            isPreviousStepCompleted = true
            (0..<progress).forEach { index ->
                getChildAt(index).show()
            }
            if (childCount > 0) {
                getChildAt(childCount - 1).invisible()
            }
        }
    }

    fun setSteps(size: Int) {
        if (size < 2) return

        isPreviousStepCompleted = false
        removeAllViews()
        (0..<size).forEach { index ->
            val view = getProgressStepView()
            when (index) {
                0 -> {
                    view.background = getRoundedCornerDrawable(roundLeftCorners)
                }

                size - 1 -> {
                    view.background = getRoundedCornerDrawable(roundRightCorners)
                }

                else -> {
                    view.background = getRoundedCornerDrawable(roundCorners)
                }
            }
            addView(view)
        }
        resetProgress()
    }

    private fun getRoundedCornerDrawable(corners: FloatArray): Drawable {
        val shapeDrawable = ShapeDrawable().apply {
            shape = RoundRectShape(corners, null, null)
            paint.color = progressColor
        }
        return shapeDrawable
    }

    private fun getProgressStepView(): View {
        val params = LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        params.setMargins(margin, margin, margin, margin)
        params.weight = 1f
        val view = View(context)
        view.layoutParams = params
        view.invisible()
        return view
    }

    private fun setProgressAnimation(view: View, onAnimationEnd: () -> Unit) {
        val anim = AnimationUtils.loadAnimation(view.context, R.anim.line_progressbar_anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                animation?.cancel()
                view.clearAnimation()
                onAnimationEnd.invoke()
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })
        view.startAnimation(anim)
    }
}