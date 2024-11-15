package com.app.development.winter.shared.extension

fun Boolean.toInt() = if (this) 1 else 0

fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.orTrue(): Boolean = this ?: true
