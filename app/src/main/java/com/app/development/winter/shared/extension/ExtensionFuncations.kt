@file:Suppress("DEPRECATION")

package com.app.development.winter.shared.extension

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.*
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.app.development.winter.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.Serializable
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * puts a key value pair in shared prefs if doesn't exists, otherwise updates value o`n given [key]
 */
operator fun SharedPreferences.set(key: String, value: Any?) {
    when (value) {
        is String? -> edit { it.putString(key, value) }
        is Int -> edit { it.putInt(key, value) }
        is Boolean -> edit { it.putBoolean(key, value) }
        is Float -> edit { it.putFloat(key, value) }
        is Long -> edit { it.putLong(key, value) }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

fun View.clickWithDepthEffect(onClick: () -> Unit) {
    this.setOnClickListener {
        animate().scaleX(0.9f).scaleY(0.9f).setDuration(90).withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(90).withEndAction {
                onClick.invoke()
            }
        }
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.fadeVisibility(visibility: Int, duration: Long = 400) {
    val transition: Transition = Fade()
    transition.duration = duration
    transition.addTarget(this)
    TransitionManager.beginDelayedTransition(this.parent as ViewGroup, transition)
    this.visibility = visibility
}

fun View.absX(): Int {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return location[0]
}

fun View.absY(): Int {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    return location[1]
}

fun View.setStartMargin(@DimenRes dimensionResId: Int) {
    (layoutParams as ViewGroup.MarginLayoutParams).leftMargin =
        resources.getDimension(dimensionResId).toInt()
}

fun View.setTopMargin(@DimenRes dimensionResId: Int) {
    (layoutParams as ViewGroup.MarginLayoutParams).topMargin =
        resources.getDimension(dimensionResId).toInt()
}

fun View.setEndMargin(@DimenRes dimensionResId: Int) {
    (layoutParams as ViewGroup.MarginLayoutParams).rightMargin =
        resources.getDimension(dimensionResId).toInt()
}


private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}

/**
 * finds value on given key.
 * [T] is the type of value
 * @param defaultValue optional default value - will take null for strings, false for bool and -1 for numeric values if [defaultValue] is not specified
 */
inline operator fun <reified T : Any> SharedPreferences.get(
    key: String,
    defaultValue: T? = null,
): T? {
    return when (T::class) {
        String::class -> getString(key, defaultValue as? String) as T?
        Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
        Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
        Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
        Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

/**
 * Return trimmed text of EditText
 * */
fun EditText.getTrimText(): String = text.toString().trim()

fun TextView.getTrimText(): String = text.toString().trim()

/**
 * Return true If EditText is empty otherwise false
 * */
fun EditText.isEmpty(): Boolean = TextUtils.isEmpty(text.toString().trim())

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String?.isNumber(): Boolean {
    return if (this.isNullOrEmpty()) false else this.all { Character.isDigit(it) }
}

var isClicked = false
fun View?.setBounceButtonListeners(listener: (view: View?) -> Unit) {
    this?.setOnClickListener { view ->
        if (!isClicked) {
            isClicked = true
            view?.postDelayed({
                isClicked = false
                view.hapticFeedbackEnabled()
                listener.invoke(this)
            }, 80)
        }
    }
}

/**
 * Return true If EditText is not empty otherwise false
 * */
fun EditText.isNotEmpty(): Boolean = !isEmpty()

inline fun AppBarLayout.disableCollapsingToolBarScroll() {
    val params = layoutParams as CoordinatorLayout.LayoutParams
    params.behavior = AppBarLayout.Behavior()
    val behavior: AppBarLayout.Behavior = params.behavior as AppBarLayout.Behavior
    behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
        override fun canDrag(appBarLayout: AppBarLayout): Boolean {
            return false
        }
    })
}

inline fun EditText.onTextChanged(crossinline listener: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            listener(s, start, before, count)
        }
    })
}

/**
 * Extension to capitalize characters.
 */
fun EditText.setAllCaps() {
    val filter = InputFilter.AllCaps()
    editableText.filters = arrayOf(filter)
}

@SuppressLint("ClickableViewAccessibility")
inline fun EditText.setOnRightDrawableClickListener(crossinline listener: () -> Unit) {
    setOnTouchListener(View.OnTouchListener { _, event ->
        // val DRAWABLE_LEFT = 0
        // val DRAWABLE_TOP = 1
        val drawableRight = 2
        // val DRAWABLE_BOTTOM = 3
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.rawX >= right - compoundDrawables[drawableRight].bounds.width()) {
                listener()
                return@OnTouchListener true
            }
        }
        false
    })
}


/**
 * Get bitmap from view
 */
