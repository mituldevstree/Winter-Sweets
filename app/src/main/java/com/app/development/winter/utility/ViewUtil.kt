package com.app.development.winter.utility

import android.R.attr.radius
import android.R.attr.state_hovered
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.text.util.Linkify
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.WindowMetrics
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.development.winter.R
import com.app.development.winter.shared.base.viewbase.AdvancedBaseActivity
import com.app.development.winter.shared.extension.createBitmap
import com.bumptech.glide.Glide
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.tabs.TabLayout
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.system.exitProcess

object ViewUtil {
    fun isNullOrEmpty(charSequence: CharSequence?): Boolean {
        return charSequence?.toString()?.isEmpty() ?: true
    }

    fun isViewContains(view: View, rx: Int, ry: Int): Boolean {
        val l = IntArray(2)
        view.getLocationOnScreen(l)
        val x = l[0]
        val y = l[1]
        val w = view.width
        val h = view.height
        return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    @Suppress("unused")
    fun applyRoundedDrawableBg(view: View) {
        val shapeAppearanceModel =
            ShapeAppearanceModel().toBuilder().setAllCorners(CornerFamily.ROUNDED, radius.toFloat())
                .build()
        val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        val states = arrayOf(intArrayOf(state_hovered))
        val colors = intArrayOf(R.color.colorBlack)
        val myColorList = ColorStateList(states, colors)
        shapeDrawable.fillColor = myColorList
        ViewCompat.setBackground(view, shapeDrawable)
    }

    fun killApp(context: Activity) {
        context.finishAndRemoveTask()
        exitProcess(0)
    }

    fun restartApp(context: AppCompatActivity) {
        Log.e("check_cycle_ads", "restartApp")
        if (isOtherAppOnTopInAndroidVersionBelow10()) {
            context.onBackPressedDispatcher.onBackPressed()
        }
        val i = context.intent
        i?.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
        Log.e("check_cycle_ads", "restartAppSame")
    }

    fun restartApp(context: FragmentActivity) {
        Log.e("check_cycle_ads", "restartApp")
        if (isOtherAppOnTopInAndroidVersionBelow10()) {
            context.onBackPressedDispatcher.onBackPressed()
        }
        val i = context.intent
        i?.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
        Log.e("check_cycle_ads", "restartAppSame")
    }

    private fun isAndroidVersionBelow10(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }

    private fun isOtherAppOnTopInAndroidVersionBelow10(): Boolean {
        return isAndroidVersionBelow10() && (!com.app.development.winter.application.Controller.appInForeground)
    }

    @Suppress("unused")
    fun updateViewHeight(view: View) {
        val anim: ValueAnimator = ValueAnimator.ofInt(view.measuredHeight, 2100)
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams: ViewGroup.LayoutParams = view.layoutParams
            layoutParams.height = `val`
            view.layoutParams = layoutParams
        }
        anim.duration = 200
        anim.start()
    }

