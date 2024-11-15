package com.app.development.winter.shared.extension

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavArgs
import androidx.navigation.NavArgsLazy
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.app.development.winter.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

const val UNDEFINED_RES = 0
val DOB_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())/*fun Fragment.setupToolbar(
    @IdRes toolbarId: Int = R.id.toolbar,
    @StringRes titleRes: Int = -1,
    title: CharSequence? = null,
    homeEnabled: Boolean = false,
    homeAsUpEnabled: Boolean = false,
    homeAction: (() -> Unit) = { requireActivity().onBackPressed() },
) {
    val toolbar = view?.findViewById<Toolbar>(toolbarId) ?: return
    val activity = requireActivity()
    if (activity is AppCompatActivity) {
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(homeEnabled)
            setHomeButtonEnabled(homeAsUpEnabled)
        }
    }
    toolbar.setNavigationOnClickListener { homeAction() }
    if (titleRes != -1) {
        activity.setTitle(titleRes)
    }
    if (title != null) {
        if (activity is AppCompatActivity) {
            activity.supportActionBar?.title = title
        } else {
            activity.title = title
        }
    }
}*/

fun Fragment.clearToolbar() {
    val activity = requireActivity()
    if (activity is AppCompatActivity) {
        activity.setSupportActionBar(null)
    }
}

@ColorInt
fun Fragment.getColor(@ColorRes colorRes: Int) = requireContext().getColorCompat(colorRes)
fun Fragment.getColorList(@ColorRes res: Int) = requireContext().getColorListCompat(res)
fun Fragment.getDrawable(@DrawableRes res: Int) = requireContext().getDrawableCompat(res)

@ColorInt
fun Fragment.getThemeColor(@AttrRes res: Int) = requireContext().getThemeColor(res)


const val NAVIGATION_RESULT_KEY = "navigation_result_key"

fun <T> Fragment.setNavigationResult(
    key: String = NAVIGATION_RESULT_KEY,
    value: T,
    navController: NavController = findNavController(),
    @IdRes entryId: Int = -1,
) {
    val entry = if (entryId == -1) {
        navController.previousBackStackEntry
    } else {
        navController.getBackStackEntry(entryId)
    }
    entry?.savedStateHandle?.set(key, value)
}

fun <T> Fragment.getNavigationResult(
    @IdRes id: Int = -1,
    key: String = NAVIGATION_RESULT_KEY,
    navController: NavController = findNavController(),
    onResult: (result: T) -> Unit,
) {
    val navBackStackEntry = if (id == -1) {
        navController.currentBackStackEntry ?: return
    } else {
        navController.getBackStackEntry(id)
    }

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME && navBackStackEntry.savedStateHandle.contains(key)) {
            val result = navBackStackEntry.savedStateHandle.get<T>(key)
            result?.let(onResult)
            navBackStackEntry.savedStateHandle.remove<T>(key)
        }
    }
    navBackStackEntry.lifecycle.addObserver(observer)

    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            navBackStackEntry.lifecycle.removeObserver(observer)
        }
    })
}

fun <T> Fragment.observeNavigationResult(
    @IdRes id: Int = -1,
    key: String = NAVIGATION_RESULT_KEY,
    navController: NavController = findNavController(),
    onResult: (result: T) -> Unit,
) {
    val navBackStackEntry = if (id == -1) {
        navController.currentBackStackEntry ?: return
    } else {
        navController.getBackStackEntry(id)
    }

    val dataObserver = Observer<T> { result ->
        if (result != null) {
            onResult(result)
            navBackStackEntry.savedStateHandle.set(key, null)
        }
    }

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            navBackStackEntry.savedStateHandle.getLiveData<T>(key).observeForever(dataObserver)
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            navBackStackEntry.savedStateHandle.getLiveData<T>(key).removeObserver(dataObserver)
        }
    }
    navBackStackEntry.lifecycle.addObserver(observer)

    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            navBackStackEntry.lifecycle.removeObserver(observer)
        }
    })
}