fun View.getBitmapFromView(activity: Activity, callback: (Bitmap?) -> Unit) {
    if (SDK_INT >= Build.VERSION_CODES.O) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            getLocationInWindow(locationOfViewInWindow)
            try {

                PixelCopy.request(
                    window, Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + width,
                        locationOfViewInWindow[1] + height
                    ), bitmap, { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        } else {
                            callback(null)
                        }
                        // possible to handle other result codes ...
                    }, Handler()
                )

            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
    } else {
        callback(null)
    }
}

/**
 * Save bitmap to Storage
 */
suspend fun Bitmap.saveImageToDownloadFolder(): Boolean {
    try {
        val filePath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "/share_image.png"
        )
        val outputStream: OutputStream = FileOutputStream(filePath)
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return true
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return false
    }
}


/*
* Make EditText Scrollable inside scrollview
* */
@SuppressLint("ClickableViewAccessibility")
fun EditText.makeScrollableInScrollView() {
    setOnTouchListener(View.OnTouchListener { v, event ->
        if (hasFocus()) {
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_SCROLL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    return@OnTouchListener true
                }
            }
        }
        false
    })
}

fun TextView.rightDrawable(@DrawableRes resId: Int) {
    this.setCompoundDrawablesWithIntrinsicBounds(0, 0, resId, 0)
}

fun RecyclerView.setDivider(@DrawableRes drawableRes: Int) {
    val divider = DividerItemDecoration(
        this.context, DividerItemDecoration.VERTICAL
    )
    val drawable = ContextCompat.getDrawable(
        this.context, drawableRes
    )
    drawable?.let {
        divider.setDrawable(it)
        addItemDecoration(divider)
    }
}

fun View.hapticFeedbackEnabled() {
    this.isHapticFeedbackEnabled = true
    if (SDK_INT >= Build.VERSION_CODES.P) {
        this.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
    } else {
        this.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}

fun ImageView.loadImage(
    imageUrl: String?, @DrawableRes placeholder: Int = 0,
) {
    justTry {
        Glide.with(com.app.development.winter.application.Controller.instance).load(imageUrl).transition(
            DrawableTransitionOptions.withCrossFade()
        )
            /* .placeholder(placeholder)*/.into(this)
    }
}

fun ImageView.loadImage(
    @DrawableRes placeholder: Int = 0,
) {
    justTry {
        Glide.with(com.app.development.winter.application.Controller.instance).asDrawable()
            .load(placeholder)
            .into(this)
    }
}

/*
* Execute block if OS version is greater than or equal Naugat(24)
* */
inline fun nougatAndAbove(block: () -> Unit) {
    block()
}

/*
* Execute block into try...catch
* */
inline fun <T> justTry(tryBlock: () -> T) = try {
    tryBlock()
} catch (e: Exception) {
    e.printStackTrace()
}

// Start new Activity functions

/*
* Start Activity from Activity
* */
inline fun <reified T : Any> Context.launchActivity(
    noinline init: Intent.() -> Unit = {},
) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivity(intent)
}

/*
* Start Activity from Activity
* */
inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    noinline init: Intent.() -> Unit = {},
) {
    val intent = newIntent<T>(this)
    intent.init()
    if (requestCode == -1) startActivity(intent)
    else startActivityForResult(intent, requestCode)
}

inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    extras: Bundle.() -> Unit = {},
    noinline init: Intent.() -> Unit = {},
) {
    val intent = newIntent<T>(this)
    intent.init()
    intent.putExtras(Bundle().apply(extras))
    if (requestCode == -1) startActivity(intent)
    else startActivityForResult(intent, requestCode)
}


inline fun <reified T : Any> Fragment.launchActivity(
    requestCode: Int = -1,
    noinline init: Intent.() -> Unit = {},
) {
    val intent = newIntent<T>(this.requireContext())
    intent.init()
    if (requestCode == -1) startActivity(intent)
    else startActivityForResult(intent, requestCode)

}

inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)

fun Context.openPdfFromUrl(pdfUrl: String?) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
    startActivity(browserIntent)
}

fun Fragment.openPdfFromUrl(pdfUrl: String?) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
    startActivity(browserIntent)
}

fun Context.openCall(call: String?) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$call")
    startActivity(intent)
}

fun Fragment.openCall(call: String?) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$call")
    startActivity(intent)
}

fun Context.openMap(latitude: Double?, longitude: Double?) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW, Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude('')")
        )
        startActivity(intent)
    } catch (ane: ActivityNotFoundException) {
        Toast.makeText(this, "Please Install Google Maps ", Toast.LENGTH_LONG).show()
    } catch (ex: java.lang.Exception) {
        ex.message
    }
}
//
//fun Context.openMapNavigation(startLatLng: LatLng, endLatLng: LatLng) {
//    try {
//        val uri: String = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", startLatLng.latitude, startLatLng.longitude, endLatLng.latitude, endLatLng.longitude)
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
//        intent.setPackage("com.google.android.apps.maps")
//        startActivity(intent)
//    } catch (ane: ActivityNotFoundException) {
//        Toast.makeText(this, "Please Install Google Maps ", Toast.LENGTH_LONG).show()
//    } catch (ex: java.lang.Exception) {
//        ex.message
//    }
//}

