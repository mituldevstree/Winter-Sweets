package com.app.development.winter.shared.extension

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

fun Lifecycle.doOnDestroy(action: (LifecycleOwner) -> Unit) {
    addObserver(
        LifecycleEventObserver { lifecycleOwner, event ->
            if (event == Lifecycle.Event.ON_DESTROY) action(lifecycleOwner)
        }
    )
}
