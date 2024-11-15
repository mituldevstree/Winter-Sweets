package com.app.development.winter.shared.bottomsheetbase

import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData

abstract class BaseDialogFragment(
    @LayoutRes val layoutId: Int
) : DialogFragment(layoutId) {

    protected fun <T> LiveData<T>.observe(function: (T) -> Unit) {
        observe(viewLifecycleOwner, function)
    }
}
