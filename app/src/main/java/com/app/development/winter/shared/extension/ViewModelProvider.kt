package com.app.development.winter.shared.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

fun <T : ViewModel> AppCompatActivity.viewModelProvider(
    factory: ViewModelProvider.Factory?, model: KClass<T>,
): T {
    return if (factory == null) {
        ViewModelProvider(this)[model.java]
    } else {
        ViewModelProvider(this, factory)[model.java]
    }
}

fun <T : ViewModel> Fragment.viewModelProvider(
    factory: ViewModelProvider.Factory? = null, model: KClass<T>,
): T {
    return if (factory == null) {
        ViewModelProvider(this)[model.java]
    } else {
        ViewModelProvider(this, factory)[model.java]
    }
}

fun Boolean.runIfTrue(block: () -> Unit) {
    if (this) {
        block()
    }
}