    @Suppress("unused")
    fun getDeviceHeight(context: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics? =
                context.getSystemService(WindowManager::class.java)?.currentWindowMetrics
            metrics?.bounds?.height() ?: 0
        } else {
            val metrics = DisplayMetrics()
            context.windowManager.defaultDisplay.getMetrics(metrics)
            metrics.heightPixels.plus(getNavigationBarHeight(context))
                .plus(getStatusBarHeight(context))
        }
    }

    fun getStatusBarHeight(context: Activity): Int {
        val resourceId: Int =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            val rectangle = Rect()
            val window: Window = context.window
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            rectangle.top
        }
        return statusBarHeight
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getNavigationBarHeight(activity: Activity): Int {
        var navigationBarHeight = 0
        val resourceId: Int =
            activity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            navigationBarHeight = activity.resources.getDimensionPixelSize(resourceId)
        }
        return navigationBarHeight
    }

    @Suppress("unused")
    fun getDeviceWidth(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics? =
                (context as Activity).getSystemService(WindowManager::class.java)?.currentWindowMetrics
            metrics?.bounds?.height() ?: 0
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION") (context as Activity).windowManager.defaultDisplay.getMetrics(
                metrics
            )
            metrics.widthPixels
        }
    }

    fun isYesterday(whenInMillis: Long): Boolean {
        return (whenInMillis < Calendar.getInstance().timeInMillis)
    }

    @Suppress("unused")
    fun isToday(whenInMillis: Long): Boolean {
        return DateUtils.isToday(whenInMillis)
    }

    @JvmStatic
    fun getTimeAgo(context: Context, time: Long, format: String?): String? {
        return when {
            DateUtils.isToday(time) -> {
                context.getString(R.string.today)
            }

            DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS) -> {
                context.getString(R.string.yesterday)
            }

            else -> {
                getFormattedDate(time, format)
            }
        }
    }

    @JvmStatic
    fun getFormattedDate(time: Long, format: String?): String? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        return SimpleDateFormat(format, Locale.ENGLISH).format(calendar.time)
    }

    @JvmStatic
    fun convertToHHMMSS(seconds: Long): String {
        var seconds = seconds
        seconds /= 1000
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24
        val d = seconds / (60 * 60 * 24) % 365
        return if (h > 0) {
            String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
        } else if (m > 0) {
            String.format(Locale.ENGLISH, "00:%02d:%02d", m, s)
        } else {
            String.format(Locale.ENGLISH, "00:00:%02d", s)
        }
    }

    @JvmStatic
    fun convertSecondsToHHMMSS(seconds: Long): String {
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24
        val d = seconds / (60 * 60 * 24) % 365
        return if (d > 0) {
            val totalHours = (d * 24) + h
            String.format(Locale.ENGLISH, "%02d:%02d:%02d", totalHours, m, s)
        } else if (h > 0) {
            String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
        } else if (m > 0) {
            String.format(Locale.ENGLISH, "00:%02d:%02d", m, s)
        } else {
            String.format(Locale.ENGLISH, "00:00:%02d", s)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setImageButtonSelector(vararg views: ImageView) {

        for (imageView in views) {
            imageView.setOnTouchListener(object : View.OnTouchListener {
                private var rect: Rect? = null
                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            rect = Rect(
                                imageView.left, imageView.top, imageView.right, imageView.bottom
                            )
                            if (imageView.drawable != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    imageView.drawable.colorFilter = BlendModeColorFilter(
                                        0x44000000, BlendMode.SRC_ATOP
                                    )
                                } else {
                                    @Suppress("DEPRECATION") imageView.drawable.setColorFilter(
                                        0x44000000, PorterDuff.Mode.SRC_ATOP
                                    )
                                }
                                imageView.invalidate()
                            }
                        }

                        MotionEvent.ACTION_MOVE -> if (!rect!!.contains(
                                imageView.left + event.x.toInt(), imageView.top + event.y.toInt()
                            )
                        ) { // User moved outside bounds
                            if (imageView.drawable != null) {
                                imageView.drawable.clearColorFilter()
                                imageView.invalidate()
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            // mVelocityTracker.recycle();
                            if (imageView.drawable != null) {
                                imageView.drawable.clearColorFilter()
                                imageView.invalidate()
                            }
                        }
                    }
                    return false
                }
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setButtonSelector(vararg views: View) {
        for (imageView in views) {
            imageView.setOnTouchListener(object : View.OnTouchListener {
                private var rect: Rect? = null
                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            rect = Rect(
                                imageView.left, imageView.top, imageView.right, imageView.bottom
                            )
                            if (imageView.background != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    imageView.background.colorFilter =
                                        BlendModeColorFilter(0x44000000, BlendMode.SRC_ATOP)
                                } else {
                                    @Suppress("DEPRECATION") imageView.background.setColorFilter(
                                        0x44000000, PorterDuff.Mode.SRC_ATOP
                                    )
                                }
                                imageView.invalidate()
                            }
                        }

                        MotionEvent.ACTION_MOVE -> {
                            rect?.let {
                                if (!it.contains(
                                        imageView.left + event.x.toInt(),
                                        imageView.top + event.y.toInt()
                                    )
                                ) { // User moved outside bounds
                                    if (imageView.background != null) {
                                        imageView.background.clearColorFilter()
                                        imageView.invalidate()
                                    }
                                }
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (imageView.background != null) {
                                imageView.background.clearColorFilter()
                                imageView.invalidate()
                            }
                        }
                    }
                    return false
                }
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setBounceButtonSelector(vararg views: View) {
        for (view in views) {
            view.setOnTouchListener(object : View.OnTouchListener {
                private var rect: Rect? = null
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            rect = Rect(
                                view.left, view.top, view.right, view.bottom
                            )
                            val anim: Animation = ScaleAnimation(
                                1f,
                                0.9f,
                                1f,
                                0.9f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f
                            )
                            anim.fillAfter = true
                            anim.duration = 70
                            view.startAnimation(anim)
                            view.invalidate()
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if (rect != null) {
                                if (!rect?.contains(
                                        view.left + event.x.toInt(), view.top + event.y.toInt()
                                    )!!
                                ) { // User moved outside bounds
                                    val anim: Animation = ScaleAnimation(
                                        0.9f,
                                        1f,
                                        0.9f,
                                        1f,
                                        Animation.RELATIVE_TO_SELF,
                                        0.5f,
                                        Animation.RELATIVE_TO_SELF,
                                        0.5f
                                    )
                                    anim.fillAfter = true
                                    anim.duration = 70
                                    view.startAnimation(anim)
                                    view.invalidate()
                                }
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            // mVelocityTracker.recycle();
                            val anim: Animation = ScaleAnimation(
                                0.9f,
                                1f,
                                0.9f,
                                1f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f
                            )
                            anim.fillAfter = false
                            anim.duration = 70
                            anim.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {

                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                    animation?.cancel()
                                    v.clearAnimation()
                                }

                                override fun onAnimationRepeat(animation: Animation?) {

                                }
                            })
                            v.startAnimation(anim)
                            v.invalidate()
                        }
                    }
                    return false
                }
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setScaleButtonSelector(vararg views: View) {
        for (view in views) {
            view.setOnTouchListener(object : View.OnTouchListener {
                private var rect: Rect? = null
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            rect = Rect(
                                view.left, view.top, view.right, view.bottom
                            )
                            val anim: Animation = ScaleAnimation(
                                1f,
                                1.075f,
                                1f,
                                1.075f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f
                            )
                            anim.fillAfter = true
                            anim.duration = 70
                            view.startAnimation(anim)
                            view.invalidate()
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if (rect != null) {
                                if (!rect?.contains(
                                        view.left + event.x.toInt(), view.top + event.y.toInt()
                                    )!!
                                ) { // User moved outside bounds
                                    val anim: Animation = ScaleAnimation(
                                        1.075f,
                                        1f,
                                        1.075f,
                                        1f,
                                        Animation.RELATIVE_TO_SELF,
                                        0.5f,
                                        Animation.RELATIVE_TO_SELF,
                                        0.5f
                                    )
                                    anim.fillAfter = true
                                    anim.duration = 70
                                    view.startAnimation(anim)
                                    view.invalidate()
                                }
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            // mVelocityTracker.recycle();
                            val anim: Animation = ScaleAnimation(
                                1.075f,
                                1f,
                                1.075f,
                                1f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f
                            )
                            anim.fillAfter = true
                            anim.duration = 70
                            v.startAnimation(anim)
                            v.invalidate()
                        }
                    }
                    return false
                }
            })
        }
    }

    fun setTextViewSelector(vararg views: View) {
        for (view in views) {
            view.setOnTouchListener(object : View.OnTouchListener {
                private var rect: Rect? = null

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        rect = Rect(
                            v.left, v.top, v.right, v.bottom
                        )
                        view.alpha = 0.5f
                    } else if (event.action == MotionEvent.ACTION_UP) {
                        view.alpha = 1f
                    } else if (event.action == MotionEvent.ACTION_MOVE) {
                        if (!rect!!.contains(
                                v.left + event.x.toInt(), v.top + event.y.toInt()
                            )
                        ) { // User moved outside bounds
                            view.alpha = 1f
                        }
                    }
                    return false
                }
            })
        }
    }

    fun isNullOrEmpty(string: String?): Boolean {
        return string?.isEmpty() ?: true
    }

    fun isNullOrEmpty(textView: TextView?): Boolean {
        if (textView == null) return true
        return if (textView.text == null) true else isNullOrEmpty(
            textView.text.toString().trim { it <= ' ' })
    }

    fun isNullOrEmpty(editText: EditText?): Boolean {
        if (editText == null) return true
        return if (editText.text == null) true else isNullOrEmpty(
            editText.text.toString().trim { it <= ' ' })
    }


    @Suppress("unused")
    fun linkify(txtMsg: TextView?) {
        Linkify.addLinks(txtMsg!!, Linkify.ALL)
    }

    @Suppress("unused")
    fun parseDouble(value: String): Double {
        return try {
            value.toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    @Suppress("unused")
    fun parseFloat(value: Int?): Float {
        if (value == null) return 0f
        return try {
            value.toFloat()
        } catch (e: Exception) {
            0f
        }
    }

    @Suppress("unused")
    fun parseLong(value: String): Long {
        return try {
            value.toLong()
        } catch (e: Exception) {
            0
        }
    }

    @Suppress("unused")
    fun parseInt(raw: String): Int {
        return try {
            raw.toInt()
        } catch (e: Exception) {
            0
        }
    }

    @Suppress("unused")
    fun parseInt(raw: Double): Int {
        return try {
            raw.toInt()
        } catch (e: Exception) {
            0
        }
    }

    @Suppress("unused")
    fun getLastCharacters(word: String, count: Int): String {
        if (word.length == count) return word
        return if (word.length > count) word.substring(word.length - count) else ""
    }

    @Suppress("unused")
    fun addStrike(textView: TextView) {
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    @Suppress("unused")
    fun removeStrike(textView: TextView) {
        textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    fun openAppRating(context: Context) {
        val uri: Uri = Uri.parse("market://details?id=" + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            null?.openAppInPlayStore(context)
        }
    }

    fun copyToClipboard(context: Context, roomCode: String, link: String, message: String) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", getSharingMessage(context, roomCode, link))
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getSharingMessage(context: Context, roomCode: String, link: String): CharSequence {
        return ("Let`s do mining together in ${context.getString(R.string.app_name)}! Use link or code to join room:$link : $roomCode")
    }

    fun appMiningSharing(context: Context?, roomCode: String, link: String) {
        val subject = String.format(
            "%s wants you to mining with you!",
            com.app.development.winter.localcache.LocalDataHelper.getUserDetail()?.name
        )

        ShareCompat.IntentBuilder(context as AppCompatActivity).setType("text/plain")
            .setSubject(subject).setChooserTitle(subject)
            .setText(getSharingMessage(context, roomCode, link)).startChooser()
    }

    @Suppress("unused")
    fun appSharing(context: Context, appName: String, message: String) {
        val sharingTitle = "Download $appName App"
        val sharingMessage =
            ("Download $appName App From:" + "https://play.google.com/store/apps/details?id=" + context.packageName).plus(
                ""
            ).plus(message)

        ShareCompat.IntentBuilder(context as AppCompatActivity).setType("text/plain")
            .setSubject(sharingTitle).setText(sharingMessage).setChooserTitle("Share via")
            .startChooser()
    }

    fun appReferralSharing(context: Context, referralLink: String) {
        val subject = String.format(
            "%s wants you to play ${context.getString(R.string.app_name)} with you!",
            /*LocalDataHelper.getUserDetail()?.name*/
        )
        val invitationLink: String = referralLink
        val msg =
            ("Let`s grow our trees together in ${context.getString(R.string.app_name)}! Use my referrer link: $invitationLink")
        ShareCompat.IntentBuilder.from(context as AppCompatActivity).setType("text/plain")
            .setSubject(subject).setChooserTitle(subject).setText(msg).startChooser()
    }

    @Suppress("unused")
    fun convertBitmapToBase64(activity: AppCompatActivity, url: String): String {
        val imageUri: Uri = Uri.parse(url)
        val imageStream: InputStream? = activity.contentResolver.openInputStream(imageUri)
        val selectedImage = BitmapFactory.decodeStream(imageStream)
        return encodeImage(selectedImage)
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    @Suppress("unused")
    fun hideKeyboard(view: View?) {
        if (view == null) return
        val inputMethodManager =
            view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboard(activity: Activity?) {
        activity?.currentFocus ?: return
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            activity.window.decorView.windowToken, 0
        )
    }

    @Suppress("unused")
    fun showKeyboard(view: View?) {
        if (view == null) return
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun sessionIdEncode(s: String, k: Int): String {
        var shifted = ""
        for (element in s) {
            val `val` = element.code
            shifted += (`val` + k).toChar()
        }
        return shifted
    }

    fun dipToPixel(view: View, rate: Int): Int {
        val scale: Float = view.context.resources.displayMetrics.density
        return (rate * scale + 0.5f).toInt()
    }

    fun ViewPager2.removeOverScroll() {
        (getChildAt(0) as? RecyclerView)?.overScrollMode = View.OVER_SCROLL_NEVER
    }

    fun View.enableHapticsForView() {
        isHapticFeedbackEnabled = true
        performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    fun getApplicationInstallDate(context: Context): Int {
        val elapsedTimestamp: Long =
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        val timediff = System.currentTimeMillis() - elapsedTimestamp
        return (timediff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun TabLayout.toggleState(state: Boolean) {

        val tabStrip = getChildAt(0) as LinearLayout
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).setOnTouchListener { v, event -> state }
        }
    }

    @JvmStatic
    fun printLog(key: String, message: String) {
        //if (BuildConfig.DEBUG) {
        Log.e(key, message)
        //}
    }

    fun changeStatusBarColor(context: Context, color: Int) {
        val window: Window = (context as AppCompatActivity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(context, color)
    }

    private fun extractPackageNames(resolveInfos: List<ResolveInfo>): Set<String> {
        val packageNameSet: MutableSet<String> = HashSet()
        for (ri in resolveInfos) {
            packageNameSet.add(ri.activityInfo.packageName)
        }
        return packageNameSet
    }

    private fun launchNativeBeforeApi30(context: Context, uri: Uri): Boolean {
        val pm = context.packageManager

        // Get all Apps that resolve a generic url
        val browserActivityIntent =
            Intent().setAction(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.fromParts("http", "", null))
        val genericResolvedList: Set<String>? =
            extractPackageNames(pm.queryIntentActivities(browserActivityIntent, 0))

        // Get all apps that resolve the specific Url
        val specializedActivityIntent =
            Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
        val resolvedSpecializedList: MutableSet<String>? = extractPackageNames(
            pm.queryIntentActivities(
                specializedActivityIntent, 0
            )
        )?.toMutableSet()

        // Keep only the Urls that resolve the specific, but not the generic
        // urls.
        genericResolvedList?.let { resolvedSpecializedList?.removeAll(it) }

        // If the list is empty, no native legendary handlers were found.
        if (resolvedSpecializedList?.isEmpty() == true) {
            return false
        }

        // We found native handlers. Launch the Intent.
        specializedActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(specializedActivityIntent)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun launchNativeApi30(context: Context, uri: Uri?): Boolean {
        val nativeAppIntent =
            Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
            )
        return try {
            context.startActivity(nativeAppIntent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    /* private fun launchUri(context: Context, uri: Uri) {
         val launched: Boolean = if (Build.VERSION.SDK_INT >= 30) launchNativeApi30(
             context, uri
         ) else launchNativeBeforeApi30(context, uri)
         if (!launched) {
             CustomTabsIntent.Builder().build().launchUrl(context, uri)
         }
     }*/

    fun openWebPage(context: Context, url: String?) {
        try {
            if (!URLUtil.isValidUrl(url)) {
                Toast.makeText(context, "This is not a valid link", Toast.LENGTH_LONG).show()
            } else {
                @ColorInt val colorPrimaryLight =
                    ContextCompat.getColor(context, R.color.colorPrimary)
                @ColorInt val colorPrimaryDark =
                    ContextCompat.getColor(context, R.color.colorPrimary)

                val customTabsIntentBuilder: CustomTabsIntent.Builder =
                    CustomTabsIntent.Builder().setUrlBarHidingEnabled(true).setShowTitle(true)

                        // set the default color scheme
                        .setDefaultColorSchemeParams(
                            CustomTabColorSchemeParams.Builder().setToolbarColor(colorPrimaryLight)
                                .build()
                        )
                        // set the alternative dark color scheme
                        .setColorSchemeParams(
                            CustomTabsIntent.COLOR_SCHEME_DARK,
                            CustomTabColorSchemeParams.Builder().setToolbarColor(colorPrimaryDark)
                                .build()
                        ).setStartAnimations(
                            context, android.R.anim.slide_in_left, android.R.anim.slide_out_right
                        ).setExitAnimations(
                            context, android.R.anim.slide_out_right, android.R.anim.slide_out_right
                        )/*.setToolbarCornerRadiusDp(20.toPx(context))*/
                val customTabsIntent = customTabsIntentBuilder.build()
                customTabsIntent.launchUrl(context, Uri.parse(url))

//                launchUri(context, Uri.parse(url))

                /*   val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                   context.startActivity(intent)*/

            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context, "You don't have any browser to open web page", Toast.LENGTH_LONG
            ).show()
        }
    }

    fun String.openAppInPlayStore(context: Context) {
        if (isNullOrEmpty().not()) {
            openWebPage(context, this)
        } else {
            openWebPage(
                context, "http://play.google.com/store/apps/details?id=" + context.packageName
            )
        }
    }

    fun clearAnimationFromTag(view: View) {
        val objectTag: Any = view.tag
        if (objectTag is ObjectAnimator) {
            objectTag.cancel()
        }
    }


//    fun getUTCOffset(): Long {
//        val offset: Int =
//            TimeZone.getTimeZone("UTC").rawOffset + TimeZone.getTimeZone("UTC").dstSavings
//        return Controller.instance.getInterNetTime() - offset
//    }

    fun openLink(context: Context, link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        context.let { contextWrapped ->
            if (intent.resolveActivity(contextWrapped.packageManager) == null) {
                // No Activity found that can handle this intent.
                contextWrapped.startActivity(
                    Intent.createChooser(
                        intent, "Choose an option"
                    )
                )
            } else {
                // There is an activity which can handle this intent.
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }

    fun setBlurView(
        activity: AdvancedBaseActivity<*, *, *, *>?,
        blurView: com.app.development.winter.shared.views.BlurView,
        blurLevel: Int = 45
    ) {
        activity?.let { act ->
            val view = act.getContainerView()
            if (!isLowMemory(activity)) {
                blurView.setBlur(act, view, blurLevel)
            }
        }
    }

    fun isLowMemory(activity: AppCompatActivity): Boolean {
        val mi = ActivityManager.MemoryInfo()
        val activityManager =
            activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        activityManager?.getMemoryInfo(mi)
        val percentAvail: Double = mi.availMem / mi.totalMem.toDouble() * 100.0
        return percentAvail <= 20
    }

    fun setBlurView(context: Context?, parentView: View?, blurView: AppCompatImageView) {
        if (context != null) {
            /* Glide.with(context).asBitmap().load(parentView?.createBitmap())
                 .transform(BlurTransformation(context)).into(blurView)*/

            Glide.with(context).load(parentView?.createBitmap())
                .transform(BlurTransformation(context)).into(blurView)
        }
    }

    fun minimizeApp(context: Activity) {
        context.moveTaskToBack(true)
        val sentAppToBackground: Boolean = context.moveTaskToBack(true)
        if (!sentAppToBackground) {
            val i = Intent()
            i.setAction(Intent.ACTION_MAIN)
            i.addCategory(Intent.CATEGORY_HOME)
            context.startActivity(i)
        }
    }
}