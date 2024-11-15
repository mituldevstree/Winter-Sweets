package com.app.development.winter.shared.bottomsheetbase

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.app.development.winter.R
import com.app.development.winter.shared.base.activitybase.NavigationBaseActivity
import com.app.development.winter.shared.base.viewbase.AdvancedBaseActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel

abstract class BaseBottomSheetFragment<V : ViewBinding>(
    val bindingFactory: (LayoutInflater) -> V,
    private val isExpand: Boolean = true,
    private val isFullScreen: Boolean = false,
    private val isDraggable: Boolean = false,
) : BottomSheetDialogFragment(), CoroutineScope by CoroutineScope(Main) {
    val TAG = javaClass.name
    protected var mBase: AdvancedBaseActivity<*, *, *, *>? = null
    protected var mNavigation: NavigationBaseActivity<*, *, *, *>? = null
    lateinit var mContext: Context
    val mBinding: V by lazy { bindingFactory(layoutInflater) }

    open fun onBottomSheetStateChange(bottomSheet: View, newState: Int) {}
    open fun onBottomSheetSlide(bottomSheet: View, slideOffset: Float) {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }

    open fun setVariables() {}

    open fun parseArguments() {}

    open fun resume() {}
    open fun pause() {}
    open fun init() {
        setVariables()
    }

    open fun onBackPressed() {
        dismiss()
    }

    open fun setUpViews() {
        dialog?.let {
            val dialog = it as BottomSheetDialog
            dialog.setOnShowListener {
                if (isExpand) {
                    if (isFullScreen) {
                        setupFullHeight(dialog)
                    } else {
                        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
            setBottomSheetListeners(dialog)
        }
    }

    private fun setBottomSheetListeners(dialog: BottomSheetDialog) {
        dialog.behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                onBottomSheetStateChange(bottomSheet, newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                onBottomSheetSlide(bottomSheet, slideOffset)
            }
        })
        dialog.behavior.isDraggable = isDraggable
    }

    open fun setupFullHeight(dialog: BottomSheetDialog) {
        if (isAdded.not()) return
        dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let { bottomSheet ->
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val layoutParams = bottomSheet.layoutParams
                if (layoutParams != null) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                bottomSheet.layoutParams = layoutParams
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is AdvancedBaseActivity<*, *, *, *>) mBase = context
        if (context is NavigationBaseActivity<*, *, *, *>) mNavigation = context
    }

    override fun onResume() {
        resume()
        super.onResume()
    }

    override fun onPause() {
        pause()
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        parseArguments()
        setUpViews()
        init()
        return mBinding.root
    }

    fun snackBar(msg: String, view: View? = dialog?.window?.decorView) {
        view?.let { Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show() }
    }

    fun toast(msg: String) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun showAlert(
        title: String,
        msg: String? = null,
        @DrawableRes icon: Int? = null,
        setCancelable: Boolean = true,
        buttonName: String,
        buttonClick: () -> Unit,
    ) {
        AlertDialog.Builder(mContext).setTitle(title).also {
            if (msg != null) it.setMessage(msg)
            if (icon != null) it.setIcon(icon)
        }.setCancelable(setCancelable).setNeutralButton(buttonName) { _, _ ->
            buttonClick()
        }.create().show()
    }

    override fun onDestroy() {
        this.cancel()
        super.onDestroy()
    }

    open fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "")
    }
}
