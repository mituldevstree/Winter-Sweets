package com.app.development.winter.shared.extension

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

val LOCALE: Locale = Locale.ENGLISH

fun Float?.toFormattedPoints(): String =
    DecimalFormat("#.#", DecimalFormatSymbols(LOCALE)).format(this ?: 0f)

fun Float?.orZero(): Float = this ?: 0f

fun Int?.orZero(): Int = this ?: 0

fun Double?.toFormattedPoints(): String = this?.toFloat().toFormattedPoints()

fun Long.toFormattedNumber(): String {
    val suffix = charArrayOf(' ', 'K', 'M', 'B', 'T', 'P', 'E')
    val log10 = log10(this.toDouble())
    val value = floor(log10).toInt()
    val base = log10.toInt() / 3
    return if (value >= 3 && base < suffix.size) {
        val prettifiedNumber = this / 10.0.pow((base * 3).toDouble())
        DecimalFormat(
            "#0.#", DecimalFormatSymbols(LOCALE)
        ).format(
            if (prettifiedNumber > 100) {
                prettifiedNumber.toInt()
            } else {
                prettifiedNumber
            }
        ) + suffix[base]
    } else {
        DecimalFormat("#,###", DecimalFormatSymbols(LOCALE)).format(this)
    }
}

fun Int.toFormattedNumber() = this.toLong().toFormattedNumber()
fun Int.prettify() = DecimalFormat("#,###.##", DecimalFormatSymbols(LOCALE)).format(this)

fun Long.toStringOrEmpty() = if (this == 0L) "" else this.toString()

fun Long?.toStringOrEmpty() = this?.toStringOrEmpty().orEmpty()

fun Int.toStringOrEmpty() = toLong().toStringOrEmpty()

fun Long.toPrettyNumber() = NumberFormat.getNumberInstance(LOCALE).format(this)
fun Int.toPrettyNumber() = toLong().toPrettyNumber()