fun Fragment.openMap(latitude: Double?, longitude: Double?) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW, Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude('')")
        )
        startActivity(intent)
    } catch (ane: ActivityNotFoundException) {
        Toast.makeText(context, "Please Install Google Maps ", Toast.LENGTH_LONG).show()
    } catch (ex: java.lang.Exception) {
        ex.message
    }
}

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
    val fragmentTransaction = beginTransaction()
    fragmentTransaction.func()
    fragmentTransaction.commit()
}

/**
 * Return simple class name
 * */
fun Any.getClassName(): String {
    return this::class.java.simpleName
}

/*
* Show Home button at ActionBar and set icon
* *//*fun ActionBar?.showHomeButton(show: Boolean, @DrawableRes icon: Int = R.drawable.ic_back_white) {
    this?.setDisplayHomeAsUpEnabled(show)
    this?.setDisplayShowHomeEnabled(show)
    this?.setHomeAsUpIndicator(icon)
}*/


fun Intent.getInt(key: String, defaultValue: Int = 0): Int {
    return extras?.getInt(key, defaultValue) ?: defaultValue
}

fun Intent.getString(key: String, defaultValue: String = ""): String {
    return extras?.getString(key, defaultValue) ?: defaultValue
}

/*
* Return activity main content view
* *//*
val Activity.contentView: View?
    get() = findViewById<ViewGroup>(R.id.content)?.getChildAt(0)
*/


/**
 * Hide/Show view with scale animation
 * */
fun View.setVisibilityWithScaleAnim(visibility: Int) {
    this.clearAnimation()
    this.visibility = View.VISIBLE
    val scale = if (visibility == View.GONE) 0f
    else 1f

    val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
        this,
        PropertyValuesHolder.ofFloat("scaleX", scale),
        PropertyValuesHolder.ofFloat("scaleY", scale)
    )
    scaleDown.duration = 300
    scaleDown.interpolator = DecelerateInterpolator()
    scaleDown.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator) {

        }

        override fun onAnimationEnd(p0: Animator) {
            this@setVisibilityWithScaleAnim.visibility = visibility
        }

        override fun onAnimationCancel(p0: Animator) {

        }

        override fun onAnimationRepeat(p0: Animator) {

        }
    })
    scaleDown.start()
}

fun View.removeFromParent(invalidate: Boolean = true): Boolean {
    if (parent is ViewGroup) {
        val parentView = parent as ViewGroup
        if (invalidate) parentView.removeView(this)
        else parentView.removeViewInLayout(this)
        return true
    }
    return false
}

fun View.setAnimationWithVisibility(@AnimRes animationRes: Int, visibility: Int) {
    setVisibility(visibility)
    clearAnimation()
    val viewAnim = AnimationUtils.loadAnimation(context, animationRes)
    animation = viewAnim
    viewAnim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            setVisibility(visibility)
        }

        override fun onAnimationStart(animation: Animation?) {
            setVisibility(View.VISIBLE)
        }
    })
    // viewAnim.start()
}

fun Context.getAppVersionName(): String {
    return packageManager.getPackageInfo(packageName, 0).versionName
}

fun Context.showToast(
    message: String?,
    duration: Int = Toast.LENGTH_SHORT,
    gravity: Int = Gravity.CENTER,
) {
    if (!message.isNullOrEmpty()) Toast.makeText(this, message, duration).run {
        setGravity(gravity, 0, 0)
        show()
    }
}

/*fun setSpannableColor(){
    var word: SpannableString =  SpannableString("Your message");

    word.setSpan(new ForegroundColorSpan(Color.BLUE), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    textView.setText(word);
    Spannable wordTwo = new SpannableString("Your new message");

    wordTwo.setSpan(new ForegroundColorSpan(Color.RED), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    textView.append(wordTwo);
}*/

