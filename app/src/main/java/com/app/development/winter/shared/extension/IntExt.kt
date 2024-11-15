package com.app.development.winter.shared.extension

import android.content.Context
import android.icu.text.MessageFormat
import android.util.DisplayMetrics
import android.util.TypedValue
import com.app.development.winter.localcache.LocaleHelper
import java.util.Locale

fun Int.toBoolean(): Boolean = (this == 1)
fun Int.hasFlag(flag: Int) = flag and this == flag
fun Int.withFlag(flag: Int) = this or flag
fun Int.minusFlag(flag: Int) = this and flag.inv()

fun Int.toPx(context: Context) =
    (this * context.resources.displayMetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT

fun Float.toPx(context: Context) =
    (this * context.resources.displayMetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT

fun Float.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics
    ).toInt()
}


/**
 * This method converts device specific pixels to density independent pixels.
 */
fun Int.pxToDp(context: Context): Int {
    return (this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 */
fun Int.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
    ).toInt()
}

/**
 * This method converts sp unit to equivalent pixels, depending on device density.
 */
fun Int.spToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics
    ).toInt()
}


fun Int.toWords(): String {
    val formatter = MessageFormat(
        "{0,spellout,currency}",
        Locale(LocaleHelper.getLanguage().locale)

    )
    return formatter.format(arrayOf(this))
}
