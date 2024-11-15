package com.app.development.winter.shared.extension

import android.widget.FrameLayout
import com.google.android.material.snackbar.Snackbar

fun Snackbar.displaySnackBarWithBottomMargin(
    sideMargin: Int,
    marginBottom: Int
) {
    val snackBarView = view
    val params = snackBarView.layoutParams as FrameLayout.LayoutParams
    params.setMargins(
        params.leftMargin + sideMargin,
        params.topMargin,
        params.rightMargin + sideMargin,
        params.bottomMargin + marginBottom
    )
    snackBarView.layoutParams = params
    show()
}