fun SpannableString.setClickableSpan(
    start: Int,
    end: Int, @ColorInt color: Int,
    block: (view: View?) -> Unit,
) {
    setSpan(object : ClickableSpan() {
        override fun onClick(view: View) {
            block(view)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false // set to false to remove underline
        }

    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    // Set Color Span
    setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

/*
* Toggle integer value, from 0->1 or 1->0
* */
fun Int.toggle() = if (this == 1) 0 else 1

/*
* Return true if view is visible otherwise return false
* */
fun View.isVisible() = visibility == View.VISIBLE
fun View.isGone(): Boolean = visibility == View.GONE
fun View.isInvisible(): Boolean = visibility == View.INVISIBLE

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

/*
* Set enabled/disable
* */
fun View.setEnabledWithAlpha(enabled: Boolean, disabledAlpha: Float = 0.5f) {
    isEnabled = enabled
    alpha = if (isEnabled) 1f else disabledAlpha
}

fun RecyclerView.stickyHeaderView(position: Int, stickyView: View) {
    val index = position - 4

    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
            toggleStickyLayout(position, this@stickyHeaderView, stickyView, dy)
            if (index in firstVisiblePosition..lastVisiblePosition) {
                if (dy > 0) {
                    stickyView.hide()
                } else {
                    if (index == lastVisiblePosition) {
                        stickyView.show()
                    } else {
                        stickyView.hide()
                    }
                }
            } else {
                stickyView.show()
            }
        }
    })
}

fun toggleStickyLayout(position: Int, recyclerView: RecyclerView, stickyView: View, dy: Int) {

    val index = position - 4

    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

    /* Log.e("position", "Index -> ${index.toString()}")
     Log.e("position", "firstVisiblePosition -> ${firstVisiblePosition.toString()}")
     Log.e("position", "lastVisiblePosition -> ${lastVisiblePosition.toString()}")*/

    if (index in firstVisiblePosition..lastVisiblePosition) {
        if (dy > 0) {
            if (index == firstVisiblePosition - 1) {
                stickyView.show()
            } else stickyView.hide()
        } else {
            stickyView.hide()
        }
    } else {
        stickyView.show()

    }
}

fun String?.nullSafe(defaultValue: String = ""): String {
    return this ?: defaultValue
}

fun Int?.nullSafe(defaultValue: Int = 0): Int {
    return this ?: defaultValue
}

fun Float?.nullSafe(defaultValue: Float = 0f): Float {
    return this ?: defaultValue
}

fun Long?.nullSafe(defaultValue: Long = 0L): Long {
    return this ?: defaultValue
}

fun Double?.nullSafe(defaultValue: Double = 0.0): Double {
    return this ?: defaultValue
}

fun Boolean?.nullSafe(defaultValue: Boolean = false): Boolean {
    return this ?: defaultValue
}

fun <T> List<T>?.nullSafe(): List<T> {
    return this ?: ArrayList()
}


fun String?.toLongOrDefaultValue(defaultValue: Long = 0L): Long {
    return if (isNullOrEmpty()) defaultValue
    else {
        try {
            this.toLong().nullSafe()
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }
}

fun TextView.setTextViewTextColor(color: Int) {
    justTry {
        setTextColor(
            ContextCompat.getColor(
                com.app.development.winter.application.Controller.instance,
                color
            )
        )
    }
}

fun MaterialButton.setBackgroundTintColor(color: Int) {
    justTry {
        backgroundTintList =
            ContextCompat.getColorStateList(
                com.app.development.winter.application.Controller.instance,
                color
            )
    }
}


fun String?.fromHtml(): Spanned {
    return if (this == null) SpannableString("")
    else Html.fromHtml(this.trim(), Html.FROM_HTML_MODE_LEGACY)
}

/**
 * Return ActionBar height
 * *//*fun Activity.getActionBarHeight(): Int {
    val tv = TypedValue()
    return if (theme.resolveAttribute(R.attr.actionBarSize, tv, true))
        TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
    else 0
}*/

fun View.measureWidthHeight(onCompleteMeasure: (width: Int, height: Int) -> Unit) {
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            viewTreeObserver.removeOnPreDrawListener(this)
            onCompleteMeasure.invoke(measuredWidth, measuredHeight)
            return true
        }
    })
}

fun getDummyList(length: Int): List<String> {
    val list: MutableList<String> = ArrayList()
    for (i in 0 until length) {
        list.add("test $i")
    }
    return list
}

fun getListHour(length: Int): List<String> {
    val list: MutableList<String> = ArrayList()
    for (i in 1 until length) {
        list.add("$i Hour")
    }
    return list
}

fun Drawable.setTintColor(@ColorInt colorTint: Int): Drawable {
    colorFilter = PorterDuffColorFilter(colorTint, PorterDuff.Mode.SRC_ATOP)
    return this
}

/*
fun SearchView.setHintColor(@ColorInt hintColor: Int) {
    (findViewById<EditText>(R.id.search_src_text)).setHintTextColor(hintColor)
}
*/

