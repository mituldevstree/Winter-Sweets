package com.app.development.winter.utility

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

class KeyboardUtil(act: FragmentActivity, contentView: View) {
    private var decorView: View? = null
    private var contentView: View? = null

    //a small helper to allow showing the editText focus
    private var onGlobalLayoutListener = OnGlobalLayoutListener {
        val r = Rect()
        //r will be populated with the coordinates of your view that area still visible.
        decorView?.getWindowVisibleDisplayFrame(r)

        //get screen height and calculate the difference with the useable area from the r
        val height = decorView?.context?.resources?.displayMetrics?.heightPixels ?: 1
        val diff = height - r.bottom

        //if it could be a keyboard add the padding to the view
        if (diff != 0) {
            // if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
            //check if the padding is 0 (if yes set the padding for the keyboard)
            if (contentView.paddingBottom != diff) {
                //set the padding of the contentView for the keyboard
                contentView.setPadding(0, 0, 0, diff)
            }
        } else {
            //check if the padding is != 0 (if yes reset the padding)
            if (contentView.paddingBottom != 0) {
                //reset the padding of the contentView
                contentView.setPadding(0, 0, 0, 0)
            }
        }
    }

    init {
        decorView = act.window.decorView
        this.contentView = contentView

        //only required on newer android versions. it was working on API level 19
        decorView?.viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    fun enable() {
        decorView?.viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    fun disable() {
        decorView?.viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    companion object {
        /**
         * Helper to hide the keyboard
         *
         * @param act
         */
        fun hideKeyboard(act: Activity?) {
            if (act != null && act.currentFocus != null) {
                val inputMethodManager =
                    act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(act.currentFocus!!.windowToken, 0)
            }
        }
    }
}