@Suppress("LongParameterList")
fun Fragment.createAlertDialog(
    @StringRes titleRes: Int = UNDEFINED_RES,
    @StringRes subtitleRes: Int = UNDEFINED_RES,
    @StringRes messageRes: Int = UNDEFINED_RES,
    title: String? = null,
    subtitle: String? = null,
    message: String? = null,
    @StringRes positiveRes: Int = R.string.ok,
    positiveClick: ((itf: DialogInterface) -> Unit)? = null,
    @StringRes negativeRes: Int = UNDEFINED_RES,
    negativeClick: ((itf: DialogInterface) -> Unit)? = null,
    cancelable: Boolean = true,
    showClose: Boolean = false,
    onCancel: (() -> Unit)? = null,
    @DrawableRes imageRes: Int = UNDEFINED_RES,
    imageUrl: String? = null,
    customView: View? = null,
    iconShape: DialogIconShape = DialogIconShape.Default,
    positiveButtonStyle: DialogButtonStyle = DialogButtonStyle.Default,
    buttonLayoutStyle: DialogLayoutStyle = DialogLayoutStyle.Horizontal,
    onDismiss: (() -> Unit)? = null,
    titleClick: ((String) -> Unit)? = null,
) = requireContext().createAlertDialog(
    titleRes = titleRes,
    subtitleRes = subtitleRes,
    messageRes = messageRes,
    title = title,
    subtitle = subtitle,
    message = message,
    positiveRes = positiveRes,
    positiveClick = positiveClick,
    negativeRes = negativeRes,
    negativeClick = negativeClick,
    cancelable = cancelable,
    showClose = showClose,
    onCancel = onCancel,
    imageRes = imageRes,
    imageUrl = imageUrl,
    customView = customView,
    iconShape = iconShape,
    buttonLayoutStyle = buttonLayoutStyle,
    onDismiss = onDismiss,
    titleClick = titleClick
)

@Suppress("LongParameterList")
fun Fragment.showAlertDialog(
    @StringRes titleRes: Int = UNDEFINED_RES,
    @StringRes subtitleRes: Int = UNDEFINED_RES,
    @StringRes messageRes: Int = UNDEFINED_RES,
    title: String? = null,
    subtitle: String? = null,
    message: String? = null,
    @StringRes positiveRes: Int = R.string.ok,
    positiveClick: ((itf: DialogInterface) -> Unit)? = null,
    @StringRes negativeRes: Int = UNDEFINED_RES,
    negativeClick: ((itf: DialogInterface) -> Unit)? = null,
    cancelable: Boolean = true,
    showClose: Boolean = false,
    onCancel: (() -> Unit)? = null,
    @DrawableRes imageRes: Int = R.drawable.app_logo,
    @DrawableRes imageShadowRes: Int = UNDEFINED_RES,
    imageUrl: String? = null,
    customView: View? = null,
    iconShape: DialogIconShape = DialogIconShape.Default,
    positiveButtonStyle: DialogButtonStyle = DialogButtonStyle.Default,
    buttonLayoutStyle: DialogLayoutStyle = DialogLayoutStyle.Horizontal,
    onDismiss: (() -> Unit)? = null,
    titleClick: ((String) -> Unit)? = null,
) = requireContext().showAlertDialog(
    titleRes = titleRes,
    subtitleRes = subtitleRes,
    messageRes = messageRes,
    title = title,
    subtitle = subtitle,
    message = message,
    positiveRes = positiveRes,
    positiveClick = positiveClick,
    negativeRes = negativeRes,
    negativeClick = negativeClick,
    cancelable = cancelable,
    showClose = showClose,
    onCancel = onCancel,
    imageRes = imageRes,
    imageShadowRes = imageShadowRes,
    imageUrl = imageUrl,
    customView = customView,
    iconShape = iconShape,
    positiveButtonStyle = positiveButtonStyle,
    buttonLayoutStyle = buttonLayoutStyle,
    onDismiss = onDismiss,
    titleClick = titleClick
)

/**
 * System dialog for edge cases
 */
@Suppress("LongParameterList")
fun Fragment.showSystemAlertDialog(
    @StringRes titleRes: Int = -1,
    @StringRes messageRes: Int = -1,
    title: String? = null,
    message: String? = null,
    @StringRes positiveRes: Int = R.string.ok,
    positiveClick: ((itf: DialogInterface) -> Unit)? = null,
    @StringRes negativeRes: Int = -1,
    negativeClick: ((itf: DialogInterface) -> Unit)? = null,
    cancelable: Boolean = true,
    onCancel: (() -> Unit)? = null,
    customView: View? = null,
    @LayoutRes customViewRes: Int = -1,
): AlertDialog = requireContext().showSystemAlertDialog(
    titleRes = titleRes,
    messageRes = messageRes,
    title = title,
    message = message,
    positiveRes = positiveRes,
    positiveClick = positiveClick,
    negativeRes = negativeRes,
    negativeClick = negativeClick,
    cancelable = cancelable,
    onCancel = onCancel,
    customView = customView,
    customViewRes = customViewRes
)

inline fun <reified Args : NavArgs> SavedStateHandle.navArgs() = NavArgsLazy(Args::class) {
    val bundle = Bundle()
    keys().forEach {
        val value = get<Any>(it)
        if (value is Serializable) {
            bundle.putSerializable(it, value)
        } else if (value is Parcelable) {
            bundle.putParcelable(it, value)
        }
    }
    bundle
}

