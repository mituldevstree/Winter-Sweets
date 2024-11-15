package com.app.development.winter.shared.extension

import java.util.Locale

const val HASHTAG_SYMBOL = '#'
val HASHTAG_SYMBOLS_ARRAY = arrayOf("\u0023\uFE0F\u20E3", ":hash:", HASHTAG_SYMBOL.toString())

fun CharSequence?.isNotNullOrEmpty() = !isNullOrEmpty()
fun CharSequence?.isNotNullOrBlank() = !isNullOrBlank()

fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }



fun String.countMatches(pattern: String): Int {
    if (pattern.isEmpty()) return 0
    var index = 0
    var count = 0

    while (true) {
        index = this.indexOf(pattern, index)
        index += if (index != -1) {
            count++
            pattern.length
        } else {
            return count
        }
    }
}
fun String?.getTextWithoutSpace(): String {
    val stringBuilder = StringBuilder()
    this?.forEach {
        if (it.isDigit()) {
            stringBuilder.append(it)
        }
    }
    return stringBuilder.toString()
}

fun String.getHashtags(): List<String> {
    if (this.isEmpty() || !this.contains(HASHTAG_SYMBOL)) return emptyList()
    return split(*HASHTAG_SYMBOLS_ARRAY).filter { it.isNotEmpty() }.map { it.trim() }
}

inline fun String?.ifNullOrBlank(defaultValue: () -> String): String =
    if (this == null || isBlank()) defaultValue() else this

fun String.toCountryName(): String {
    return Locale("", this.uppercase()).getDisplayCountry(Locale.US)
}

fun String.toLanguageName(): String {
    return Locale(this.uppercase()).getDisplayLanguage(Locale.US)
}

fun String.removeAtSign(): String {
    return replace("@", "")
}
