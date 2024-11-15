package com.app.development.winter.shared.callback

import android.view.View

interface ViewCreatedListeners {
    fun onViewCreated(view: View)
    fun onAnimationFinished()
}