fun String?.int(defaultValue: Int = 0): Int {
    return if (isNullOrEmpty()) {
        defaultValue
    } else {
        try {
            this.toInt().nullSafe()
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }
}

fun String?.double(defaultValue: Double = 0.0): Double {
    return if (isNullOrEmpty()) {
        defaultValue
    } else {
        try {
            this.toDouble().nullSafe()
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }
}

fun View.tag(): String = if (tag == null) "" else tag.toString()

fun RadioGroup.checkedButtonText(): String {
    return findViewById<RadioButton>(checkedRadioButtonId).text.toString()
        .takeIf { checkedRadioButtonId != -1 } ?: ""
}


fun Location.getPostalCode(context: Context): String {
    var postalCode = "000000"
    try {
        val geocoder = Geocoder(context)
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            for (element in addresses) {
                if (element.postalCode != null) {
                    postalCode = element.postalCode
                    break
                }
            }
            return postalCode
        }
    } catch (e: Exception) {
        return postalCode
    }
    return postalCode
}

fun <T> Class<T>.checkTopActivityIsNotOneInConstrain(): Boolean {
    val result =
        com.app.development.winter.application.Controller.instance.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
    val task: List<ActivityManager.AppTask> = result.appTasks
    val activeComponent = task[0].taskInfo.topActivity
    return activeComponent?.className.toString()
        .equals(this::class.qualifiedName, ignoreCase = true)
}

fun <T> Class<T>.checkTopActivityIsOneInConstrain(): Boolean {
    val result =
        com.app.development.winter.application.Controller.instance.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
    val task: List<ActivityManager.AppTask> = result.appTasks
    return if (task.isEmpty().not()) {
        val activeComponent = task[0].taskInfo.topActivity
        activeComponent?.className.toString()
            .equals(this@checkTopActivityIsOneInConstrain.name, ignoreCase = true)
    } else {
        false
    }
}

fun <T> Class<T>.getCurrentActivity(): Class<T>? {
    if (this::class.java.checkTopActivityIsOneInConstrain().not()) {
        return this
    }
    return null
}


/**
 * Hides the system bars and makes the Activity "fullscreen". If this should be the default
 * state it should be called from [Activity.onWindowFocusChanged] if hasFocus is true.
 * It is also recommended to take care of cutout areas. The default behavior is that the legendary shows
 * in the cutout area in portrait mode if not in fullscreen mode. This can cause "jumping" if the
 * user swipes a system bar to show it. It is recommended to set [WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER],
 * call [showBelowCutout] from [Activity.onCreate]
 * (see [Android Developers article about cutouts](https://developer.android.com/guide/topics/display-cutout#never_render_content_in_the_display_cutout_area)).
 * @see showSystemUI
 * @see addSystemUIVisibilityListener
 */
fun Activity.hideSystemUI() {
    if (SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let {
            // Default behavior is that if navigation bar is hidden, the system will "steal" touches
            // and show it again upon user's touch. We just want the user to be able to show the
            // navigation bar by swipe, touches are handled by custom code -> change system bar behavior.
            // Alternative to deprecated SYSTEM_UI_FLAG_IMMERSIVE.
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // make navigation bar translucent (alternative to deprecated
            // WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            // - do this already in hideSystemUI() so that the bar
            // is translucent if user swipes it up
//                window.navigationBarColor = getColor(R.color.internal_black_semitransparent_light)
            // Finally, hide the system bars, alternative to View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            // and SYSTEM_UI_FLAG_FULLSCREEN.
            it.hide(WindowInsets.Type.systemBars())
        }
    } else {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        @Suppress("DEPRECATION") window.decorView.systemUiVisibility = (
                // Do not let system steal touches for showing the navigation bar
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                        // Keep the legendary content behind the bars even if user swipes them up
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        // make navbar translucent - do this already in hideSystemUI() so that the bar
        // is translucent if user swipes it up
        @Suppress("DEPRECATION") window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }
}

/**
 * Shows the system bars and returns back from fullscreen.
 * @see hideSystemUI
 * @see addSystemUIVisibilityListener
 */
fun Activity.showSystemUI() {
    if (SDK_INT >= Build.VERSION_CODES.R) {
        // show legendary content in fullscreen, i. e. behind the bars when they are shown (alternative to
        // deprecated View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.setDecorFitsSystemWindows(false)
        // finally, show the system bars
        window.insetsController?.show(WindowInsets.Type.systemBars())
    } else {
        // Shows the system bars by removing all the flags
        // except for the ones that make the content appear under the system bars.
        @Suppress("DEPRECATION") window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}

fun Window.hideSystemUI() {
    if (SDK_INT >= Build.VERSION_CODES.R) {
        insetsController?.let {
            // Default behavior is that if navigation bar is hidden, the system will "steal" touches
            // and show it again upon user's touch. We just want the user to be able to show the
            // navigation bar by swipe, touches are handled by custom code -> change system bar behavior.
            // Alternative to deprecated SYSTEM_UI_FLAG_IMMERSIVE.
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // make navigation bar translucent (alternative to deprecated
            // WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            // - do this already in hideSystemUI() so that the bar
            // is translucent if user swipes it up
//                window.navigationBarColor = getColor(R.color.internal_black_semitransparent_light)
            // Finally, hide the system bars, alternative to View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            // and SYSTEM_UI_FLAG_FULLSCREEN.
            it.hide(WindowInsets.Type.systemBars())
        }
    } else {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        @Suppress("DEPRECATION") decorView.systemUiVisibility = (
                // Do not let system steal touches for showing the navigation bar
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                        // Keep the legendary content behind the bars even if user swipes them up
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        // make navbar translucent - do this already in hideSystemUI() so that the bar
        // is translucent if user swipes it up
        @Suppress("DEPRECATION") addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }
}

/**
 * Shows the system bars and returns back from fullscreen.
 * @see hideSystemUI
 * @see addSystemUIVisibilityListener
 */
fun Window.showSystemUI() {
    if (SDK_INT >= Build.VERSION_CODES.R) {
        // show legendary content in fullscreen, i. e. behind the bars when they are shown (alternative to
        // deprecated View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        setDecorFitsSystemWindows(false)
        // finally, show the system bars
        insetsController?.show(WindowInsets.Type.systemBars())
    } else {
        // Shows the system bars by removing all the flags
        // except for the ones that make the content appear under the system bars.
        @Suppress("DEPRECATION") decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}

fun TextView.makeLinksWithFont(
    color: String? = null,
    isLinkBold: Boolean = false,
    vararg links: Pair<String, View.OnClickListener>
) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                if (color.isNullOrEmpty()) {
                    textPaint.color = textPaint.linkColor
                } else {
                    textPaint.color = Color.parseColor(color)
                }
                // toggle below value to enable/disable
                // the underline shown below the clickable text
                //textPaint.isUnderlineText = true
                //textPaint.isUnderlineText = true
                if (isLinkBold) {
                    textPaint.typeface = Typeface.DEFAULT_BOLD
                }
            }

            override fun onClick(view: View) {
                view.hapticFeedbackEnabled()
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun TextView.makeLinksWithFont(
    fontRes: Int? = R.font.montserrat_bold,
    color: Int,
    vararg links: Pair<String, View.OnClickListener>
) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                textPaint.color = resources.getColor(color) //textPaint.linkColor
                // toggle below value to enable/disable
                // the underline shown below the clickable text
                //textPaint.isUnderlineText = true
                //textPaint.isUnderlineText = true
                textPaint.typeface =
                    ResourcesCompat.getFont(context, fontRes ?: R.font.montserrat_bold)
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun TextView.makePrimaryColorAndBold(vararg links: String, foregroundColorSpan: Int) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        startIndexOfLink = this.text.toString().indexOf(link, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            ForegroundColorSpan(foregroundColorSpan),
            startIndexOfLink,
            startIndexOfLink + link.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
        spannableString.setSpan(
            CustomTypefaceSpan("", typeface!!),
            startIndexOfLink,
            startIndexOfLink + link.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            RelativeSizeSpan(1.25f),
            startIndexOfLink,
            startIndexOfLink + link.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun TextView.setColoredText(fullText: String, highlightText: String, highlightColor: Int, isBold: Boolean = false) {
    val spannableString = SpannableString(fullText)
    val start = fullText.indexOf(highlightText)
    val end = start + highlightText.length
    if (start >= 0) {
        spannableString.setSpan(
            ForegroundColorSpan(highlightColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        if (isBold) {
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    this.text = spannableString
}


fun TextView.makeColorNBold(vararg links: String, color: Int) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        startIndexOfLink = this.text.toString().indexOf(link, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(color)),
            startIndexOfLink,
            startIndexOfLink + link.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val typeface = ResourcesCompat.getFont(context, R.font.baloo)
        spannableString.setSpan(
            CustomTypefaceSpan("", typeface!!),
            startIndexOfLink,
            startIndexOfLink + link.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.post {
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }
}

fun TextView.makeColor(links: ArrayList<String>, color: Int) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    links.forEach {
        startIndexOfLink = this.text.toString().indexOf(it, startIndexOfLink + 1)
        if (startIndexOfLink == -1) return@forEach
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(color)),
            startIndexOfLink,
            startIndexOfLink + it.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.post {
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }
}

fun TextView.makeColor(vararg links: String, color: Int) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        startIndexOfLink = this.text.toString().indexOf(link, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(color)),
            startIndexOfLink,
            startIndexOfLink + link.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.NORMAL),
            startIndexOfLink,
            startIndexOfLink + link.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}


fun View.handleVisualOverlaps(
    marginInsteadOfPadding: Boolean = true,
    gravity: Int,
    isLandScape: Boolean = false,
) {
    val marginTop = marginTop
    val marginBottom = marginBottom
    val marginLeft = marginLeft
    val marginRight = marginRight

    val paddingTop = paddingTop
    val paddingBottom = paddingBottom
    val paddingLeft = paddingLeft
    val paddingRight = paddingRight

    setOnApplyWindowInsetsListener { view, insets ->
        val insetLeft = insets.systemWindowInsetLeft
        val insetRight = insets.systemWindowInsetRight
        val insetTop = insets.systemWindowInsetTop
        val insetBottom = insets.systemWindowInsetBottom
        if (isLandScape) {
            if (marginInsteadOfPadding) {

                when (gravity) {
                    Gravity.TOP -> {
                        view.updateMargin(start = insetLeft + marginLeft)
                    }

                    Gravity.BOTTOM -> {
                        view.updateMargin(end = insetRight + marginRight)
                    }

                    else -> {
                        view.updateMargin(start = insetLeft + marginLeft)
                        view.updateMargin(end = insetRight + marginRight)
                    }
                }

            } else {
                when (gravity) {
                    Gravity.TOP -> {
                        view.updatePaddingRelative(start = insetLeft + paddingLeft)
                    }

                    Gravity.BOTTOM -> {
                        view.updatePaddingRelative(end = insetRight + paddingRight)
                    }

                    else -> {
                        view.updatePaddingRelative(start = insetLeft + paddingLeft)
                        view.updatePaddingRelative(end = insetRight + paddingRight)
                    }
                }
            }
        }
        if (marginInsteadOfPadding) {
            when (gravity) {
                Gravity.TOP -> {
                    view.updateMargin(top = insetTop + marginTop)
                }

                Gravity.BOTTOM -> {
                    view.updateMargin(bottom = insetBottom + marginBottom)
                }

                else -> {
                    view.updateMargin(top = insetTop + marginTop)
                    view.updateMargin(bottom = insetBottom + marginBottom)
                }
            }
        } else {
            when (gravity) {
                Gravity.TOP -> {
                    view.updatePaddingRelative(top = insetTop + paddingTop)
                }

                Gravity.BOTTOM -> {
                    view.updatePaddingRelative(bottom = insetBottom + paddingBottom)
                }

                else -> {
                    view.updatePaddingRelative(top = insetTop + paddingTop)
                    view.updatePaddingRelative(bottom = insetBottom + paddingBottom)
                }
            }
        }

        insets
    }
}

fun RecyclerView.removeItemAnimator() {
    itemAnimator?.changeDuration = 0
    itemAnimator = null
}

fun RecyclerView.changeItemAnimatorDuration(value: Long) {
    itemAnimator?.addDuration = value
    itemAnimator?.moveDuration = value
    itemAnimator?.changeDuration = value
    itemAnimator?.removeDuration = value
}

fun View.locationOnScreen(): IntArray {
    val location = IntArray(2)
    this.getLocationInWindow(location)
    return location
}

fun View.updateMargin(
    @Px top: Int = 0, @Px bottom: Int = 0, @Px start: Int = 0, @Px end: Int = 0,
) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(
        if (start == 0) marginStart else start,
        if (top == 0) marginTop else top,
        if (end == 0) marginEnd else end,
        if (bottom == 0) marginBottom else bottom
    )
    layoutParams = params
}

val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
fun Any?.toJSONObject(): JSONObject {
    return JSONObject(this.toJson())
}

fun Any?.toJson(): String = gson.toJson(this)


fun getBitmapFromVectorDrawable(
    resources: Resources, drawableId: Int, height: Int?, width: Int?,
): Bitmap {
    val drawable = ResourcesCompat.getDrawable(resources, drawableId, null)
    val bitmap = Bitmap.createBitmap(
        width ?: drawable!!.intrinsicWidth,
        height ?: drawable!!.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable?.setBounds(0, 0, canvas.width, canvas.height)
    drawable?.draw(canvas)
    return bitmap
}

suspend fun startRepeatingJob(timeInterval: Long, action: () -> Unit): Job {
    return CoroutineScope(Dispatchers.IO).launch {
        while (isActive) {
            action()
            delay(timeInterval)
        }
    }
}


fun <T> MutableList<T>.clearAdd(replace: List<T>) = apply {
    clear()
    addAll(replace)
}

/**
 * get uri to any resource type Via Context Resource instance
 * @param context - context
 * @param resId - resource id
 * @throws Resources.NotFoundException if the given ID does not exist.
 * @return - Uri to resource by given id
 */
@Throws(NotFoundException::class)
fun getUriToResource(
    context: Context, @AnyRes resId: Int,
): Uri {
    /** Return a Resources instance for your application's package.  */
    val res = context.resources
    return getUriToResource(res, resId)
}

/**
 * get uri to any resource type via given Resource Instance
 * @param res - resources instance
 * @param resId - resource id
 * @throws Resources.NotFoundException if the given ID does not exist.
 * @return - Uri to resource by given id
 */
@Throws(NotFoundException::class)
fun getUriToResource(
    res: Resources, @AnyRes resId: Int,
): Uri {
    /** return uri  */
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(
            resId
        ) + '/' + res.getResourceTypeName(resId) + '/' + res.getResourceEntryName(resId)
    )
}

fun getResizedBitmap(
    bm: Bitmap, ratio: Float, isNecessaryToKeepOrig: Boolean,
): Bitmap {
    val width = bm.width
    val height = bm.height
    val scaleWidth = width / ratio
    val scaleHeight = height / ratio/*   // CREATE A MATRIX FOR THE MANIPULATION
     val matrix = Matrix()
     // RESIZE THE BIT MAP
     matrix.postScale(ratio, ratio)

     // "RECREATE" THE NEW BITMAP
     val resizedBitmap =
         Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
     if (!isNecessaryToKeepOrig) {
         bm.recycle()
     }*/

    return Bitmap.createScaledBitmap(
        bm, (scaleWidth / 1.2f).roundToInt(), (scaleHeight / 1.2f).roundToInt(), false
    )
}


private val CLICK_ACTION_THRESHOLD = 90
fun isSingleClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
    val differenceX = abs(startX - endX)
    val differenceY = abs(startY - endY)
    return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD)
}

fun randomInt(max: Int): Int {
    return Random().nextInt(max)
}

fun <T> shuffle(list: MutableList<T>) {
    list.shuffle()
}

/**
 * Max value of X to move before we declare that we are going to do
 * drag.
 */
private const val MAX_X_MOVE = 120f

/**
 * Max value of Y to move before we declare that we are going to do
 * drag.
 */
private const val MAX_Y_MOVE = 120f

@SuppressLint("ClickableViewAccessibility")
fun View.addOnGestureListener(
    onViewTap: (View) -> Unit,
    onViewDrag: (View) -> Unit,
    onViewDragCancel: (View) -> Unit,
) {
    val NONE = 0
    val DRAG = 1
    var mMode: Int = NONE
    this.setOnTouchListener { view, event ->
        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                Log.e("TAG", "ACTION_DOWN -> ${abs(event.x)} , ${abs(event.y)}")
////                onViewTap.invoke(view)
//                return@setOnTouchListener false
////               /* when (mMode) {
//                    DRAG -> {
//                        onViewDrag.invoke(view)
//                        return@setOnTouchListener false
//                    }
//                }*/
//            }
            MotionEvent.ACTION_MOVE -> {
                Log.e("TAG", "ACTION_MOVE -> ${abs(event.x)} , ${abs(event.y)}")
                if (abs(event.x) > MAX_X_MOVE || abs(event.y) > MAX_Y_MOVE) {
                    mMode = DRAG
                    onViewDrag.invoke(view)
                } else {
                    mMode = NONE
                }
            }

            MotionEvent.ACTION_UP -> {
                Log.e("TAG", "ACTION_UP -> Card click")
                when (mMode) {
                    NONE -> {
                        onViewTap.invoke(view)
                        return@setOnTouchListener false
                    }

                    else -> {
                        onViewDragCancel.invoke(view)
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                Log.e("ACTION_CANCEL", "ACTION_CANCEL")
                onViewDragCancel(view)
            }
        }
        return@setOnTouchListener true
    }
}

fun TextView.setBadgeNumber(number: Int) {
    if (number > 9) this.text = "9+"
    else this.text = "$number"
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Parcel.parcelable(key: ClassLoader?): T? = when {
    SDK_INT >= 33 -> readParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") readParcelable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    SDK_INT >= 33 -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    SDK_INT >= 33 -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

inline fun <reified T : Serializable> Bundle.serializableArrayList(key: String): T? = when {
    SDK_INT >= 33 -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") (getSerializable(key) as T?)
}

inline fun <reified T : Serializable> Intent.serializableArrayList(key: String): T? = when {
    SDK_INT >= 33 -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") (getSerializableExtra(key) as T?)
}

fun View.toTransitionGroup() = this to transitionName
fun Long.toFormattedThousands(): String {
    return (DecimalFormat.getInstance(Locale.US) as DecimalFormat).apply {
        maximumFractionDigits = 0
        val formatSymbols = decimalFormatSymbols
        formatSymbols.groupingSeparator = ' '
        decimalFormatSymbols = formatSymbols
    }.format(this)
}

fun View.startTransition(duration: Int = 500) {
    val transition = this.background as TransitionDrawable
    transition.startTransition(duration)
}

fun View.reverseTransition() {
    val transition = this.background as TransitionDrawable
    transition.reverseTransition(300)
}

fun <T> List<T>.split(predicate: (T) -> Boolean): List<List<T>> {
    val idx = this.indexOfFirst(predicate)
    return if (idx == -1) {
        listOf(this)
    } else {
        return listOf(this.take(idx)) + this.drop(idx + 1).split(predicate)
    }
}

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()