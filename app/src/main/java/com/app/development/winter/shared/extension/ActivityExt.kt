package com.app.development.winter.shared.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.development.winter.R
import com.app.development.winter.localcache.LocaleHelper
import com.app.development.winter.shared.base.BetterActivityResult
import com.app.development.winter.utility.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun ComponentActivity.launchWithLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(minActiveState) {
            block()
        }
    }
}

val Activity.screenWidth: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
            metrics.bounds.width() - insets.left - insets.right
        } else {
            val view = window.decorView
            val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets, view)
                .getInsets(WindowInsetsCompat.Type.systemBars())
            resources.displayMetrics.widthPixels - insets.left - insets.right
        }
    }

fun Activity.isNotAdsActivity(): Boolean {
    Log.e(
        "ADTYPE",
        "${com.app.development.winter.application.Controller.foregroundActivity?.localClassName}--${this.localClassName}"
    )
    return com.app.development.winter.application.Controller.foregroundActivity == this
}

fun Activity.openAppByPackageName(packageNameToOpen: String?) {
    packageNameToOpen?.let {
        val startIntent = packageManager.getLaunchIntentForPackage(packageNameToOpen)
        if (startIntent != null) {
            startActivity(startIntent)
        }
    }
}

fun Activity.killAndRestartApp() {
    val ctx = baseContext
    LocaleHelper.setLocale(ctx, LocaleHelper.getLanguage().locale)
    val pm = ctx.packageManager
    val intent = pm.getLaunchIntentForPackage(ctx.packageName)
    val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
    ctx.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}

val Activity.screenHeight: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
            metrics.bounds.height() - insets.bottom - insets.top
        } else {
            val view = window.decorView
            val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets, view)
                .getInsets(WindowInsetsCompat.Type.systemBars())
            resources.displayMetrics.heightPixels - insets.bottom - insets.top
        }
    }

fun Activity.openAppSetting() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}


fun Activity.toast(message: String?) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.toastHtml(message: String?) {
    Toast.makeText(
        this,
        HtmlCompat.fromHtml("<big>$message</big>", HtmlCompat.FROM_HTML_MODE_LEGACY),
        Toast.LENGTH_SHORT
    ).show()
}

fun Activity.setNavigationBarColor(color: Int) {
    window?.let { it.navigationBarColor = color }
}

fun Activity.setStatusBarColor(color: Int) {
    window?.let { it.statusBarColor = color }
}

private var hasPermissionDialogShown = false
fun Activity.hasOverlayPermission(
    shouldShowAgainOnCancel: Boolean = false,
    shouldShowAgainOnBackWithoutPermission: Boolean = false,
    activityLauncher: BetterActivityResult<Intent, ActivityResult>?,
    onPermissionGranted: (Boolean) -> Unit
) {
    try {
        if (!isFinishing && !isDestroyed) {
            if (!hasOverLayPermissionGranted()) {
                if (hasPermissionDialogShown) return

                hasPermissionDialogShown = true
                showAlertDialog(titleRes = R.string.required_permission,
                    messageRes = R.string.overlay_permission_message,
                    positiveRes = R.string.grant,
                    negativeRes = R.string.cancel,
                    showClose = false,
                    cancelable = false,
                    positiveClick = {
                        hasPermissionDialogShown = false
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + applicationContext.packageName)
                        )
                        activityLauncher?.launch(intent) {
                            if (it.resultCode == Activity.RESULT_OK && Util.isHuaweiDevice()) {
                                onPermissionGranted.invoke(true)
                            } else {
                                if (!Settings.canDrawOverlays(this)) {
                                    if (shouldShowAgainOnBackWithoutPermission) {
                                        hasOverlayPermission(
                                            shouldShowAgainOnCancel,
                                            shouldShowAgainOnBackWithoutPermission,
                                            activityLauncher,
                                            onPermissionGranted
                                        )
                                    } else {
                                        onPermissionGranted.invoke(false)
                                    }
                                } else {
                                    onPermissionGranted.invoke(true)
                                }
                            }
                        }
                    },
                    negativeClick = {
                        hasPermissionDialogShown = false
                        if (shouldShowAgainOnCancel) {
                            hasOverlayPermission(
                                shouldShowAgainOnCancel,
                                shouldShowAgainOnBackWithoutPermission,
                                activityLauncher,
                                onPermissionGranted
                            )
                        } else {
                            onPermissionGranted.invoke(false)
                        }
                    },
                    onCancel = {
                        hasPermissionDialogShown = false
                        if (shouldShowAgainOnCancel) {
                            hasOverlayPermission(
                                shouldShowAgainOnCancel,
                                shouldShowAgainOnBackWithoutPermission,
                                activityLauncher,
                                onPermissionGranted
                            )
                        }
                    })
            } else {
                hasPermissionDialogShown = false
                onPermissionGranted.invoke(true)
            }
        }
    } catch (e: ActivityNotFoundException) {
        onPermissionGranted.invoke(false)
        e.printStackTrace()
    }
}

