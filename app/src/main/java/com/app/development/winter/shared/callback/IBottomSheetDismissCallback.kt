package com.app.development.winter.shared.callback

import android.view.View

interface IBottomSheetDismissCallback {

    fun onDismissListener(view: View?, vararg objects: Any?)
}