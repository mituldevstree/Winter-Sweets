package com.app.development.winter.utility

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi

object SystemBarsCompat {
    private val api: Api = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> Api31()
        Build.VERSION.SDK_INT == Build.VERSION_CODES.R -> Api30()
        else -> Api()
    }

    fun hideSystemBars(window: Window, view: View, isImmersiveStickyMode: Boolean = false) =
        api.hideSystemBars(window, view, isImmersiveStickyMode)

    fun showSystemBars(window: Window, view: View) = api.showSystemBars(window, view)

    fun hideSystemBars(window: Window, isImmersiveStickyMode: Boolean = false) =
        api.hideSystemBars(window, window.decorView, isImmersiveStickyMode)

    fun showSystemBarsIfApplied(window: Window) {
        if (areSystemBarsHidden(window)) {
            showSystemBars(window)
        }
    }

    private fun showSystemBars(window: Window) = api.showSystemBars(window, window.decorView)
    private fun areSystemBarsHidden(window: Window): Boolean =
        api.areSystemBarsHidden(window.decorView)

    fun areSystemBarsHidden(view: View): Boolean = api.areSystemBarsHidden(view)

    @Suppress("DEPRECATION")
    private open class Api {
        open fun hideSystemBars(
            window: Window, view: View, isImmersiveStickyMode: Boolean = false
        ) {
            val flags =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

            view.systemUiVisibility = if (isImmersiveStickyMode) {
                flags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            } else {
                flags or View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }

        open fun showSystemBars(window: Window, view: View) {
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            //view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        open fun areSystemBarsHidden(view: View) =
            view.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.R)
    private open class Api30 : Api() {

        open val defaultSystemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE

        override fun hideSystemBars(window: Window, view: View, isImmersiveStickyMode: Boolean) {
            window.setDecorFitsSystemWindows(false)
            view.windowInsetsController?.let {
                it.systemBarsBehavior =
                    if (isImmersiveStickyMode) WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    else defaultSystemBarsBehavior
                it.hide(WindowInsets.Type.systemBars())
            }
        }

        override fun showSystemBars(window: Window, view: View) {
            window.setDecorFitsSystemWindows(true)
            view.windowInsetsController?.show(WindowInsets.Type.systemBars())
        }

        override fun areSystemBarsHidden(view: View) =
            view.rootWindowInsets?.isVisible(WindowInsets.Type.navigationBars())?.not() ?: false
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private class Api31 : Api30() {
        override val defaultSystemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
    }
}