package com.app.development.winter.shared.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

@ColorInt
fun View.getColor(@ColorRes colorRes: Int) = context.getColorCompat(colorRes)
fun View.getColorList(@ColorRes res: Int) = context.getColorListCompat(res)
fun View.getDrawable(@DrawableRes res: Int) = context.getDrawableCompat(res)

@ColorInt
fun View.getThemeColor(@AttrRes res: Int) = context.getThemeColor(res)

fun View.clickOnRandomPosOfTheScreen() {
    val foregroundView = this ?: return
    val coordinates = foregroundView.getBottomRandomCoordinates()
    val x = coordinates.first
    val y = coordinates.second

    val motionEventDown = MotionEvent.obtain(
        System.currentTimeMillis(),
        System.currentTimeMillis() + 100,
        MotionEvent.ACTION_DOWN,
        x,
        y,
        0
    )
    val motionEventUp = MotionEvent.obtain(
        System.currentTimeMillis(),
        System.currentTimeMillis() + 100,
        MotionEvent.ACTION_UP,
        x,
        y,
        0
    )

    foregroundView.dispatchTouchEvent(motionEventDown)
    foregroundView.dispatchTouchEvent(motionEventUp)
    android.util.Log.d("AutoClick", "Auto click at X: $x, Y: $y")
}

private fun View.getRandomCoordinates(): Pair<Float, Float> {
    val screenWidth = this.width.toFloat()
    val screenHeight = this.height.toFloat()

    val randomX = (Math.random() * screenWidth).toFloat()
    val randomY = (Math.random() * screenHeight).toFloat()

    return Pair(randomX, randomY)
}

private fun View.getBottomRandomCoordinates(): Pair<Float, Float> {
    val screenWidth = this.width.toFloat() / 4
    val screenHeight = this.height.toFloat() / 20
    val bottomSpace = (this.height.toFloat() - screenHeight * 2)
//    Log.wtf("NEDKO_WTF", "getBottomRandomCoordinates height = ${this.height.toFloat()}, 1/20 = $screenHeight, bottom space = $bottomSpace")

    val randomX = screenWidth + Random.nextInt(-100, 100)
    val randomY = bottomSpace + Random.nextInt(-70, 50)
//    Log.wtf("NEDKO_WTF", "getBottomRandomCoordinates randomX = $randomX && randomY = $randomY")

    return Pair(randomX, randomY)
}


fun View.showTopSnackBar(
    @StringRes messageRes: Int,
    @BaseTransientBottomBar.Duration length: Int,
) {
    Snackbar.make(this, messageRes, length).apply {
        if (view.layoutParams is CoordinatorLayout.LayoutParams) {
            val params = view.layoutParams as CoordinatorLayout.LayoutParams
            params.gravity = Gravity.TOP
            view.layoutParams = params
        } else if (view.layoutParams is FrameLayout.LayoutParams) {
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            view.layoutParams = params
        }
        animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        show()
    }
}

fun TextView.showStrikeThrough(show: Boolean) {
    paintFlags = if (show) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}


val ViewGroup.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)


fun View.createBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).apply {
        background?.draw(this) ?: this.drawColor(Color.WHITE)
        draw(this)
    }
    return bitmap
}

fun View.addSystemWindowInsetToPadding(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
) {
    val (initialLeft, initialTop, initialRight, initialBottom) =
        listOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        view.updatePadding(
            left = initialLeft + (if (left) insets.getInsets(WindowInsetsCompat.Type.systemBars()).left else 0),
            top = initialTop + (if (top) insets.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0),
            right = initialRight + (if (right) insets.getInsets(WindowInsetsCompat.Type.systemBars()).right else 0),
            bottom = initialBottom + (if (bottom) insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom else 0)
        )

        insets
    }
}

fun View.addSystemWindowInsetToMargin(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
) {
    val (initialLeft, initialTop, initialRight, initialBottom) =
        listOf(marginLeft, marginTop, marginRight, marginBottom)
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        view.updateLayoutParams {
            (this as? ViewGroup.MarginLayoutParams)?.let {
                updateMargins(
                    left = initialLeft + (if (left) insets.getInsets(WindowInsetsCompat.Type.systemBars()).left else 0),
                    top = initialTop + (if (top) insets.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0),
                    right = initialRight + (if (right) insets.getInsets(WindowInsetsCompat.Type.systemBars()).right else 0),
                    bottom = initialBottom + (if (bottom) insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom else 0)
                )
            }
        }

        insets
    }
}