fun Fragment.setFullscreen() {
    requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

val Fragment.requestManager: RequestManager
    get() = Glide.with(this)

fun Fragment.hasPermissions(vararg permissions: String): Boolean {
    return requireContext().hasPermissions(*permissions)
}

fun Context.hasPermissions(vararg permissions: String): Boolean {
    return permissions.all { permission ->
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permission == Manifest.permission.READ_EXTERNAL_STORAGE || permission == Manifest.permission.WRITE_EXTERNAL_STORAGE -> true

            else -> checkSelfPermission(this, permission) == PERMISSION_GRANTED
        }
    }
}


@Deprecated("Deprecated", ReplaceWith("getCountryFlag(iso2)"))
fun Fragment.getFlagIcon(iso2: String?): Drawable? {
    return requireContext().getFlagIcon(iso2)
}

@Deprecated("Deprecated", ReplaceWith("getCountryFlag(iso2)"))
fun Context.getFlagIcon(iso2: String?): Drawable? {
    return try {
        ContextCompat.getDrawable(
            this, this.resources.getIdentifier(
                "flag_$iso2", "drawable", this.packageName
            )
        )
    } catch (e: Exception) {
        null
    }
}

fun Fragment.getCountryFlag(countryCode: String): Drawable {
    return requireContext().getCountryFlag(countryCode)
}

fun Context.getCountryFlag(countryCode: String): Drawable {
    return try {
        getDrawableCompat(getCountryFlagRes(countryCode))
    } catch (e: Exception) {
        BitmapDrawable(resources, Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888))
    }
}

@DrawableRes
fun Context.getCountryFlagRes(countryCode: String): Int {
    return try {
        resources.getIdentifier("flag_${countryCode.lowercase()}", "drawable", packageName)
    } catch (e: Exception) {
        0
    }
}

fun Context.getFlagIconRes(iso2: String?): Drawable? {
    return try {
        ContextCompat.getDrawable(
            this, this.resources.getIdentifier(
                "flag_$iso2", "drawable", this.packageName
            )
        )
    } catch (e: Exception) {
        null
    }
}


fun Fragment.isFragmentInBackStack(destinationId: Int) = try {
    findNavController().getBackStackEntry(destinationId)
    true
} catch (e: Exception) {
    false
}

fun <T : Fragment> T.withArguments(vararg pairs: Pair<String, Any?>) =
    this.apply { arguments = bundleOf(*pairs) }

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

class SafeClickListener(
    private var defaultInterval: Int = 400,
    private val onDoubleClick: ((View) -> Unit)? = null,
    private val onSafeCLick: (View) -> Unit,
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (onDoubleClick != null && SystemClock.elapsedRealtime() - doubleClickLastDuration < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick.invoke(v)
            return
        }
        doubleClickLastDuration = SystemClock.elapsedRealtime()

        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300
        private var doubleClickLastDuration = 0L
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        it.hapticFeedbackEnabled()
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun View.setSafeOnClickListener(
    onSafeClick: (View) -> Unit,
    onDoubleClick: ((View) -> Unit)? = null,
) {
    val safeClickListener = SafeClickListener(onSafeCLick = {
        it.hapticFeedbackEnabled()
        onSafeClick(it)
    }, onDoubleClick = {
        it.hapticFeedbackEnabled()
        onDoubleClick?.invoke(it)
    })
    setOnClickListener(safeClickListener)
}

@Deprecated("For internal use only. Use HomeGraph.openShareDialog with args instead")
fun Context.shareLink(url: String, title: String? = null) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, title)
    startActivity(shareIntent)
}

fun Fragment.copyToClipboard(value: String, @StringRes toastMessageRes: Int) {
    val clipboard: ClipboardManager? =
        ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
    val clip = ClipData.newPlainText("", value)
    clipboard?.setPrimaryClip(clip)
    requireView().showTopSnackBar(toastMessageRes, Snackbar.LENGTH_SHORT)
}


fun Fragment.toast(message: String?) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

inline fun Fragment.launchWithViewLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            block()
        }
    }
}

val Fragment.screenWidth: Int
    get() = requireActivity().screenWidth

val Fragment.screenHeight: Int
    get() = requireActivity().screenHeight

inline fun Fragment.doOnBackPressed(crossinline action: (() -> Unit)) =
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { action() }

fun Fragment.findChildNavController(@IdRes viewId: Int): NavController {
    return (childFragmentManager.findFragmentById(viewId) as NavHostFragment).navController
}

fun Fragment.isFragmentAdded(): Boolean {
    return (activity != null/* && view != null*/ && !isDetached && isAdded)
}

fun Fragment.setNavigationBarColor(color: Int) {
    activity?.window?.let { it.navigationBarColor = color }
}

fun Fragment.setStatusBarColor(color: Int) {
    activity?.window?.let { it.statusBarColor = color }
}